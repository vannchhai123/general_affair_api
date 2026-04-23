package com.norton.backend.controllers.officer;

import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.services.file.FileStorageService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(OfficerController.BASE_URL)
public class OfficerImageController {

  private final OfficerRepository officerRepository;
  private final FileStorageService fileStorageService;

  @GetMapping("/{id}/image")
  public ResponseEntity<Map<String, String>> getOfficerImage(@PathVariable Long id) {
    OfficerModel officer =
        officerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", id));

    if (officer.getImageUrl() == null || officer.getImageUrl().isBlank()) {
      throw new BadRequestException("Officer image not found");
    }

    return ResponseEntity.ok(
        Map.of(
            "message", "Officer image retrieved successfully", "imageUrl", officer.getImageUrl()));
  }

  @PostMapping(value = "/{id}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, String>> uploadImage(
      @PathVariable Long id, @RequestParam("file") MultipartFile file) {

    OfficerModel officer =
        officerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", id));

    String imageUrl = fileStorageService.storeImage(file);
    officer.setImageUrl(imageUrl);
    officerRepository.save(officer);

    return ResponseEntity.ok(
        Map.of("message", "Image uploaded successfully", "imageUrl", imageUrl));
  }
}
