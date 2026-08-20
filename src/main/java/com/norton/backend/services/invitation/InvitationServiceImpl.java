package com.norton.backend.services.invitation;

import com.norton.backend.dto.request.invitation.CreateInvitationRequest;
import com.norton.backend.dto.request.invitation.InvitationResponseRequest;
import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import com.norton.backend.dto.responses.invitation.DisplayInvitationResponse;
import com.norton.backend.dto.responses.invitation.InvitationResponseDto;
import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.exceptions.UnauthorizedException;
import com.norton.backend.models.InvitationModel;
import com.norton.backend.models.InvitationParticipantModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.UploadImageModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.InvitationRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.UploadImageRepository;
import com.norton.backend.services.file.FileStorageService;
import com.norton.backend.services.security.OfficeAccessService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

  private final InvitationRepository invitationRepository;
  private final OfficerRepository officerRepository;
  private final UploadImageRepository uploadImageRepository;
  private final FileStorageService fileStorageService;
  private final OfficeAccessService officeAccessService;

  @Override
  @Transactional
  public CreateInvitationResponse createInvitation(
      String title, List<Long> participantIds, MultipartFile image) {
    Long imageId = null;
    String imageUrl = null;
    if (image != null && !image.isEmpty()) {
      imageUrl = fileStorageService.storeImage(image);
    }
    return createInvitation(
        title, null, null, null, null, null, imageId, participantIds, imageUrl, null, null);
  }

  @Override
  @Transactional
  public CreateInvitationResponse createInvitation(
      String title,
      String description,
      String presidedBy,
      LocalDate eventDate,
      LocalTime eventTime,
      String location,
      List<Long> imageIds,
      List<Long> participantIds,
      String type,
      String status) {
    List<String> imageUrls = null;
    List<UploadImageModel> uploadImages = null;
    if (imageIds != null && !imageIds.isEmpty()) {
      Set<Long> uniqueImageIds = new LinkedHashSet<>(imageIds);
      uniqueImageIds.removeIf(id -> id == null);
      if (uniqueImageIds.isEmpty()) {
        throw new BadRequestException("imageIds must contain at least one valid id");
      }

      uploadImages = uploadImageRepository.findAllById(uniqueImageIds);
      Set<Long> foundImageIds =
          uploadImages.stream().map(UploadImageModel::getId).collect(Collectors.toSet());
      if (foundImageIds.size() != uniqueImageIds.size()) {
        Set<Long> missingIds =
            uniqueImageIds.stream()
                .filter(id -> !foundImageIds.contains(id))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        throw new BadRequestException("Upload images not found for ids: " + missingIds);
      }
      imageUrls = uploadImages.stream().map(UploadImageModel::getUrl).collect(Collectors.toList());
    }
    return createInvitation(
        title,
        description,
        presidedBy,
        eventDate,
        eventTime,
        location,
        imageIds,
        participantIds,
        uploadImages,
        imageUrls,
        type,
        status);
  }

  @Override
  public List<CreateInvitationResponse> getInvitations(String type) {
    List<InvitationModel> invitations;
    if (type != null && !type.isBlank()) {
      invitations = invitationRepository.findByTypeIgnoreCase(type.trim());
    } else {
      invitations = invitationRepository.findAll();
    }
    return invitations.stream().map(this::toResponse).collect(Collectors.toList());
  }

  @Override
  public CreateInvitationResponse getInvitationById(Long id) {
    InvitationModel invitation =
        invitationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", id));
    return toResponse(invitation);
  }

  @Override
  @Transactional
  public CreateInvitationResponse updateInvitation(Long id, CreateInvitationRequest request) {
    InvitationModel invitation =
        invitationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", id));

    if (request.getTitle() != null) {
      if (request.getTitle().isBlank()) {
        throw new BadRequestException("title cannot be blank");
      }
      invitation.setTitle(request.getTitle().trim());
    }
    if (request.getDescription() != null) {
      invitation.setDescription(request.getDescription());
    }
    if (request.getPresidedBy() != null) {
      if (request.getPresidedBy().isBlank()) {
        throw new BadRequestException("presidedBy cannot be blank");
      }
      invitation.setPresidedBy(request.getPresidedBy());
    }
    if (request.getEventDate() != null) {
      invitation.setEventDate(request.getEventDate());
    }
    if (request.getEventTime() != null) {
      invitation.setEventTime(request.getEventTime());
    }
    if (request.getLocation() != null) {
      if (request.getLocation().isBlank()) {
        throw new BadRequestException("location cannot be blank");
      }
      invitation.setLocation(request.getLocation());
    }
    if (request.getType() != null) {
      invitation.setType(request.getType());
    }
    if (request.getStatus() != null) {
      invitation.setStatus(request.getStatus());
    }

    if (request.getParticipantIds() != null) {
      Set<Long> originalParticipantIds =
          invitation.getParticipants().stream()
              .map(participant -> participant.getOfficer().getId())
              .collect(Collectors.toSet());

      Set<Long> uniqueParticipantIds = new LinkedHashSet<>(request.getParticipantIds());
      uniqueParticipantIds.removeIf(participantId -> participantId == null);
      if (uniqueParticipantIds.isEmpty()) {
        throw new BadRequestException("participant_ids must contain at least one valid id");
      }

      List<OfficerModel> officers = officerRepository.findAllById(uniqueParticipantIds);
      Set<Long> foundParticipantIds =
          officers.stream().map(OfficerModel::getId).collect(Collectors.toSet());
      if (foundParticipantIds.size() != uniqueParticipantIds.size()) {
        Set<Long> missingIds =
            uniqueParticipantIds.stream()
                .filter(participantId -> !foundParticipantIds.contains(participantId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        throw new BadRequestException("Officers not found for ids: " + missingIds);
      }

      for (OfficerModel officer : officers) {
        if (!originalParticipantIds.contains(officer.getId())) {
          if (officer.getStatus() != OfficerStatus.ACTIVE || !officer.isInvitationPriority()) {
            throw new BadRequestException(
                "Officer with id " + officer.getId() + " is not eligible for invitations");
          }
        }
      }

      Set<Long> newParticipantIds =
          officers.stream().map(OfficerModel::getId).collect(Collectors.toSet());

      invitation
          .getParticipants()
          .removeIf(participant -> !newParticipantIds.contains(participant.getOfficer().getId()));

      Set<Long> existingParticipantIds =
          invitation.getParticipants().stream()
              .map(participant -> participant.getOfficer().getId())
              .collect(Collectors.toSet());

      officers.stream()
          .filter(officer -> !existingParticipantIds.contains(officer.getId()))
          .forEach(invitation::addParticipant);
    }

    if (request.getImageIds() != null) {
      List<UploadImageModel> uploadImages = null;
      List<String> imageUrls = null;
      if (!request.getImageIds().isEmpty()) {
        Set<Long> uniqueImageIds = new LinkedHashSet<>(request.getImageIds());
        uniqueImageIds.removeIf(imageId -> imageId == null);
        if (!uniqueImageIds.isEmpty()) {
          uploadImages = uploadImageRepository.findAllById(uniqueImageIds);
          Set<Long> foundImageIds =
              uploadImages.stream().map(UploadImageModel::getId).collect(Collectors.toSet());
          if (foundImageIds.size() != uniqueImageIds.size()) {
            Set<Long> missingIds =
                uniqueImageIds.stream()
                    .filter(imageId -> !foundImageIds.contains(imageId))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            throw new BadRequestException("Upload images not found for ids: " + missingIds);
          }
          imageUrls =
              uploadImages.stream().map(UploadImageModel::getUrl).collect(Collectors.toList());
        }
      }

      invitation.setImageId(
          request.getImageIds() != null && !request.getImageIds().isEmpty()
              ? request.getImageIds().get(0)
              : null);
      invitation.setImageUrl(imageUrls != null && !imageUrls.isEmpty() ? imageUrls.get(0) : null);

      Set<Long> newImageIds =
          uploadImages != null
              ? uploadImages.stream().map(UploadImageModel::getId).collect(Collectors.toSet())
              : java.util.Collections.emptySet();

      invitation
          .getImages()
          .removeIf(invImage -> !newImageIds.contains(invImage.getUploadImage().getId()));

      Set<Long> existingImageIds =
          invitation.getImages().stream()
              .map(invImage -> invImage.getUploadImage().getId())
              .collect(Collectors.toSet());

      if (uploadImages != null) {
        uploadImages.stream()
            .filter(image -> !existingImageIds.contains(image.getId()))
            .forEach(invitation::addImage);
      }
    }

    InvitationModel updatedInvitation = invitationRepository.save(invitation);
    return toResponse(updatedInvitation);
  }

  @Override
  @Transactional
  public void deleteInvitation(Long id) {
    InvitationModel invitation =
        invitationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", id));
    invitationRepository.delete(invitation);
  }

  private CreateInvitationResponse toResponse(InvitationModel savedInvitation) {
    List<CreateInvitationResponse.AssignedOfficerDto> assignedOfficers =
        savedInvitation.getParticipants().stream()
            .map(
                participant -> {
                  OfficerModel officer = participant.getOfficer();
                  return CreateInvitationResponse.AssignedOfficerDto.builder()
                      .id(officer.getId())
                      .firstName(officer.getFirstNameEn())
                      .lastName(officer.getLastNameEn())
                      .firstNameKh(officer.getFirstNameKh())
                      .lastNameKh(officer.getLastNameKh())
                      .department(
                          officer.getOffice() != null ? officer.getOffice().getName() : null)
                      .position(
                          officer.getPosition() != null ? officer.getPosition().getName() : null)
                      .officerCode(officer.getOfficerCode())
                      .status(participant.getStatus())
                      .rejectionReason(participant.getRejectionReason())
                      .build();
                })
            .collect(Collectors.toList());

    return CreateInvitationResponse.builder()
        .id(savedInvitation.getId())
        .title(savedInvitation.getTitle())
        .description(savedInvitation.getDescription())
        .presidedBy(savedInvitation.getPresidedBy())
        .eventDate(
            savedInvitation.getEventDate() != null
                ? savedInvitation.getEventDate().toString()
                : null)
        .eventTime(
            savedInvitation.getEventTime() != null
                ? savedInvitation.getEventTime().toString()
                : null)
        .location(savedInvitation.getLocation())
        .imageIds(
            savedInvitation.getImages().stream()
                .map(invImage -> invImage.getUploadImage().getId())
                .collect(Collectors.toList()))
        .imageUrls(
            savedInvitation.getImages().stream()
                .map(invImage -> invImage.getUploadImage().getUrl())
                .collect(Collectors.toList()))
        .participantIds(
            savedInvitation.getParticipants().stream()
                .map(participant -> participant.getOfficer().getId())
                .collect(Collectors.toList()))
        .assignedOfficers(assignedOfficers)
        .type(savedInvitation.getType())
        .status(savedInvitation.getStatus())
        .createdAt(
            savedInvitation.getCreatedAt() != null
                ? savedInvitation.getCreatedAt().toString()
                : null)
        .updatedAt(
            savedInvitation.getUpdatedAt() != null
                ? savedInvitation.getUpdatedAt().toString()
                : null)
        .build();
  }

  @Transactional
  private CreateInvitationResponse createInvitation(
      String title,
      String description,
      String presidedBy,
      LocalDate eventDate,
      LocalTime eventTime,
      String location,
      Long imageId,
      List<Long> participantIds,
      String imageUrl,
      String type,
      String status) {
    return createInvitation(
        title,
        description,
        presidedBy,
        eventDate,
        eventTime,
        location,
        imageId != null ? List.of(imageId) : null,
        participantIds,
        null,
        imageUrl != null ? List.of(imageUrl) : null,
        type,
        status);
  }

  @Transactional
  private CreateInvitationResponse createInvitation(
      String title,
      String description,
      String presidedBy,
      LocalDate eventDate,
      LocalTime eventTime,
      String location,
      List<Long> imageIds,
      List<Long> participantIds,
      List<UploadImageModel> uploadImages,
      List<String> imageUrls,
      String type,
      String status) {
    if (title == null || title.isBlank()) {
      throw new BadRequestException("title is required");
    }

    if (participantIds == null || participantIds.isEmpty()) {
      throw new BadRequestException("participant_ids is required and must not be empty");
    }

    if (presidedBy == null || presidedBy.isBlank()) {
      throw new BadRequestException("presidedBy is required");
    }
    if (eventDate == null) {
      throw new BadRequestException("eventDate is required");
    }
    if (location == null || location.isBlank()) {
      throw new BadRequestException("location is required");
    }

    Set<Long> uniqueIds = new LinkedHashSet<>(participantIds);
    uniqueIds.removeIf(id -> id == null);
    if (uniqueIds.isEmpty()) {
      throw new BadRequestException("participant_ids must contain at least one valid id");
    }

    List<OfficerModel> officers = officerRepository.findAllById(uniqueIds);
    Set<Long> foundIds = officers.stream().map(OfficerModel::getId).collect(Collectors.toSet());
    if (foundIds.size() != uniqueIds.size()) {
      Set<Long> missingIds =
          uniqueIds.stream()
              .filter(id -> !foundIds.contains(id))
              .collect(Collectors.toCollection(LinkedHashSet::new));
      throw new BadRequestException("Officers not found for ids: " + missingIds);
    }

    for (OfficerModel officer : officers) {
      if (officer.getStatus() != OfficerStatus.ACTIVE || !officer.isInvitationPriority()) {
        throw new BadRequestException(
            "Officer with id " + officer.getId() + " is not eligible for invitations");
      }
    }

    InvitationModel invitation =
        InvitationModel.builder()
            .title(title.trim())
            .description(description)
            .presidedBy(presidedBy)
            .eventDate(eventDate)
            .eventTime(eventTime)
            .location(location)
            .imageId(imageIds != null && !imageIds.isEmpty() ? imageIds.get(0) : null)
            .imageUrl(imageUrls != null && !imageUrls.isEmpty() ? imageUrls.get(0) : null)
            .type(type != null && !type.isBlank() ? type.trim() : "incoming")
            .status(status != null && !status.isBlank() ? status.trim() : "pending")
            .build();
    officers.forEach(invitation::addParticipant);
    if (uploadImages != null) {
      uploadImages.forEach(invitation::addImage);
    }

    InvitationModel savedInvitation = invitationRepository.save(invitation);
    return toResponse(savedInvitation);
  }

  @Override
  @Transactional
  public InvitationResponseDto respondToInvitation(Long id, InvitationResponseRequest request) {
    InvitationModel invitation =
        invitationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", id));

    UserModel currentUser = officeAccessService.currentUser();
    String currentRole = currentUser.getRole().getRoleName();

    OfficerModel respondingOfficer;
    if (request.getOfficerId() != null) {
      respondingOfficer =
          officerRepository
              .findById(request.getOfficerId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Officer", "id", request.getOfficerId()));

      // If the user has ROLE_OFFICER or ROLE_MANAGER, they can only respond for themselves
      if ("ROLE_OFFICER".equals(currentRole) || "ROLE_MANAGER".equals(currentRole)) {
        if (respondingOfficer.getUser() == null
            || !respondingOfficer.getUser().getId().equals(currentUser.getId())) {
          throw new UnauthorizedException("You can only respond on behalf of yourself");
        }
      }
    } else {
      respondingOfficer =
          officerRepository
              .findByUserIdWithPosition(currentUser.getId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Officer", "userId", currentUser.getId()));
    }

    InvitationParticipantModel participant =
        invitation.getParticipants().stream()
            .filter(p -> p.getOfficer().getId().equals(respondingOfficer.getId()))
            .findFirst()
            .orElseThrow(
                () -> new BadRequestException("Officer is not a participant of this invitation"));

    String status = request.getStatus().toUpperCase();
    if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
      throw new BadRequestException("status must be either APPROVED or REJECTED");
    }

    if ("REJECTED".equals(status)) {
      if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
        throw new BadRequestException("rejectionReason is required when status is REJECTED");
      }
      participant.setRejectionReason(request.getRejectionReason());
    } else {
      participant.setRejectionReason(null);
    }
    participant.setStatus(status);

    boolean allResponded =
        invitation.getParticipants().stream()
            .noneMatch(p -> "PENDING".equalsIgnoreCase(p.getStatus()));

    if (allResponded) {
      invitation.setStatus("completed");
    } else {
      invitation.setStatus("pending");
    }

    invitationRepository.save(invitation);

    String successMessage =
        "APPROVED".equals(status)
            ? "Invitation approved successfully"
            : "Invitation rejected successfully";

    return InvitationResponseDto.builder()
        .message(successMessage)
        .invitationId(invitation.getId())
        .officerId(respondingOfficer.getId())
        .status(status)
        .rejectionReason(participant.getRejectionReason())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<DisplayInvitationResponse> getInvitationsByParticipantAndMonth(
      Long participantId, String yearMonth) {

    if (!officerRepository.existsById(participantId)) {
      throw new ResourceNotFoundException("Officer", "id", participantId);
    }

    LocalDate startDate;
    LocalDate endDate;
    try {
      java.time.YearMonth ym = java.time.YearMonth.parse(yearMonth.trim());
      startDate = ym.atDay(1);
      endDate = ym.atEndOfMonth();
    } catch (Exception ex) {
      throw new BadRequestException("Invalid yearMonth format. Expected YYYY-MM (e.g. 2026-02)");
    }

    List<InvitationModel> invitations =
        invitationRepository.findByParticipantIdAndEventDateBetween(
            participantId, startDate, endDate);

    return invitations.stream()
        .map(
            invitation -> {
              InvitationParticipantModel participant =
                  invitation.getParticipants().stream()
                      .filter(p -> p.getOfficer().getId().equals(participantId))
                      .findFirst()
                      .orElse(null);

              DisplayInvitationResponse.ParticipantResponseDto myResponse = null;
              if (participant != null) {
                myResponse =
                    DisplayInvitationResponse.ParticipantResponseDto.builder()
                        .status(participant.getStatus())
                        .rejectionReason(participant.getRejectionReason())
                        .build();
              }

              return DisplayInvitationResponse.builder()
                  .id(invitation.getId())
                  .title(invitation.getTitle())
                  .description(invitation.getDescription())
                  .presidedBy(invitation.getPresidedBy())
                  .eventDate(
                      invitation.getEventDate() != null
                          ? invitation.getEventDate().toString()
                          : null)
                  .eventTime(
                      invitation.getEventTime() != null
                          ? invitation.getEventTime().toString()
                          : null)
                  .location(invitation.getLocation())
                  .imageUrls(
                      invitation.getImages().stream()
                          .map(img -> img.getUploadImage().getUrl())
                          .collect(Collectors.toList()))
                  .type(invitation.getType())
                  .status(invitation.getStatus())
                  .myResponse(myResponse)
                  .createdAt(
                      invitation.getCreatedAt() != null
                          ? invitation.getCreatedAt().toString()
                          : null)
                  .updatedAt(
                      invitation.getUpdatedAt() != null
                          ? invitation.getUpdatedAt().toString()
                          : null)
                  .build();
            })
        .collect(Collectors.toList());
  }
}
