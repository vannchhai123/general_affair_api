package com.norton.backend.controllers.document;

import com.norton.backend.dto.request.document.CreateInternalDocRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.document.InternalDocDetailsResponse;
import com.norton.backend.dto.responses.document.InternalDocResponse;
import com.norton.backend.services.document.InternalDocService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(InternalDocController.BASE_URL)
@RequiredArgsConstructor
public class InternalDocController {

  public static final String BASE_URL = "/api/v1/internalDoc";

  private final InternalDocService internalDocService;

  @GetMapping("/getDocType")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('HEAD_OFFICE') or hasRole('MANAGER') or hasRole('OFFICER')")
  public ResponseEntity<List<String>> getDocType() {
    return ResponseEntity.ok(internalDocService.getDocTypeNames());
  }

  @GetMapping("/DocType/get")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('HEAD_OFFICE') or hasRole('MANAGER') or hasRole('OFFICER')")
  public ResponseEntity<PageResponse<InternalDocResponse>> getDocTypes(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(internalDocService.getInternalDocuments(page, size));
  }

  @GetMapping("/search")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('HEAD_OFFICE') or hasRole('MANAGER') or hasRole('OFFICER')")
  public ResponseEntity<PageResponse<InternalDocResponse>> searchInternalDocuments(
      @RequestParam String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(internalDocService.searchInternalDocuments(query, page, size));
  }

  @GetMapping("/{docType}/search")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('HEAD_OFFICE') or hasRole('MANAGER') or hasRole('OFFICER')")
  public ResponseEntity<PageResponse<InternalDocResponse>> searchInternalDocumentsByType(
      @PathVariable String docType,
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(
        internalDocService.searchInternalDocumentsByType(docType, query, page, size));
  }

  @GetMapping("/filter/date/{startDate}/{endDate}")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('HEAD_OFFICE') or hasRole('MANAGER') or hasRole('OFFICER')")
  public ResponseEntity<PageResponse<InternalDocResponse>> getDocByDateRange(
      @PathVariable String startDate,
      @PathVariable String endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(
        internalDocService.getInternalDocumentsByDateRange(startDate, endDate, page, size));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('HEAD_OFFICE') or hasRole('MANAGER') or hasRole('OFFICER')")
  public ResponseEntity<InternalDocDetailsResponse> getInternalDocumentDetails(
      @PathVariable Long id) {
    return ResponseEntity.ok(internalDocService.getInternalDocumentDetails(id));
  }

  @PostMapping
  @PreAuthorize(
      "hasRole('ADMIN') or hasRole('HEAD_OFFICE') or hasRole('MANAGER') or hasRole('OFFICER')")
  public ResponseEntity<InternalDocDetailsResponse> createInternalDocument(
      @Validated @RequestBody CreateInternalDocRequest request) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
        .body(internalDocService.createInternalDocument(request, username));
  }
}
