package com.norton.backend.services.leave;

import com.norton.backend.dto.request.leave.CreateLeaveRequestRequest;
import com.norton.backend.dto.request.leave.UpdateLeaveRequestRequest;
import com.norton.backend.dto.responses.leave.LeaveRequestResponse;
import com.norton.backend.dto.responses.leave.LeaveTypeResponse;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ConflictException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.LeaveRequestModel;
import com.norton.backend.models.LeaveTypeModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.repositories.LeaveRequestRepository;
import com.norton.backend.repositories.LeaveTypeRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.utils.SecurityUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

  private final LeaveRequestRepository leaveRequestRepository;
  private final OfficerRepository officerRepository;
  private final LeaveTypeRepository leaveTypeRepository;
  private final SecurityUtils securityUtils;

  @Override
  @Transactional(readOnly = true)
  public List<LeaveTypeResponse> getLeaveTypes() {
    return leaveTypeRepository.findByIsActiveTrueOrderByIdAsc().stream()
        .map(
            t ->
                LeaveTypeResponse.builder()
                    .id(t.getId())
                    .key(t.getKey())
                    .labelEn(t.getLabelEn())
                    .labelKh(t.getLabelKh())
                    .description(t.getDescription())
                    .isActive(t.getIsActive())
                    .build())
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<LeaveRequestResponse> getAllLeaveRequests() {
    return leaveRequestRepository.findAllByOrderByIdDesc().stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<LeaveRequestResponse> getOfficerLeaveRequests(Long officerId) {
    if (officerId == null) {
      return List.of();
    }
    return leaveRequestRepository.findByOfficerIdOrUserIdOrderByIdDesc(officerId).stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public LeaveRequestResponse getLeaveRequestById(Long id) {
    LeaveRequestModel leaveRequest =
        leaveRequestRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request", "id", id));
    return mapToResponse(leaveRequest);
  }

  @Override
  @Transactional
  public LeaveRequestResponse cancelLeaveRequest(Long id) {
    LeaveRequestModel leaveRequest =
        leaveRequestRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request", "id", id));
    leaveRequest.setStatus("Cancelled");
    LeaveRequestModel saved = leaveRequestRepository.save(leaveRequest);
    return mapToResponse(saved);
  }

  @Override
  @Transactional
  public LeaveRequestResponse createLeaveRequest(CreateLeaveRequestRequest request) {
    if (request.getOfficerId() == null) {
      throw new BadRequestException("Officer ID must be specified");
    }

    OfficerModel officer =
        officerRepository
            .findById(request.getOfficerId())
            .orElseGet(
                () ->
                    officerRepository
                        .findByUserId(request.getOfficerId())
                        .orElseThrow(
                            () ->
                                new ResourceNotFoundException(
                                    "Officer", "id", request.getOfficerId())));

    if (request.getStartDate() == null || request.getStartDate().isBlank()) {
      throw new BadRequestException("Start date is required");
    }
    if (request.getEndDate() == null || request.getEndDate().isBlank()) {
      throw new BadRequestException("End date is required");
    }

    LocalDate startDate;
    LocalDate endDate;
    try {
      startDate = LocalDate.parse(request.getStartDate());
      endDate = LocalDate.parse(request.getEndDate());
    } catch (DateTimeParseException e) {
      throw new BadRequestException("Invalid date format. Use YYYY-MM-DD");
    }

    if (endDate.isBefore(startDate)) {
      throw new BadRequestException("End date cannot be before start date");
    }

    boolean existsOverlapping =
        leaveRequestRepository.existsOverlappingRequest(officer.getId(), startDate, endDate);
    if (existsOverlapping) {
      throw new ConflictException(
          "You already have an active leave request covering the selected date(s)");
    }

    LeaveTypeModel leaveTypeModel = null;
    if (request.getLeaveTypeId() != null) {
      leaveTypeModel = leaveTypeRepository.findById(request.getLeaveTypeId()).orElse(null);
    }
    if (leaveTypeModel == null && request.getLeaveType() != null) {
      leaveTypeModel = leaveTypeRepository.findByKey(request.getLeaveType()).orElse(null);
    }
    if (leaveTypeModel == null) {
      leaveTypeModel = leaveTypeRepository.findByKey("Annual Leave").orElse(null);
    }

    int calculatedDays = (int) (ChronoUnit.DAYS.between(startDate, endDate) + 1);
    int totalDays =
        (request.getTotalDays() != null && request.getTotalDays() > 0)
            ? request.getTotalDays()
            : calculatedDays;

    LeaveRequestModel leaveRequest =
        LeaveRequestModel.builder()
            .officer(officer)
            .startDate(startDate)
            .endDate(endDate)
            .leaveType(leaveTypeModel)
            .totalDays(totalDays)
            .reason(request.getReason() != null ? request.getReason() : "")
            .status(request.getStatus() != null ? normalizeStatus(request.getStatus()) : "Pending")
            .build();

    LeaveRequestModel saved = leaveRequestRepository.save(leaveRequest);
    return mapToResponse(saved);
  }

  @Override
  @Transactional
  public LeaveRequestResponse updateLeaveRequest(Long id, UpdateLeaveRequestRequest request) {
    LeaveRequestModel leaveRequest =
        leaveRequestRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request", "id", id));

    if (request.getStatus() != null && !request.getStatus().isBlank()) {
      String normalizedStatus = normalizeStatus(request.getStatus());
      leaveRequest.setStatus(normalizedStatus);
      if ("Approved".equalsIgnoreCase(normalizedStatus)
          || "Rejected".equalsIgnoreCase(normalizedStatus)) {
        leaveRequest.setApprovedAt(LocalDateTime.now());
        try {
          Long currentUserId = securityUtils.getCurrentUserId();
          OfficerModel approver = officerRepository.findByUserId(currentUserId).orElse(null);
          if (approver != null) {
            leaveRequest.setApprovedByOfficer(approver);
          }
        } catch (Exception ignored) {
          // If called without auth context in tests
        }
      }
    }

    if (request.getReason() != null) {
      leaveRequest.setReason(request.getReason());
    }

    if (request.getLeaveTypeId() != null || request.getLeaveType() != null) {
      LeaveTypeModel leaveTypeModel = null;
      if (request.getLeaveTypeId() != null) {
        leaveTypeModel = leaveTypeRepository.findById(request.getLeaveTypeId()).orElse(null);
      }
      if (leaveTypeModel == null && request.getLeaveType() != null) {
        leaveTypeModel = leaveTypeRepository.findByKey(request.getLeaveType()).orElse(null);
      }
      if (leaveTypeModel != null) {
        leaveRequest.setLeaveType(leaveTypeModel);
      }
    }

    LocalDate newStartDate =
        request.getStartDate() != null
            ? LocalDate.parse(request.getStartDate())
            : leaveRequest.getStartDate();
    LocalDate newEndDate =
        request.getEndDate() != null
            ? LocalDate.parse(request.getEndDate())
            : leaveRequest.getEndDate();

    if (newEndDate.isBefore(newStartDate)) {
      throw new BadRequestException("End date cannot be before start date");
    }

    if (request.getStartDate() != null || request.getEndDate() != null) {
      boolean existsOverlapping =
          leaveRequestRepository.existsOverlappingRequestExcludingId(
              leaveRequest.getOfficer().getId(), newStartDate, newEndDate, id);
      if (existsOverlapping) {
        throw new ConflictException(
            "You already have an active leave request covering the selected date(s)");
      }
      leaveRequest.setStartDate(newStartDate);
      leaveRequest.setEndDate(newEndDate);
    }

    if (request.getTotalDays() != null) {
      leaveRequest.setTotalDays(request.getTotalDays());
    } else if (request.getStartDate() != null || request.getEndDate() != null) {
      leaveRequest.setTotalDays((int) (ChronoUnit.DAYS.between(newStartDate, newEndDate) + 1));
    }

    LeaveRequestModel saved = leaveRequestRepository.save(leaveRequest);
    return mapToResponse(saved);
  }

  private LeaveRequestResponse mapToResponse(LeaveRequestModel model) {
    OfficerModel officer = model.getOfficer();
    OfficerModel approver = model.getApprovedByOfficer();
    LeaveTypeModel lt = model.getLeaveType();

    String firstName =
        officer != null
            ? (officer.getFirstNameKh() != null
                ? officer.getFirstNameKh()
                : officer.getFirstNameEn())
            : "";
    String lastName =
        officer != null
            ? (officer.getLastNameKh() != null ? officer.getLastNameKh() : officer.getLastNameEn())
            : "";

    String departmentName = "General Department";
    if (officer != null && officer.getOffice() != null) {
      departmentName =
          officer.getOffice().getNameKh() != null
              ? officer.getOffice().getNameKh()
              : officer.getOffice().getName();
    }

    String approverName =
        approver != null
            ? (approver.getLastNameKh() + " " + approver.getFirstNameKh())
            : ("Approved".equalsIgnoreCase(model.getStatus()) ? "ប្រធាននាយកដ្ឋាន" : null);

    Long leaveTypeId = lt != null ? lt.getId() : null;
    String leaveTypeKey = lt != null ? lt.getKey() : "Annual Leave";

    return LeaveRequestResponse.builder()
        .id(model.getId())
        .officerId(officer != null ? officer.getId() : null)
        .approvedBy(approver != null ? approver.getId() : null)
        .startDate(model.getStartDate() != null ? model.getStartDate().toString() : null)
        .endDate(model.getEndDate() != null ? model.getEndDate().toString() : null)
        .leaveTypeId(leaveTypeId)
        .leaveType(leaveTypeKey)
        .totalDays(model.getTotalDays())
        .reason(model.getReason())
        .status(model.getStatus())
        .approvedAt(model.getApprovedAt() != null ? model.getApprovedAt().toString() : null)
        .firstName(firstName)
        .lastName(lastName)
        .department(departmentName)
        .approverName(approverName)
        .build();
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return "Pending";
    }
    String s = status.trim();
    if ("APPROVED".equalsIgnoreCase(s)) return "Approved";
    if ("REJECTED".equalsIgnoreCase(s)) return "Rejected";
    if ("CANCELLED".equalsIgnoreCase(s) || "CANCELED".equalsIgnoreCase(s)) return "Cancelled";
    if ("PENDING".equalsIgnoreCase(s)) return "Pending";
    return Character.toUpperCase(s.charAt(0))
        + (s.length() > 1 ? s.substring(1).toLowerCase() : "");
  }
}
