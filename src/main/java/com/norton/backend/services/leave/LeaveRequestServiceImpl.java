package com.norton.backend.services.leave;

import com.norton.backend.dto.request.leave.CreateLeaveRequestRequest;
import com.norton.backend.dto.request.leave.UpdateLeaveRequestRequest;
import com.norton.backend.dto.responses.leave.LeaveRequestResponse;
import com.norton.backend.dto.responses.leave.LeaveTypeResponse;
import com.norton.backend.models.LeaveRequestModel;
import com.norton.backend.models.LeaveTypeModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.repositories.LeaveRequestRepository;
import com.norton.backend.repositories.LeaveTypeRepository;
import com.norton.backend.repositories.OfficerRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
            .orElseThrow(
                () -> new IllegalArgumentException("Leave request not found with id: " + id));
    return mapToResponse(leaveRequest);
  }

  @Override
  @Transactional
  public LeaveRequestResponse cancelLeaveRequest(Long id) {
    LeaveRequestModel leaveRequest =
        leaveRequestRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException("Leave request not found with id: " + id));
    leaveRequest.setStatus("Cancelled");
    LeaveRequestModel saved = leaveRequestRepository.save(leaveRequest);
    return mapToResponse(saved);
  }

  @Override
  @Transactional
  public LeaveRequestResponse createLeaveRequest(CreateLeaveRequestRequest request) {
    OfficerModel officer =
        officerRepository
            .findById(request.getOfficerId())
            .orElseGet(
                () ->
                    officerRepository
                        .findByUserId(request.getOfficerId())
                        .orElseThrow(
                            () ->
                                new IllegalArgumentException(
                                    "Officer not found with id: " + request.getOfficerId())));

    LocalDate startDate = LocalDate.parse(request.getStartDate());
    LocalDate endDate = LocalDate.parse(request.getEndDate());

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

    LeaveRequestModel leaveRequest =
        LeaveRequestModel.builder()
            .officer(officer)
            .startDate(startDate)
            .endDate(endDate)
            .leaveType(leaveTypeModel)
            .totalDays(request.getTotalDays() != null ? request.getTotalDays() : 1)
            .reason(request.getReason() != null ? request.getReason() : "")
            .status(request.getStatus() != null ? request.getStatus() : "Pending")
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
            .orElseThrow(
                () -> new IllegalArgumentException("Leave request not found with id: " + id));

    if (request.getStatus() != null) {
      leaveRequest.setStatus(request.getStatus());
      if ("Approved".equalsIgnoreCase(request.getStatus())) {
        leaveRequest.setApprovedAt(LocalDateTime.now());
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

    if (request.getStartDate() != null) {
      leaveRequest.setStartDate(LocalDate.parse(request.getStartDate()));
    }

    if (request.getEndDate() != null) {
      leaveRequest.setEndDate(LocalDate.parse(request.getEndDate()));
    }

    if (request.getTotalDays() != null) {
      leaveRequest.setTotalDays(request.getTotalDays());
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
}
