package com.norton.backend.controllers.invitation;

import com.norton.backend.dto.request.invitation.CreateInvitationRequest;
import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import com.norton.backend.dto.responses.invitation.EligibleParticipantsResponse;
import com.norton.backend.dto.responses.officers.OfficerResponse;
import com.norton.backend.services.invitation.InvitationService;
import com.norton.backend.services.officer.OfficerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(InvitationController.BASE_URL)
public class InvitationController {

  public static final String BASE_URL = "/api/v1/invitations";

  private final OfficerService officerService;
  private final InvitationService invitationService;

  @GetMapping("/eligible-participants")
  @PreAuthorize(
      "(hasRole('SUPER_ADMIN') or hasRole('ADMIN')) and hasAuthority(T(com.norton.backend.security.Permissions).INVITATION_PARTICIPANT_VIEW)")
  public ResponseEntity<EligibleParticipantsResponse> getEligibleParticipants(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer limit) {
    List<OfficerResponse> participants =
        officerService.getEligibleInvitationParticipants(keyword, limit);
    List<Long> participantIds =
        participants.stream()
            .map(OfficerResponse::getId)
            .filter(java.util.Objects::nonNull)
            .toList();

    return ResponseEntity.ok(
        EligibleParticipantsResponse.builder()
            .participantIds(participantIds)
            .participants(participants)
            .build());
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize(
      "(hasRole('SUPER_ADMIN') or hasRole('ADMIN')) and hasAuthority(T(com.norton.backend.security.Permissions).INVITATION_PARTICIPANT_VIEW)")
  public ResponseEntity<CreateInvitationResponse> createInvitationMultipart(
      @RequestParam("title") String title,
      @RequestParam("participant_ids") List<Long> participantIds,
      @RequestParam(value = "image", required = false) MultipartFile image) {
    CreateInvitationResponse response =
        invitationService.createInvitation(title, participantIds, image);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize(
      "(hasRole('SUPER_ADMIN') or hasRole('ADMIN')) and hasAuthority(T(com.norton.backend.security.Permissions).INVITATION_PARTICIPANT_VIEW)")
  public ResponseEntity<CreateInvitationResponse> createInvitationJson(
      @Validated @RequestBody CreateInvitationRequest request) {
    CreateInvitationResponse response =
        invitationService.createInvitation(
            request.getTitle(),
            request.getDescription(),
            request.getPresidedBy(),
            request.getEventDate(),
            request.getEventTime(),
            request.getLocation(),
            request.getImageIds(),
            request.getParticipantIds());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
