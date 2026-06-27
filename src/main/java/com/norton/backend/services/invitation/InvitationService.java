package com.norton.backend.services.invitation;

import com.norton.backend.dto.request.invitation.CreateInvitationRequest;
import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface InvitationService {

  CreateInvitationResponse createInvitation(
      String title, List<Long> participantIds, MultipartFile image);

  CreateInvitationResponse createInvitation(
      String title,
      String description,
      String presidedBy,
      LocalDate eventDate,
      LocalTime eventTime,
      String location,
      List<Long> imageIds,
      List<Long> participantIds);

  List<CreateInvitationResponse> getInvitations();

  CreateInvitationResponse getInvitationById(Long id);

  CreateInvitationResponse updateInvitation(Long id, CreateInvitationRequest request);

  void deleteInvitation(Long id);
}
