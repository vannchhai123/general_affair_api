package com.norton.backend.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.norton.backend.config.FileStorageProperties;
import com.norton.backend.controllers.upload.FileServeController;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class FileServeControllerTest {

  @TempDir Path tempDir;

  private FileServeController controller;
  private FileStorageProperties properties;

  @BeforeEach
  void setUp() {
    properties = new FileStorageProperties();
    properties.setUploadDir(tempDir.toString());
    properties.setBaseUrl("http://localhost:8080/uploads");
    controller = new FileServeController(properties);
  }

  @Test
  void testServePdfFileSuccessfully() throws IOException {
    String filename = "1787445082657-sample.pdf";
    Path pdfFile = tempDir.resolve(filename);
    Files.write(pdfFile, "%PDF-1.4 sample content".getBytes());

    ResponseEntity<Resource> response = controller.serveFile(filename);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
    assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("inline"));
    assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains(filename));
    assertNotNull(response.getBody());
    assertTrue(response.getBody().exists());
  }

  @Test
  void testServeImageFileSuccessfully() throws IOException {
    String filename = "sample-image.jpg";
    Path imageFile = tempDir.resolve(filename);
    Files.write(imageFile, new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

    ResponseEntity<Resource> response = controller.serveFile(filename);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
    assertNotNull(response.getBody());
  }

  @Test
  void testServeNonExistentFileReturns404() {
    ResponseEntity<Resource> response = controller.serveFile("non-existent-file.pdf");

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  void testDirectoryTraversalReturnsBadRequest() {
    ResponseEntity<Resource> response = controller.serveFile("../secret.txt");

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}
