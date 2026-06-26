package com.norton.backend.controllers.invitation;

import com.norton.backend.dto.responses.officers.OfficerResponse;
import com.norton.backend.services.officer.OfficerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(InvitationController.BASE_URL)
public class InvitationController {

  public static final String BASE_URL = "/api/v1/invitations";

  private final OfficerService officerService;

  @GetMapping("/eligible-participants")
  @PreAuthorize(
      "(hasRole('SUPER_ADMIN') or hasRole('ADMIN')) and hasAuthority(T(com.norton.backend.security.Permissions).INVITATION_PARTICIPANT_VIEW)")
  public ResponseEntity<List<OfficerResponse>> getEligibleParticipants(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer limit) {
    return ResponseEntity.ok(officerService.getEligibleInvitationParticipants(keyword, limit));
  }
}
