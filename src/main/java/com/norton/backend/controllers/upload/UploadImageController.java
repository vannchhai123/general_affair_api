package com.norton.backend.controllers.upload;

import com.norton.backend.dto.responses.upload.UploadImageDataResponse;
import com.norton.backend.dto.responses.upload.UploadImageResponse;
import com.norton.backend.models.UploadImageModel;
import com.norton.backend.repositories.UploadImageRepository;
import com.norton.backend.services.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(UploadImageController.BASE_URL)
public class UploadImageController {

  public static final String BASE_URL = "/api/v1/uploads/images";

  private final FileStorageService fileStorageService;
  private final UploadImageRepository uploadImageRepository;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
  public ResponseEntity<UploadImageResponse> uploadImage(
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestParam(value = "image", required = false) MultipartFile image) {

    MultipartFile uploadFile = file != null ? file : image;
    if (uploadFile == null || uploadFile.isEmpty()) {
      throw new IllegalArgumentException(
          "Required multipart field 'file' or 'image' is not present.");
    }

    String imageUrl = fileStorageService.storeImage(uploadFile);
    String fileName =
        uploadFile.getOriginalFilename() != null ? uploadFile.getOriginalFilename() : "image";

    UploadImageModel uploadImage =
        UploadImageModel.builder().fileName(fileName).url(imageUrl).build();
    UploadImageModel saved = uploadImageRepository.save(uploadImage);

    UploadImageDataResponse data =
        UploadImageDataResponse.builder()
            .id(saved.getId())
            .fileName(saved.getFileName())
            .url(saved.getUrl())
            .build();

    UploadImageResponse response =
        UploadImageResponse.builder()
            .success(true)
            .message("Image uploaded successfully.")
            .data(data)
            .build();

    return ResponseEntity.ok(response);
  }
}
