package com.norton.backend.services.invitation;

import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import com.norton.backend.enums.OfficerStatus;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.models.InvitationModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.UploadImageModel;
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
    return createInvitation(title, null, null, null, null, null, imageId, participantIds, imageUrl);
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
      Long imageId,
      List<Long> participantIds) {
    String imageUrl = null;
    UploadImageModel uploadImage = null;
    if (imageId != null) {
      uploadImage =
          uploadImageRepository
              .findById(imageId)
              .orElseThrow(
                  () -> new BadRequestException("Upload image not found for id: " + imageId));
      imageUrl = uploadImage.getUrl();
    }
    return createInvitation(
        title,
        description,
        presidedBy,
        eventDate,
        eventTime,
        location,
        imageId,
        participantIds,
        imageUrl);
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
      String imageUrl) {
    if (title == null || title.isBlank()) {
      throw new BadRequestException("title is required");
    }

    if (participantIds == null || participantIds.isEmpty()) {
      throw new BadRequestException("participant_ids is required and must not be empty");
    }

    if (description == null || description.isBlank()) {
      throw new BadRequestException("description is required");
    }
    if (presidedBy == null || presidedBy.isBlank()) {
      throw new BadRequestException("presidedBy is required");
    }
    if (eventDate == null) {
      throw new BadRequestException("eventDate is required");
    }
    if (eventTime == null) {
      throw new BadRequestException("eventTime is required");
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
            .imageId(imageId)
            .imageUrl(imageUrl)
            .build();
    officers.forEach(invitation::addParticipant);

    InvitationModel savedInvitation = invitationRepository.save(invitation);

    List<Long> savedParticipantIds =
        savedInvitation.getParticipants().stream()
            .map(participant -> participant.getOfficer().getId())
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
        .imageId(savedInvitation.getImageId())
        .imageUrl(savedInvitation.getImageUrl())
        .participantIds(savedParticipantIds)
        .build();
  }
}
