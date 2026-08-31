package com.norton.backend.controllers.superadmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.norton.backend.dto.request.superadmin.CreateUserInvitationRequest;
import com.norton.backend.dto.responses.superadmin.UserInvitationResponse;
import com.norton.backend.models.UserModel;
import com.norton.backend.services.superadmin.SuperAdminInvitationService;
import com.norton.backend.utils.SecurityUtils;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SuperAdminInvitationControllerTest {

  @Mock private SuperAdminInvitationService invitationService;
  @Mock private SecurityUtils securityUtils;

  @InjectMocks private SuperAdminInvitationController controller;

  @Test
  void testGetAllInvitations_Success() {
    UserInvitationResponse responseDto =
        UserInvitationResponse.builder()
            .id(1L)
            .email("mean.sokha@domain.gov.kh")
            .fullName("Sokha Mean")
            .khmerName("មាន សុខា")
            .roleName("Provincial Governor")
            .status("PENDING")
            .expiresAt(Instant.parse("2026-09-07T08:00:00Z"))
            .build();

    when(invitationService.getAllInvitations()).thenReturn(List.of(responseDto));

    ResponseEntity<List<UserInvitationResponse>> response = controller.getAllInvitations();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    assertEquals("mean.sokha@domain.gov.kh", response.getBody().get(0).getEmail());
    assertEquals("Sokha Mean", response.getBody().get(0).getFullName());
    assertEquals("មាន សុខា", response.getBody().get(0).getKhmerName());
    assertEquals("Provincial Governor", response.getBody().get(0).getRoleName());
    assertEquals("PENDING", response.getBody().get(0).getStatus());
  }

  @Test
  void testCreateInvitation_Success() {
    CreateUserInvitationRequest request =
        CreateUserInvitationRequest.builder()
            .email("mean.sokha@domain.gov.kh")
            .fullName("Sokha Mean")
            .khmerName("មាន សុខា")
            .roleId(2L)
            .build();

    UserInvitationResponse responseDto =
        UserInvitationResponse.builder()
            .id(1L)
            .email("mean.sokha@domain.gov.kh")
            .fullName("Sokha Mean")
            .khmerName("មាន សុខា")
            .roleName("Provincial Governor")
            .status("PENDING")
            .expiresAt(Instant.parse("2026-09-07T08:00:00Z"))
            .build();

    UserModel mockUser = UserModel.builder().username("superadmin").build();
    when(securityUtils.getCurrentUser()).thenReturn(mockUser);
    when(invitationService.createInvitation(any(CreateUserInvitationRequest.class), eq(mockUser)))
        .thenReturn(responseDto);

    ResponseEntity<UserInvitationResponse> response = controller.createInvitation(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("mean.sokha@domain.gov.kh", response.getBody().getEmail());
    assertEquals("PENDING", response.getBody().getStatus());
  }
}
