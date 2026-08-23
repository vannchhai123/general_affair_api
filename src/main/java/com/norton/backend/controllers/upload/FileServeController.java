package com.norton.backend.controllers.upload;

import com.norton.backend.config.FileStorageProperties;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileServeController {

  private final FileStorageProperties fileStorageProperties;

  @GetMapping("/uploads/{filename:.+}")
  public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
    try {
      Path uploadDir = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
      Path filePath = uploadDir.resolve(filename).normalize();

      // Security check against directory traversal
      if (!filePath.startsWith(uploadDir)) {
        return ResponseEntity.badRequest().build();
      }

      Resource resource = new UrlResource(filePath.toUri());
      if (!resource.exists() || !resource.isReadable()) {
        log.warn("File not found on disk at: {}", filePath);
        return ResponseEntity.notFound().build();
      }

      MediaType mediaType =
          MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);

      return ResponseEntity.ok()
          .contentType(mediaType)
          .header(
              HttpHeaders.CONTENT_DISPOSITION,
              "inline; filename=\"" + resource.getFilename() + "\"")
          .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
          .body(resource);
    } catch (IOException e) {
      log.error("Error serving file: {}", filename, e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
