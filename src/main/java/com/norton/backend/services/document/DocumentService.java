package com.norton.backend.services.document;

import com.norton.backend.dto.request.document.CreateDocumentRequest;
import com.norton.backend.dto.responses.document.DocumentDetailsResponse;

public interface DocumentService {
  DocumentDetailsResponse createDocument(CreateDocumentRequest request, String currentUsername);

  DocumentDetailsResponse getDocumentDetails(Long id);

  java.util.List<DocumentDetailsResponse.DocTypeDto> getDocumentTypes();

  java.util.List<DocumentDetailsResponse> getAllDocuments(String status);

  void deleteDocument(Long id);

  DocumentDetailsResponse updateDocument(
      Long id, CreateDocumentRequest request, String currentUsername);

  DocumentDetailsResponse updateStatus(Long id, String status, String currentUsername);
}
