package com.norton.backend.services.invitation;

import com.norton.backend.dto.request.invitation.CreateInvitationRequest;
import com.norton.backend.dto.request.invitation.InvitationResponseRequest;
import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import com.norton.backend.dto.responses.invitation.DisplayInvitationResponse;
import com.norton.backend.dto.responses.invitation.InvitationResponseDto;
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
      List<Long> participantIds,
      String type,
      String status);

  List<CreateInvitationResponse> getInvitations(String type);

  CreateInvitationResponse getInvitationById(Long id);

  CreateInvitationResponse updateInvitation(Long id, CreateInvitationRequest request);

  void deleteInvitation(Long id);

  InvitationResponseDto respondToInvitation(Long id, InvitationResponseRequest request);

  List<DisplayInvitationResponse> getInvitationsByParticipantAndMonth(
      Long participantId, String yearMonth);

  List<DisplayInvitationResponse> getInvitationsByTypeAndParticipantAndMonth(
      String type, Long participantId, String yearMonth);
}
