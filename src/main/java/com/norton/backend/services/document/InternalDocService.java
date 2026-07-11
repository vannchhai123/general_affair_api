package com.norton.backend.services.document;

import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.document.InternalDocResponse;
import java.util.List;

public interface InternalDocService {
  List<String> getDocTypeNames();

  PageResponse<InternalDocResponse> getInternalDocuments(int page, int size);

  PageResponse<InternalDocResponse> searchInternalDocuments(String query, int page, int size);

  PageResponse<InternalDocResponse> getInternalDocumentsByDateRange(
      String startDate, String endDate, int page, int size);

  com.norton.backend.dto.responses.document.InternalDocDetailsResponse getInternalDocumentDetails(
      Long id);
}
