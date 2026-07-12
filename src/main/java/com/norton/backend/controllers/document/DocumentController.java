package com.norton.backend.controllers.document;

import com.norton.backend.dto.request.document.CreateDocumentRequest;
import com.norton.backend.dto.responses.document.DocumentDetailsResponse;
import com.norton.backend.services.document.DocumentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DocumentController.BASE_URL)
@RequiredArgsConstructor
public class DocumentController {

  public static final String BASE_URL = "/api/v1/documents";

  private final DocumentService documentService;

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).DOCUMENT_VIEW)")
  public ResponseEntity<DocumentDetailsResponse> getDocumentDetails(@PathVariable Long id) {
    return ResponseEntity.ok(documentService.getDocumentDetails(id));
  }

  @GetMapping
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).DOCUMENT_VIEW)")
  public ResponseEntity<List<DocumentDetailsResponse>> getAllDocuments(
      @RequestParam(required = false) String status) {
    return ResponseEntity.ok(documentService.getAllDocuments(status));
  }

  @GetMapping("/types")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).DOCUMENT_VIEW)")
  public ResponseEntity<List<DocumentDetailsResponse.DocTypeDto>> getDocumentTypes() {
    return ResponseEntity.ok(documentService.getDocumentTypes());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).DOCUMENT_DELETE)")
  public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
    documentService.deleteDocument(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).DOCUMENT_CREATE)")
  public ResponseEntity<DocumentDetailsResponse> createDocument(
      @Validated @RequestBody CreateDocumentRequest request) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
        .body(documentService.createDocument(request, username));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority(T(com.norton.backend.security.Permissions).DOCUMENT_UPDATE)")
  public ResponseEntity<DocumentDetailsResponse> updateDocument(
      @PathVariable Long id, @Validated @RequestBody CreateDocumentRequest request) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return ResponseEntity.ok(documentService.updateDocument(id, request, username));
  }
}
