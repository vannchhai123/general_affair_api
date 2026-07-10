package com.norton.backend.controllers.officer;

import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.services.file.FileStorageService;
import com.norton.backend.services.security.OfficeAccessService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
  private final OfficeAccessService officeAccessService;
  private final UserRepository userRepository;

  @GetMapping("/{id}/image")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).OFFICER_VIEW)")
  public ResponseEntity<Map<String, String>> getOfficerImage(@PathVariable Long id) {
    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(id)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", id));
    officeAccessService.assertCanAccessOfficer(officer);

    if (officer.getImageUrl() == null || officer.getImageUrl().isBlank()) {
      throw new BadRequestException("Officer image not found");
    }

    return ResponseEntity.ok(
        Map.of(
            "message", "Officer image retrieved successfully", "imageUrl", officer.getImageUrl()));
  }

  @PostMapping(value = "/{id}/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).OFFICER_UPDATE)")
  public ResponseEntity<Map<String, String>> uploadImage(
      @PathVariable Long id, @RequestParam("file") MultipartFile file) {

    OfficerModel officer =
        officerRepository
            .findByIdWithPosition(id)
            .orElseThrow(() -> new ResourceNotFoundException("Officer", "id", id));
    officeAccessService.assertCanAccessOfficer(officer);

    String imageUrl = fileStorageService.storeImage(file);
    officer.setImageUrl(imageUrl);
    officerRepository.save(officer);

    return ResponseEntity.ok(
        Map.of("message", "Image uploaded successfully", "imageUrl", imageUrl));
  }

  @PostMapping(value = "/me/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, String>> uploadMyImage(
      @RequestParam("file") MultipartFile file) {

    String username =
        org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    UserModel currentUser =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

    OfficerModel officer = currentUser.getOfficer();
    if (officer == null) {
      throw new BadRequestException("Current user is not associated with an officer profile");
    }

    String imageUrl = fileStorageService.storeImage(file);
    officer.setImageUrl(imageUrl);
    officerRepository.save(officer);

    return ResponseEntity.ok(
        Map.of("message", "Profile image uploaded successfully", "imageUrl", imageUrl));
  }
}
