package com.norton.backend.services.leave;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.norton.backend.dto.request.leave.CreateLeaveRequestRequest;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ConflictException;
import com.norton.backend.models.LeaveRequestModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.repositories.LeaveRequestRepository;
import com.norton.backend.repositories.LeaveTypeRepository;
import com.norton.backend.repositories.OfficerRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplTest {

  @Mock private LeaveRequestRepository leaveRequestRepository;

  @Mock private OfficerRepository officerRepository;

  @Mock private LeaveTypeRepository leaveTypeRepository;

  @InjectMocks private LeaveRequestServiceImpl leaveRequestService;

  private OfficerModel mockOfficer;

  @BeforeEach
  void setUp() {
    mockOfficer = OfficerModel.builder().firstNameEn("John").lastNameEn("Doe").build();
    mockOfficer.setId(1L);
  }

  @Test
  void createLeaveRequest_shouldThrowBadRequest_whenEndDateBeforeStartDate() {
    CreateLeaveRequestRequest request = new CreateLeaveRequestRequest();
    request.setOfficerId(1L);
    request.setStartDate("2026-08-10");
    request.setEndDate("2026-08-05");

    when(officerRepository.findById(1L)).thenReturn(Optional.of(mockOfficer));

    BadRequestException ex =
        assertThrows(
            BadRequestException.class, () -> leaveRequestService.createLeaveRequest(request));
    assertEquals("End date cannot be before start date", ex.getMessage());
  }

  @Test
  void createLeaveRequest_shouldThrowConflict_whenOverlappingRequestExists() {
    CreateLeaveRequestRequest request = new CreateLeaveRequestRequest();
    request.setOfficerId(1L);
    request.setStartDate("2026-08-10");
    request.setEndDate("2026-08-12");

    when(officerRepository.findById(1L)).thenReturn(Optional.of(mockOfficer));
    when(leaveRequestRepository.existsOverlappingRequest(
            eq(1L), eq(LocalDate.of(2026, 8, 10)), eq(LocalDate.of(2026, 8, 12))))
        .thenReturn(true);

    ConflictException ex =
        assertThrows(
            ConflictException.class, () -> leaveRequestService.createLeaveRequest(request));
    assertEquals(
        "You already have an active leave request covering the selected date(s)", ex.getMessage());
  }

  @Test
  void createLeaveRequest_shouldSucceed_whenNoOverlapExists() {
    CreateLeaveRequestRequest request = new CreateLeaveRequestRequest();
    request.setOfficerId(1L);
    request.setStartDate("2026-08-10");
    request.setEndDate("2026-08-12");

    when(officerRepository.findById(1L)).thenReturn(Optional.of(mockOfficer));
    when(leaveRequestRepository.existsOverlappingRequest(
            eq(1L), eq(LocalDate.of(2026, 8, 10)), eq(LocalDate.of(2026, 8, 12))))
        .thenReturn(false);

    LeaveRequestModel savedModel =
        LeaveRequestModel.builder()
            .officer(mockOfficer)
            .startDate(LocalDate.of(2026, 8, 10))
            .endDate(LocalDate.of(2026, 8, 12))
            .totalDays(3)
            .status("Pending")
            .build();
    savedModel.setId(100L);

    when(leaveRequestRepository.save(any(LeaveRequestModel.class))).thenReturn(savedModel);

    var response = leaveRequestService.createLeaveRequest(request);

    assertNotNull(response);
    assertEquals(100L, response.getId());
    assertEquals(3, response.getTotalDays());
  }
}
