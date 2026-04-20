package com.norton.backend.controllers.officer;

import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.officers.MeResponse;
import com.norton.backend.dto.responses.officers.OfficerResponseDto;
import com.norton.backend.dto.responses.officers.OfficerStatsResponse;
import com.norton.backend.services.officer.OfficerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(OfficerController.BASE_URL)
public class OfficerController {

  public static final String BASE_URL = "/api/v1/officer";

  private final OfficerService officerService;

  @GetMapping("/me")
  public ResponseEntity<MeResponse> getMyProfile() {
    return ResponseEntity.ok(officerService.getMyProfile());
  }

  @GetMapping
  public ResponseEntity<PageResponse<OfficerResponseDto>> getAllOfficers(
      @PageableDefault(size = 10, sort = "id") Pageable request) {
    return ResponseEntity.ok(officerService.getAllOfficers(request));
  }

  @GetMapping("/stats")
  public OfficerStatsResponse getStats() {
    return officerService.getOfficerStats();
  }
}
