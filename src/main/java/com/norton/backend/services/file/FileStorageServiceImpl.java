package com.norton.backend.services.file;

import com.norton.backend.config.FileStorageProperties;
import com.norton.backend.exceptions.BadRequestException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

  private static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of("image/jpeg", "image/png", "image/jpg", "image/webp");

  private final FileStorageProperties fileStorageProperties;
  private Path uploadPath;

  @PostConstruct
  public void init() {
    this.uploadPath = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.uploadPath);
    } catch (IOException ex) {
      throw new IllegalStateException("Could not create upload directory", ex);
    }
  }

  @Override
  public String storeImage(MultipartFile file) {
    validateFile(file);

    String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "image");
    String extension = extractExtension(originalName);
    String cleanBaseName = sanitizeFileBaseName(extractBaseName(originalName));
    long timestamp = System.currentTimeMillis();

    String candidateName = buildFileName(timestamp, cleanBaseName, extension, null);
    Path targetPath = uploadPath.resolve(candidateName);
    int suffix = 1;

    while (Files.exists(targetPath)) {
      candidateName = buildFileName(timestamp, cleanBaseName, extension, suffix++);
      targetPath = uploadPath.resolve(candidateName);
    }

    try {
      Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
      return buildPublicUrl(candidateName);
    } catch (IOException ex) {
      throw new BadRequestException("Failed to store uploaded file");
    }
  }

  @Override
  public void deleteImage(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      return;
    }

    String filename = extractFilenameFromUrl(fileUrl);
    if (filename.isBlank()) {
      return;
    }

    Path path = uploadPath.resolve(filename).normalize();
    if (!path.startsWith(uploadPath)) {
      throw new BadRequestException("Invalid image URL");
    }

    try {
      Files.deleteIfExists(path);
    } catch (IOException ex) {
      throw new BadRequestException("Failed to delete image file");
    }
  }

  private String extractFilenameFromUrl(String fileUrl) {
    try {
      URI uri = new URI(fileUrl);
      String path = uri.getPath();
      if (path == null || path.isBlank()) {
        return "";
      }
      return Paths.get(path).getFileName().toString();
    } catch (URISyntaxException ex) {
      // If the URL is not a valid URI, fall back to taking the last segment.
      String[] segments = fileUrl.split("/");
      return segments.length == 0 ? "" : segments[segments.length - 1];
    }
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("File must not be empty");
    }

    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
      throw new BadRequestException("Only jpeg, jpg, png, and webp images are allowed");
    }
  }

  private String extractExtension(String filename) {
    String cleanName = Paths.get(filename).getFileName().toString();
    int dotIndex = cleanName.lastIndexOf('.');
    if (dotIndex <= 0 || dotIndex == cleanName.length() - 1) {
      throw new BadRequestException("File extension is required");
    }
    return cleanName.substring(dotIndex + 1).toLowerCase();
  }

  private String extractBaseName(String filename) {
    String cleanName = Paths.get(filename).getFileName().toString();
    int dotIndex = cleanName.lastIndexOf('.');
    return dotIndex > 0 ? cleanName.substring(0, dotIndex) : cleanName;
  }

  private String sanitizeFileBaseName(String baseName) {
    String normalized = Normalizer.normalize(baseName, Normalizer.Form.NFD);
    String ascii = normalized.replaceAll("\\p{M}", "");
    String sanitized = ascii.replaceAll("[^a-zA-Z0-9-_]", "-");
    sanitized = sanitized.replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
    return sanitized.isBlank() ? "image" : sanitized;
  }

  private String buildFileName(Long timestamp, String baseName, String extension, Integer suffix) {
    if (suffix == null) {
      return timestamp + "-" + baseName + "." + extension;
    }
    return timestamp + "-" + baseName + "-" + suffix + "." + extension;
  }

  private String buildPublicUrl(String filename) {
    String baseUrl = fileStorageProperties.getBaseUrl();
    if (baseUrl.endsWith("/")) {
      return baseUrl + filename;
    }
    return baseUrl + "/" + filename;
  }
}
