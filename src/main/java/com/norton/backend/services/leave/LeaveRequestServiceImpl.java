package com.norton.backend.services.leave;

import com.norton.backend.dto.request.leave.CreateLeaveRequestRequest;
import com.norton.backend.dto.request.leave.UpdateLeaveRequestRequest;
import com.norton.backend.dto.responses.leave.LeaveRequestResponse;
import com.norton.backend.dto.responses.leave.LeaveTypeResponse;
import com.norton.backend.models.LeaveRequestModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.repositories.LeaveRequestRepository;
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

  @Override
  public List<LeaveTypeResponse> getLeaveTypes() {
    return List.of(
        LeaveTypeResponse.builder()
            .key("Annual Leave")
            .labelEn("Annual Leave")
            .labelKh("ច្បាប់សម្រាកប្រចាំឆ្នាំ")
            .description("Standard annual paid leave allocation")
            .build(),
        LeaveTypeResponse.builder()
            .key("Sick Leave")
            .labelEn("Sick Leave")
            .labelKh("ច្បាប់ជំងឺ")
            .description("Leave taken due to medical or health conditions")
            .build(),
        LeaveTypeResponse.builder()
            .key("Personal Leave")
            .labelEn("Personal Leave")
            .labelKh("ច្បាប់ផ្ទាល់ខ្លួន")
            .description("Leave for urgent personal affairs and family business")
            .build(),
        LeaveTypeResponse.builder()
            .key("Special Leave")
            .labelEn("Special Leave")
            .labelKh("ច្បាប់ពិសេស")
            .description("Special leave for weddings, events, or authorized activities")
            .build(),
        LeaveTypeResponse.builder()
            .key("Maternity Leave")
            .labelEn("Maternity / Paternity Leave")
            .labelKh("ច្បាប់មាតុភាព / បិតុភាព")
            .description("Parental leave for newborn care")
            .build());
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
    return leaveRequestRepository.findByOfficerIdOrderByIdDesc(officerId).stream()
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
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Officer not found with id: " + request.getOfficerId()));

    LocalDate startDate = LocalDate.parse(request.getStartDate());
    LocalDate endDate = LocalDate.parse(request.getEndDate());

    LeaveRequestModel leaveRequest =
        LeaveRequestModel.builder()
            .officer(officer)
            .startDate(startDate)
            .endDate(endDate)
            .leaveType(request.getLeaveType() != null ? request.getLeaveType() : "Annual Leave")
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

    LeaveRequestModel saved = leaveRequestRepository.save(leaveRequest);
    return mapToResponse(saved);
  }

  private LeaveRequestResponse mapToResponse(LeaveRequestModel model) {
    OfficerModel officer = model.getOfficer();
    OfficerModel approver = model.getApprovedByOfficer();

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

    return LeaveRequestResponse.builder()
        .id(model.getId())
        .officerId(officer != null ? officer.getId() : null)
        .approvedBy(approver != null ? approver.getId() : null)
        .startDate(model.getStartDate() != null ? model.getStartDate().toString() : null)
        .endDate(model.getEndDate() != null ? model.getEndDate().toString() : null)
        .leaveType(model.getLeaveType())
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
