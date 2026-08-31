package com.norton.backend.services.superadmin;

import com.norton.backend.dto.request.superadmin.CreateUserInvitationRequest;
import com.norton.backend.dto.responses.superadmin.UserInvitationResponse;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.UserInvitationModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.models.UserRoleModel;
import com.norton.backend.repositories.UserInvitationRepository;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.repositories.UserRoleRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SuperAdminInvitationServiceImpl implements SuperAdminInvitationService {

  private final UserInvitationRepository invitationRepository;
  private final UserRoleRepository roleRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public List<UserInvitationResponse> getAllInvitations() {
    return invitationRepository.findAllByOrderByCreatedAtDesc().stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  public UserInvitationResponse createInvitation(
      CreateUserInvitationRequest request, UserModel currentUser) {
    if (userRepository.existsByEmail(request.getEmail().trim())) {
      throw new BadRequestException("A user with this email already exists");
    }

    UserRoleModel role =
        roleRepository
            .findById(request.getRoleId())
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

    UserInvitationModel invitation =
        UserInvitationModel.builder()
            .email(request.getEmail().trim().toLowerCase())
            .fullName(request.getFullName().trim())
            .khmerName(request.getKhmerName() != null ? request.getKhmerName().trim() : null)
            .role(role)
            .status("PENDING")
            .token(UUID.randomUUID().toString())
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .invitedBy(currentUser)
            .build();

    UserInvitationModel saved = invitationRepository.save(invitation);
    log.info("Super Admin invited user email={}, role={}", saved.getEmail(), role.getNameEn());
    return toResponse(saved);
  }

  private UserInvitationResponse toResponse(UserInvitationModel invitation) {
    String roleName = null;
    if (invitation.getRole() != null) {
      roleName =
          invitation.getRole().getNameEn() != null
              ? invitation.getRole().getNameEn()
              : invitation.getRole().getRoleName();
    }

    return UserInvitationResponse.builder()
        .id(invitation.getId())
        .email(invitation.getEmail())
        .fullName(invitation.getFullName())
        .khmerName(invitation.getKhmerName())
        .roleName(roleName)
        .status(invitation.getStatus())
        .expiresAt(invitation.getExpiresAt())
        .build();
  }
}
