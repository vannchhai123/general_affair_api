package com.norton.backend.controllers.upload;

import com.norton.backend.dto.responses.upload.UploadImageDataResponse;
import com.norton.backend.dto.responses.upload.UploadImageResponse;
import com.norton.backend.dto.responses.upload.UploadImagesResponse;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.InvitationImageModel;
import com.norton.backend.models.InvitationModel;
import com.norton.backend.models.UploadImageModel;
import com.norton.backend.repositories.InvitationImageRepository;
import com.norton.backend.repositories.InvitationRepository;
import com.norton.backend.repositories.UploadImageRepository;
import com.norton.backend.services.file.FileStorageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(UploadImageController.BASE_URL)
public class UploadImageController {

  public static final String BASE_URL = "/api/v1/files";

  private final FileStorageService fileStorageService;
  private final UploadImageRepository uploadImageRepository;
  private final InvitationRepository invitationRepository;
  private final InvitationImageRepository invitationImageRepository;

  @PostMapping(value = "/{inviteId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
  public ResponseEntity<UploadImagesResponse> uploadImages(
      @PathVariable Long inviteId,
      @RequestParam(value = "files", required = false) MultipartFile[] files,
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestParam(value = "image", required = false) MultipartFile image) {

    InvitationModel invitation =
        invitationRepository
            .findById(inviteId)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", inviteId));

    List<MultipartFile> uploadFiles = new java.util.ArrayList<>();
    if (files != null) {
      for (MultipartFile multipartFile : files) {
        if (multipartFile != null && !multipartFile.isEmpty()) {
          uploadFiles.add(multipartFile);
        }
      }
    }
    if (file != null && !file.isEmpty()) {
      uploadFiles.add(file);
    }
    if (image != null && !image.isEmpty()) {
      uploadFiles.add(image);
    }

    if (uploadFiles.isEmpty()) {
      throw new IllegalArgumentException(
          "At least one multipart field 'files', 'file', or 'image' is required.");
    }

    List<UploadImageDataResponse> dataList = new java.util.ArrayList<>();
    for (MultipartFile uploadFile : uploadFiles) {
      String imageUrl = fileStorageService.storeImage(uploadFile);
      String fileName =
          uploadFile.getOriginalFilename() != null ? uploadFile.getOriginalFilename() : "image";

      UploadImageModel uploadImage =
          UploadImageModel.builder().fileName(fileName).url(imageUrl).build();
      UploadImageModel saved = uploadImageRepository.save(uploadImage);

      InvitationImageModel invitationImage =
          InvitationImageModel.builder().invitation(invitation).uploadImage(saved).build();
      invitationImageRepository.save(invitationImage);

      dataList.add(
          UploadImageDataResponse.builder()
              .id(saved.getId())
              .fileName(saved.getFileName())
              .url(saved.getUrl())
              .build());
    }

    UploadImagesResponse response =
        UploadImagesResponse.builder()
            .success(true)
            .message("Images uploaded successfully.")
            .data(dataList)
            .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{inviteId}/images/{id}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
  public ResponseEntity<UploadImageResponse> getImageById(
      @PathVariable Long inviteId, @PathVariable Long id) {
    InvitationModel invitation =
        invitationRepository
            .findById(inviteId)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", inviteId));

    UploadImageModel image =
        uploadImageRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("UploadImage", "id", id));

    boolean belongsToInvitation =
        invitationImageRepository.existsByInvitation_IdAndUploadImage_Id(inviteId, image.getId());
    if (!belongsToInvitation) {
      throw new ResourceNotFoundException("UploadImage", "id", id);
    }

    UploadImageDataResponse data =
        UploadImageDataResponse.builder()
            .id(image.getId())
            .fileName(image.getFileName())
            .url(image.getUrl())
            .build();

    UploadImageResponse response =
        UploadImageResponse.builder()
            .success(true)
            .message("Image retrieved successfully.")
            .data(data)
            .build();

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{inviteId}/images/{id}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
  public ResponseEntity<UploadImageResponse> deleteImageById(
      @PathVariable Long inviteId, @PathVariable Long id) {
    invitationRepository
        .findById(inviteId)
        .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", inviteId));

    UploadImageModel image =
        uploadImageRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("UploadImage", "id", id));

    boolean belongsToInvitation =
        invitationImageRepository.existsByInvitation_IdAndUploadImage_Id(inviteId, image.getId());
    if (!belongsToInvitation) {
      throw new ResourceNotFoundException("UploadImage", "id", id);
    }

    fileStorageService.deleteImage(image.getUrl());
    invitationImageRepository.deleteByInvitation_IdAndUploadImage_Id(inviteId, image.getId());
    uploadImageRepository.delete(image);

    UploadImageResponse response =
        UploadImageResponse.builder().success(true).message("Image deleted successfully.").build();

    return ResponseEntity.ok(response);
  }
}
