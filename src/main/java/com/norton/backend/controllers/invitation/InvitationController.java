package com.norton.backend.controllers.invitation;

import com.norton.backend.dto.request.invitation.CreateInvitationRequest;
import com.norton.backend.dto.request.invitation.InvitationResponseRequest;
import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import com.norton.backend.dto.responses.invitation.DisplayInvitationResponse;
import com.norton.backend.dto.responses.invitation.EligibleParticipantsResponse;
import com.norton.backend.dto.responses.invitation.InvitationResponseDto;
import com.norton.backend.dto.responses.officers.OfficerResponse;
import com.norton.backend.services.invitation.InvitationService;
import com.norton.backend.services.officer.OfficerService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
      "hasRole('ADMIN') or hasRole('HEAD_OFFICE') or hasRole('MANAGER') or hasRole('OFFICER')")
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
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).INVITATION_CREATE)")
  public ResponseEntity<CreateInvitationResponse> createInvitationMultipart(
      @RequestParam("title") String title,
      @RequestParam("participant_ids") List<Long> participantIds,
      @RequestParam(value = "image", required = false) MultipartFile image) {
    CreateInvitationResponse response =
        invitationService.createInvitation(title, participantIds, image);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).INVITATION_CREATE)")
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
            request.getParticipantIds(),
            request.getType(),
            request.getStatus());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).INVITATION_VIEW)")
  public ResponseEntity<List<CreateInvitationResponse>> getInvitations() {
    return ResponseEntity.ok(invitationService.getInvitations());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).INVITATION_VIEW)")
  public ResponseEntity<CreateInvitationResponse> getInvitationById(@PathVariable Long id) {
    return ResponseEntity.ok(invitationService.getInvitationById(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).INVITATION_UPDATE)")
  public ResponseEntity<CreateInvitationResponse> updateInvitation(
      @PathVariable Long id, @Validated @RequestBody CreateInvitationRequest request) {
    return ResponseEntity.ok(invitationService.updateInvitation(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).INVITATION_DELETE)")
  public ResponseEntity<Map<String, Object>> deleteInvitation(@PathVariable Long id) {
    invitationService.deleteInvitation(id);
    return ResponseEntity.ok(Map.of("success", true, "message", "Invitation deleted successfully"));
  }

  @PostMapping("/{id}/respond")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('HEAD_OFFICE') or hasRole('MANAGER') or hasRole('OFFICER')")
  public ResponseEntity<InvitationResponseDto> respondToInvitation(
      @PathVariable Long id, @Validated @RequestBody InvitationResponseRequest request) {
    return ResponseEntity.ok(invitationService.respondToInvitation(id, request));
  }

  @GetMapping("/display/{participantId}/{yearMonth}")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('HEAD_OFFICE') or hasRole('MANAGER') or hasRole('OFFICER')")
  public ResponseEntity<List<DisplayInvitationResponse>> getInvitationsByParticipantAndMonth(
      @PathVariable Long participantId, @PathVariable String yearMonth) {
    return ResponseEntity.ok(
        invitationService.getInvitationsByParticipantAndMonth(participantId, yearMonth));
  }
}
