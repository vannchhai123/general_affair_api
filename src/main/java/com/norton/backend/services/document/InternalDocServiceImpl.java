package com.norton.backend.services.document;

import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.document.InternalDocDetailsResponse;
import com.norton.backend.dto.responses.document.InternalDocResponse;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.DocumentModel;
import com.norton.backend.models.DocumentTypeModel;
import com.norton.backend.repositories.DocumentRepository;
import com.norton.backend.repositories.DocumentTypeRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InternalDocServiceImpl implements InternalDocService {

  private final DocumentTypeRepository documentTypeRepository;
  private final DocumentRepository documentRepository;

  @Override
  @Transactional(readOnly = true)
  public List<String> getDocTypeNames() {
    return documentTypeRepository.findAll().stream()
        .map(DocumentTypeModel::getName)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<InternalDocResponse> getInternalDocuments(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
    Page<DocumentModel> documentPage = documentRepository.findByDirection("INTERNAL", pageable);

    List<InternalDocResponse> content =
        documentPage.getContent().stream()
            .map(
                doc ->
                    InternalDocResponse.builder()
                        .id(doc.getId())
                        .title(doc.getSubject())
                        .documentNumber(doc.getDocumentNumber())
                        .description(doc.getSummary())
                        .documentDate(
                            doc.getDocumentDate() != null ? doc.getDocumentDate().toString() : null)
                        .type(
                            doc.getDocumentType() != null ? doc.getDocumentType().getName() : null)
                        .build())
            .collect(Collectors.toList());

    return PageResponse.<InternalDocResponse>builder()
        .content(content)
        .page(documentPage.getNumber())
        .size(documentPage.getSize())
        .totalElements(documentPage.getTotalElements())
        .totalPages(documentPage.getTotalPages())
        .last(documentPage.isLast())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<InternalDocResponse> searchInternalDocuments(
      String query, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
    String searchTerm = query != null ? query.trim() : "";
    Page<DocumentModel> documentPage = documentRepository.searchInternalDocs(searchTerm, pageable);

    List<InternalDocResponse> content =
        documentPage.getContent().stream()
            .map(
                doc ->
                    InternalDocResponse.builder()
                        .id(doc.getId())
                        .title(doc.getSubject())
                        .documentNumber(doc.getDocumentNumber())
                        .description(doc.getSummary())
                        .documentDate(
                            doc.getDocumentDate() != null ? doc.getDocumentDate().toString() : null)
                        .type(
                            doc.getDocumentType() != null ? doc.getDocumentType().getName() : null)
                        .build())
            .collect(Collectors.toList());

    return PageResponse.<InternalDocResponse>builder()
        .content(content)
        .page(documentPage.getNumber())
        .size(documentPage.getSize())
        .totalElements(documentPage.getTotalElements())
        .totalPages(documentPage.getTotalPages())
        .last(documentPage.isLast())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<InternalDocResponse> getInternalDocumentsByDateRange(
      String startDate, String endDate, int page, int size) {

    java.time.LocalDate start;
    java.time.LocalDate end;
    try {
      start = java.time.LocalDate.parse(startDate.trim());
      end = java.time.LocalDate.parse(endDate.trim());
    } catch (Exception ex) {
      throw new com.norton.backend.exceptions.BadRequestException(
          "Invalid date format. Expected YYYY-MM-DD");
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
    Page<DocumentModel> documentPage =
        documentRepository.findInternalDocsByDateRange(start, end, pageable);

    List<InternalDocResponse> content =
        documentPage.getContent().stream()
            .map(
                doc ->
                    InternalDocResponse.builder()
                        .id(doc.getId())
                        .title(doc.getSubject())
                        .documentNumber(doc.getDocumentNumber())
                        .description(doc.getSummary())
                        .documentDate(
                            doc.getDocumentDate() != null ? doc.getDocumentDate().toString() : null)
                        .type(
                            doc.getDocumentType() != null ? doc.getDocumentType().getName() : null)
                        .build())
            .collect(Collectors.toList());

    return PageResponse.<InternalDocResponse>builder()
        .content(content)
        .page(documentPage.getNumber())
        .size(documentPage.getSize())
        .totalElements(documentPage.getTotalElements())
        .totalPages(documentPage.getTotalPages())
        .last(documentPage.isLast())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public InternalDocDetailsResponse getInternalDocumentDetails(Long id) {
    DocumentModel doc =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

    if (!"INTERNAL".equals(doc.getDirection())) {
      throw new com.norton.backend.exceptions.BadRequestException(
          "Document is not an internal document");
    }

    InternalDocDetailsResponse.DocTypeDto typeDto = null;
    if (doc.getDocumentType() != null) {
      typeDto =
          InternalDocDetailsResponse.DocTypeDto.builder()
              .id(doc.getDocumentType().getId())
              .name(doc.getDocumentType().getName())
              .code(doc.getDocumentType().getCode())
              .build();
    }

    InternalDocDetailsResponse.OrgDto senderDto = null;
    if (doc.getSenderOrganization() != null) {
      senderDto =
          InternalDocDetailsResponse.OrgDto.builder()
              .id(doc.getSenderOrganization().getId())
              .name(doc.getSenderOrganization().getName())
              .shortName(doc.getSenderOrganization().getShortName())
              .build();
    }

    InternalDocDetailsResponse.OrgDto receiverDto = null;
    if (doc.getReceiverOrganization() != null) {
      receiverDto =
          InternalDocDetailsResponse.OrgDto.builder()
              .id(doc.getReceiverOrganization().getId())
              .name(doc.getReceiverOrganization().getName())
              .shortName(doc.getReceiverOrganization().getShortName())
              .build();
    }

    InternalDocDetailsResponse.CreatorDto creatorDto = null;
    if (doc.getCreatedBy() != null) {
      creatorDto =
          InternalDocDetailsResponse.CreatorDto.builder()
              .id(doc.getCreatedBy().getId())
              .officerCode(doc.getCreatedBy().getOfficerCode())
              .firstName(doc.getCreatedBy().getFirstName())
              .lastName(doc.getCreatedBy().getLastName())
              .build();
    }

    List<InternalDocDetailsResponse.FileDto> filesDto =
        doc.getFiles().stream()
            .map(
                file ->
                    InternalDocDetailsResponse.FileDto.builder()
                        .id(file.getId())
                        .fileName(file.getFileName())
                        .mimeType(file.getMimeType())
                        .fileSize(file.getFileSize())
                        .isPrimary(file.getIsPrimary())
                        .fileUrl(
                            file.getUploadImage() != null ? file.getUploadImage().getUrl() : null)
                        .build())
            .collect(Collectors.toList());

    return InternalDocDetailsResponse.builder()
        .id(doc.getId())
        .uuid(doc.getUuid() != null ? doc.getUuid().toString() : null)
        .direction(doc.getDirection())
        .documentNumber(doc.getDocumentNumber())
        .documentDate(doc.getDocumentDate() != null ? doc.getDocumentDate().toString() : null)
        .receivedDate(doc.getReceivedDate() != null ? doc.getReceivedDate().toString() : null)
        .subject(doc.getSubject())
        .summary(doc.getSummary())
        .confidentiality(doc.getConfidentiality())
        .priority(doc.getPriority())
        .status(doc.getStatus())
        .remarks(doc.getRemarks())
        .documentType(typeDto)
        .senderOrganization(senderDto)
        .receiverOrganization(receiverDto)
        .createdBy(creatorDto)
        .files(filesDto)
        .build();
  }
}
