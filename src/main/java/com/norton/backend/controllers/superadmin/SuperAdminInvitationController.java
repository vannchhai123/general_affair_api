package com.norton.backend.controllers.superadmin;

import com.norton.backend.dto.request.superadmin.CreateUserInvitationRequest;
import com.norton.backend.dto.responses.superadmin.UserInvitationResponse;
import com.norton.backend.models.UserModel;
import com.norton.backend.services.superadmin.SuperAdminInvitationService;
import com.norton.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SuperAdminInvitationController.BASE_URL)
public class SuperAdminInvitationController {

  public static final String BASE_URL = "/api/v1/super-admin/invitations";

  private final SuperAdminInvitationService invitationService;
  private final SecurityUtils securityUtils;

  @GetMapping
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<List<UserInvitationResponse>> getAllInvitations() {
    return ResponseEntity.ok(invitationService.getAllInvitations());
  }

  @PostMapping
  @PreAuthorize(
      "hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
  public ResponseEntity<UserInvitationResponse> createInvitation(
      @Valid @RequestBody CreateUserInvitationRequest request) {
    UserModel currentUser = securityUtils.getCurrentUser();
    UserInvitationResponse response = invitationService.createInvitation(request, currentUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
