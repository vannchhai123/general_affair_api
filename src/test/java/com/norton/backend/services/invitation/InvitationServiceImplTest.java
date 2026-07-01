package com.norton.backend.services.invitation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.norton.backend.dto.request.invitation.CreateInvitationRequest;
import com.norton.backend.enums.OfficerStatus;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvitationServiceImplTest {

  @Mock private InvitationRepository invitationRepository;
  @Mock private OfficerRepository officerRepository;
  @Mock private UploadImageRepository uploadImageRepository;
  @Mock private FileStorageService fileStorageService;
  @Mock private OfficeAccessService officeAccessService;

  @InjectMocks private InvitationServiceImpl invitationService;

  @Test
  void updateInvitation_updatesParticipantsAndImagesInPlace() {
    // Arrange
    Long invitationId = 1L;

    // Existing officers in DB
    OfficerModel officer1 = new OfficerModel();
    officer1.setId(1L);
    officer1.setStatus(OfficerStatus.ACTIVE);
    officer1.setInvitationPriority(true);

    OfficerModel officer2 = new OfficerModel();
    officer2.setId(2L);
    officer2.setStatus(OfficerStatus.ACTIVE);
    officer2.setInvitationPriority(true);

    OfficerModel officer7 = new OfficerModel();
    officer7.setId(7L);
    officer7.setStatus(OfficerStatus.ACTIVE);
    officer7.setInvitationPriority(true);

    // Existing invitation
    InvitationModel invitation = new InvitationModel();
    invitation.setId(invitationId);
    invitation.setTitle("Old Title");
    invitation.setDescription("Old Description");
    invitation.setPresidedBy("Old Presider");
    invitation.setEventDate(LocalDate.of(2026, 7, 1));
    invitation.setEventTime(LocalTime.of(9, 0, 0));
    invitation.setLocation("Old Location");

    // Add initial participants (1 and 2)
    invitation.addParticipant(officer1);
    invitation.addParticipant(officer2);

    // Initial images
    UploadImageModel img1 = new UploadImageModel();
    img1.setId(10L);
    img1.setUrl("http://example.com/img1.jpg");

    UploadImageModel img2 = new UploadImageModel();
    img2.setId(20L);
    img2.setUrl("http://example.com/img2.jpg");

    invitation.addImage(img1);

    // Request to update
    CreateInvitationRequest request = new CreateInvitationRequest();
    request.setTitle("Monthly Team Meeting");
    request.setDescription("Review performance and plan next sprint");
    request.setPresidedBy("Jane Doe");
    request.setEventDate(LocalDate.of(2026, 7, 10));
    request.setEventTime(LocalTime.of(10, 0, 0));
    request.setLocation("Conference Room A");
    request.setParticipantIds(List.of(2L, 7L)); // Removed 1, kept 2, added 7
    request.setImageIds(List.of(20L)); // Removed 10, added 20

    when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));
    when(officerRepository.findAllById(any())).thenReturn(List.of(officer2, officer7));
    when(uploadImageRepository.findAllById(any())).thenReturn(List.of(img2));
    when(invitationRepository.save(any(InvitationModel.class)))
        .thenAnswer(invocationMock -> invocationMock.getArgument(0));

    // Act
    var response = invitationService.updateInvitation(invitationId, request);

    // Assert
    assertNotNull(response);
    assertEquals("Monthly Team Meeting", response.getTitle());
    assertEquals("Review performance and plan next sprint", response.getDescription());
    assertEquals("Jane Doe", response.getPresidedBy());
    assertEquals("2026-07-10", response.getEventDate());
    assertEquals("10:00", response.getEventTime());
    assertEquals("Conference Room A", response.getLocation());

    // Verify participants list was updated in-place:
    // Old participants was [1, 2], new list is [2, 7]
    Set<Long> finalParticipantIds =
        invitation.getParticipants().stream()
            .map(p -> p.getOfficer().getId())
            .collect(Collectors.toSet());
    assertEquals(2, finalParticipantIds.size());
    assertTrue(finalParticipantIds.contains(2L));
    assertTrue(finalParticipantIds.contains(7L));

    // Verify images list was updated in-place:
    // Old images was [10], new is [20]
    Set<Long> finalImageIds =
        invitation.getImages().stream()
            .map(img -> img.getUploadImage().getId())
            .collect(Collectors.toSet());
    assertEquals(1, finalImageIds.size());
    assertTrue(finalImageIds.contains(20L));

    verify(invitationRepository).save(invitation);
  }
}
