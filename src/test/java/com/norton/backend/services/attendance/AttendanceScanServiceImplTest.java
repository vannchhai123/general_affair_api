package com.norton.backend.services.attendance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.norton.backend.dto.request.AttendanceScanRequest;
import com.norton.backend.dto.responses.attendances.AttendanceScanSuccessResponse;
import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.enums.ShiftStatus;
import com.norton.backend.models.*;
import com.norton.backend.repositories.*;
import com.norton.backend.security.JwtService;
import com.norton.backend.services.qr.QrSessionLifecycleService;
import com.norton.backend.services.shift.ShiftResolutionService;
import com.norton.backend.services.shift.ShiftResolutionService.ShiftWindow;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceScanServiceImplTest {

  @Mock private QrSessionRepository qrSessionRepository;
  @Mock private QrSessionCheckInRepository qrSessionCheckInRepository;
  @Mock private OfficerRepository officerRepository;
  @Mock private AttendanceRepository attendanceRepository;
  @Mock private AttendanceSessionRepository attendanceSessionRepository;
  @Mock private AttendanceStatusRepository attendanceStatusRepository;
  @Mock private JwtService jwtService;
  @Mock private QrSessionLifecycleService qrSessionLifecycleService;
  @Mock private ShiftResolutionService shiftResolutionService;

  @InjectMocks private AttendanceScanServiceImpl attendanceScanService;

  @Test
  void testSubmitScan_success() {
    AttendanceScanRequest request = new AttendanceScanRequest();
    request.setToken("dummy-token");
    request.setOfficerCode("OFF001");
    request.setDeviceId("device-123");
    request.setScannedAt(Instant.now());

    Claims claims =
        new DefaultClaims(
            Map.of(
                "sub", "qr-session-kiosk",
                "purpose", "qr-kiosk",
                "sessionId", "session-123"));

    when(jwtService.extractAllClaims(anyString())).thenReturn(claims);

    QrSessionModel qrSession = new QrSessionModel();
    qrSession.setToken("session-123");
    qrSession.setStatus("active");
    qrSession.setLocation("Office A");

    when(qrSessionRepository.findByTokenWithCreatedBy("session-123"))
        .thenReturn(Optional.of(qrSession));

    when(qrSessionLifecycleService.isScanAllowed(any(), any())).thenReturn(true);

    PositionModel position = new PositionModel();
    position.setId(1L);

    OfficerModel officer = new OfficerModel();
    officer.setId(2L);
    officer.setOfficerCode("OFF001");
    officer.setFirstNameEn("John");
    officer.setLastNameEn("Doe");
    officer.setStatus(OfficerStatus.ACTIVE);
    officer.setPosition(position);

    when(officerRepository.findByOfficerCode("OFF001")).thenReturn(Optional.of(officer));

    ShiftModel shift = new ShiftModel();
    shift.setId(3L);
    shift.setName("Morning Shift");
    shift.setStartTime(LocalTime.of(8, 0));
    shift.setEndTime(LocalTime.of(17, 0));
    shift.setStatus(ShiftStatus.ACTIVE);
    shift.setIsActive(true);

    ShiftWindow window =
        new ShiftWindow(
            shift,
            LocalDate.now(),
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusHours(8),
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().plusHours(9));

    when(shiftResolutionService.resolveOfficerShift(eq(officer), any(LocalDateTime.class)))
        .thenReturn(Optional.of(window));
    when(shiftResolutionService.shiftLabel(shift)).thenReturn("Morning Shift");
    when(shiftResolutionService.isCheckInAllowed(any(), any())).thenReturn(true);

    AttendanceStatusModel presentStatus = new AttendanceStatusModel();
    presentStatus.setCode("PRESENT");
    presentStatus.setName("Present");
    when(attendanceStatusRepository.findByCode("PRESENT")).thenReturn(Optional.of(presentStatus));

    AttendanceModel attendance = new AttendanceModel();
    attendance.setId(4L);
    attendance.setOfficer(officer);
    attendance.setDate(LocalDate.now());

    when(attendanceRepository.findByOfficerOfficerCodeAndDate("OFF001", LocalDate.now()))
        .thenReturn(Optional.empty());
    when(attendanceRepository.save(any(AttendanceModel.class))).thenReturn(attendance);

    when(attendanceSessionRepository.findByAttendanceIdAndShiftId(anyLong(), anyLong()))
        .thenReturn(Optional.empty());

    when(attendanceSessionRepository.findByAttendanceId(anyLong()))
        .thenReturn(Collections.emptyList());

    org.springframework.test.util.ReflectionTestUtils.setField(
        attendanceScanService, "scanTimezone", "Asia/Phnom_Penh");

    AttendanceScanSuccessResponse response = attendanceScanService.submitScan(request);

    assertNotNull(response);
    assertTrue(response.isSuccess());
  }

  @Test
  void testSubmitScan_nullCheckInInExistingSession() {
    AttendanceScanRequest request = new AttendanceScanRequest();
    request.setToken("dummy-token");
    request.setOfficerCode("OFF001");
    request.setDeviceId("device-123");
    request.setScannedAt(Instant.now());

    Claims claims =
        new DefaultClaims(
            Map.of(
                "sub", "qr-session-kiosk",
                "purpose", "qr-kiosk",
                "sessionId", "session-123"));

    when(jwtService.extractAllClaims(anyString())).thenReturn(claims);

    QrSessionModel qrSession = new QrSessionModel();
    qrSession.setToken("session-123");
    qrSession.setStatus("active");
    qrSession.setLocation("Office A");

    when(qrSessionRepository.findByTokenWithCreatedBy("session-123"))
        .thenReturn(Optional.of(qrSession));

    when(qrSessionLifecycleService.isScanAllowed(any(), any())).thenReturn(true);

    PositionModel position = new PositionModel();
    position.setId(1L);

    OfficerModel officer = new OfficerModel();
    officer.setId(2L);
    officer.setOfficerCode("OFF001");
    officer.setFirstNameEn("John");
    officer.setLastNameEn("Doe");
    officer.setStatus(OfficerStatus.ACTIVE);
    officer.setPosition(position);

    when(officerRepository.findByOfficerCode("OFF001")).thenReturn(Optional.of(officer));

    ShiftModel shift = new ShiftModel();
    shift.setId(3L);
    shift.setName("Morning Shift");
    shift.setStartTime(LocalTime.of(8, 0));
    shift.setEndTime(LocalTime.of(17, 0));
    shift.setStatus(ShiftStatus.ACTIVE);
    shift.setIsActive(true);

    ShiftWindow window =
        new ShiftWindow(
            shift,
            LocalDate.now(),
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().plusHours(8),
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().plusHours(9));

    when(shiftResolutionService.resolveOfficerShift(eq(officer), any(LocalDateTime.class)))
        .thenReturn(Optional.of(window));
    when(shiftResolutionService.shiftLabel(shift)).thenReturn("Morning Shift");
    when(shiftResolutionService.isCheckInAllowed(any(), any())).thenReturn(true);

    AttendanceStatusModel presentStatus = new AttendanceStatusModel();
    presentStatus.setCode("PRESENT");
    presentStatus.setName("Present");
    when(attendanceStatusRepository.findByCode("PRESENT")).thenReturn(Optional.of(presentStatus));

    AttendanceModel attendance = new AttendanceModel();
    attendance.setId(4L);
    attendance.setOfficer(officer);
    attendance.setDate(LocalDate.now());

    when(attendanceRepository.findByOfficerOfficerCodeAndDate("OFF001", LocalDate.now()))
        .thenReturn(Optional.of(attendance));
    when(attendanceRepository.save(any(AttendanceModel.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AttendanceSessionModel existingSession = new AttendanceSessionModel();
    existingSession.setId(5L);
    existingSession.setAttendance(attendance);
    existingSession.setShift(shift);
    existingSession.setCheckIn(null); // NULL check-in!
    existingSession.setCheckOut(null);

    when(attendanceSessionRepository.findByAttendanceIdAndShiftId(anyLong(), anyLong()))
        .thenReturn(Optional.of(existingSession));
    when(attendanceSessionRepository.findByAttendanceId(anyLong()))
        .thenReturn(Collections.singletonList(existingSession));

    org.springframework.test.util.ReflectionTestUtils.setField(
        attendanceScanService, "scanTimezone", "Asia/Phnom_Penh");

    AttendanceScanSuccessResponse response = attendanceScanService.submitScan(request);

    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals("Check-in recorded successfully", response.getMessage());
  }
}
