package com.norton.backend.controllers.officer;

import com.norton.backend.dto.request.CreateOfficerRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.officers.CreateOfficerResponse;
import com.norton.backend.dto.responses.officers.MeResponse;
import com.norton.backend.dto.responses.officers.OfficerResponseDto;
import com.norton.backend.dto.responses.officers.OfficerStatsResponse;
import com.norton.backend.services.officer.OfficerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(OfficerController.BASE_URL)
public class OfficerController {

  public static final String BASE_URL = "/api/v1/officer";

  private final OfficerService officerService;

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<MeResponse> getMyProfile() {
    return ResponseEntity.ok(officerService.getMyProfile());
  }

  @PostMapping
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).OFFICER_CREATE)")
  public ResponseEntity<CreateOfficerResponse> createOfficer(
      @Valid @RequestBody CreateOfficerRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(officerService.createOfficer(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).OFFICER_UPDATE)")
  public ResponseEntity<CreateOfficerResponse> updateOfficer(
      @PathVariable Long id, @Valid @RequestBody CreateOfficerRequest request) {
    return ResponseEntity.ok(officerService.updateOfficer(id, request));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).OFFICER_VIEW)")
  public ResponseEntity<OfficerResponseDto> getOfficerById(@PathVariable Long id) {
    return ResponseEntity.ok(officerService.getOfficerById(id));
  }

  @GetMapping
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).OFFICER_VIEW)")
  public ResponseEntity<PageResponse<OfficerResponseDto>> getAllOfficers(
      @PageableDefault(size = 20, sort = "id") Pageable request) {
    return ResponseEntity.ok(officerService.getAllOfficers(request));
  }

  @GetMapping("/stats")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).OFFICER_VIEW)")
  public OfficerStatsResponse getStats() {
    return officerService.getOfficerStats();
  }
}
