package com.norton.backend.services.superadmin;

import com.norton.backend.dto.request.superadmin.CreateUserInvitationRequest;
import com.norton.backend.dto.responses.superadmin.UserInvitationResponse;
import com.norton.backend.models.UserModel;
import java.util.List;

public interface SuperAdminInvitationService {

  List<UserInvitationResponse> getAllInvitations();

  UserInvitationResponse createInvitation(
      CreateUserInvitationRequest request, UserModel currentUser);
}
