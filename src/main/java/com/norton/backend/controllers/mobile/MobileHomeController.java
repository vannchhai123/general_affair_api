package com.norton.backend.controllers.mobile;

import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import com.norton.backend.dto.responses.mobile.MobileHomeResponse;
import com.norton.backend.dto.responses.mobile.MobileMeetingCalendarResponse;
import com.norton.backend.services.mobile.MobileHomeService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(MobileHomeController.BASE_URL)
public class MobileHomeController {

  public static final String BASE_URL = "/api/v1/mobile";

  private final MobileHomeService mobileHomeService;

  @GetMapping("/home")
  public ResponseEntity<MobileHomeResponse> getHomeData() {
    return ResponseEntity.ok(mobileHomeService.getHomeData());
  }

  @GetMapping("/meetings/{id}")
  public ResponseEntity<CreateInvitationResponse> getMeetingDetail(@PathVariable Long id) {
    return ResponseEntity.ok(mobileHomeService.getMeetingDetail(id));
  }

  @GetMapping("/meetings/calendar")
  public ResponseEntity<MobileMeetingCalendarResponse> getMeetingCalendar(
      @RequestParam(required = false)
          @Min(value = 1000, message = "year must be a valid four-digit year")
          @Max(value = 9999, message = "year must be a valid four-digit year")
          Integer year,
      @RequestParam(required = false)
          @Min(value = 1, message = "month must be between 1 and 12")
          @Max(value = 12, message = "month must be between 1 and 12")
          Integer month) {
    LocalDate now = LocalDate.now();
    int resolvedYear = (year != null) ? year : now.getYear();
    int resolvedMonth = (month != null) ? month : now.getMonthValue();
    return ResponseEntity.ok(mobileHomeService.getMeetingCalendar(resolvedYear, resolvedMonth));
  }
}
