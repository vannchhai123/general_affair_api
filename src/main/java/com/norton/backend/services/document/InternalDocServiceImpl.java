package com.norton.backend.services.document;

import com.norton.backend.dto.request.document.CreateInternalDocRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.document.InternalDocDetailsResponse;
import com.norton.backend.dto.responses.document.InternalDocResponse;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.DocumentFileModel;
import com.norton.backend.models.DocumentModel;
import com.norton.backend.models.DocumentTypeModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.OrganizationModel;
import com.norton.backend.models.UploadImageModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.DocumentFileRepository;
import com.norton.backend.repositories.DocumentRepository;
import com.norton.backend.repositories.DocumentTypeRepository;
import com.norton.backend.repositories.OrganizationRepository;
import com.norton.backend.repositories.UploadImageRepository;
import com.norton.backend.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final UploadImageRepository uploadImageRepository;
  private final DocumentFileRepository documentFileRepository;

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
  public PageResponse<InternalDocResponse> searchInternalDocumentsByType(
      String docType, String query, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
    String normalizedDocType = docType != null ? docType.trim() : "";
    String searchTerm = query != null ? query.trim() : "";
    Page<DocumentModel> documentPage =
        documentRepository.searchInternalDocsByType(normalizedDocType, searchTerm, pageable);

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

  @Override
  @Transactional
  public InternalDocDetailsResponse createInternalDocument(
      CreateInternalDocRequest request, String currentUsername) {
    UserModel user =
        userRepository
            .findByUsername(currentUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));

    OfficerModel creator = user.getOfficer();
    if (creator == null) {
      throw new com.norton.backend.exceptions.BadRequestException(
          "Authenticated user is not associated with an officer profile");
    }

    DocumentTypeModel docType =
        documentTypeRepository
            .findById(request.getDocumentTypeId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "DocumentType", "id", request.getDocumentTypeId()));

    OrganizationModel senderOrg = null;
    if (request.getSenderOrganizationId() != null) {
      senderOrg =
          organizationRepository
              .findById(request.getSenderOrganizationId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Organization", "id", request.getSenderOrganizationId()));
    }

    OrganizationModel receiverOrg = null;
    if (request.getReceiverOrganizationId() != null) {
      receiverOrg =
          organizationRepository
              .findById(request.getReceiverOrganizationId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Organization", "id", request.getReceiverOrganizationId()));
    }

    // Check if document number is unique for this document type
    if (documentRepository
        .findByDocumentNumberAndDocumentTypeId(
            request.getDocumentNumber(), request.getDocumentTypeId())
        .isPresent()) {
      throw new com.norton.backend.exceptions.BadRequestException(
          "Document number already exists for this document type: " + request.getDocumentNumber());
    }

    DocumentModel doc =
        DocumentModel.builder()
            .direction("INTERNAL")
            .documentType(docType)
            .senderOrganization(senderOrg)
            .receiverOrganization(receiverOrg)
            .documentNumber(request.getDocumentNumber())
            .documentDate(request.getDocumentDate())
            .subject(request.getSubject())
            .summary(request.getSummary())
            .confidentiality(request.getConfidentiality())
            .priority(request.getPriority())
            .status(request.getStatus())
            .remarks(request.getRemarks())
            .createdBy(creator)
            .files(new ArrayList<>())
            .build();

    doc = documentRepository.save(doc);

    if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
      for (int i = 0; i < request.getFileIds().size(); i++) {
        Long fileId = request.getFileIds().get(i);
        UploadImageModel uploadImage =
            uploadImageRepository
                .findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("UploadImage", "id", fileId));

        DocumentFileModel docFile =
            DocumentFileModel.builder()
                .document(doc)
                .uploadImage(uploadImage)
                .fileName(uploadImage.getFileName())
                .filePath(uploadImage.getUrl())
                .mimeType(extractMimeType(uploadImage.getFileName()))
                .isPrimary(i == 0)
                .uploadedBy(creator)
                .build();

        docFile = documentFileRepository.save(docFile);
        doc.getFiles().add(docFile);
      }
    }

    return getInternalDocumentDetails(doc.getId());
  }

  private String extractMimeType(String fileName) {
    if (fileName == null) {
      return "application/octet-stream";
    }
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".pdf")) {
      return "application/pdf";
    }
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
      return "image/jpeg";
    }
    if (lower.endsWith(".webp")) {
      return "image/webp";
    }
    return "application/octet-stream";
  }

  @Override
  @Transactional
  public InternalDocDetailsResponse updateInternalDocument(
      Long id, CreateInternalDocRequest request, String currentUsername) {
    DocumentModel doc =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

    if (!"INTERNAL".equals(doc.getDirection())) {
      throw new com.norton.backend.exceptions.BadRequestException(
          "Document is not an internal document");
    }

    UserModel user =
        userRepository
            .findByUsername(currentUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));
    OfficerModel creator = user.getOfficer();
    if (creator == null) {
      throw new com.norton.backend.exceptions.BadRequestException(
          "Authenticated user is not associated with an officer profile");
    }

    // Check if document number is unique for this document type (excluding this document)
    if (request.getDocumentNumber() != null && !request.getDocumentNumber().isBlank()) {
      Optional<DocumentModel> existingDoc =
          documentRepository.findByDocumentNumberAndDocumentTypeId(
              request.getDocumentNumber(), request.getDocumentTypeId());
      if (existingDoc.isPresent() && !existingDoc.get().getId().equals(id)) {
        throw new com.norton.backend.exceptions.BadRequestException(
            "Document number already exists for this document type: "
                + request.getDocumentNumber());
      }
    }

    doc.setDocumentNumber(request.getDocumentNumber());
    doc.setDocumentDate(request.getDocumentDate());
    doc.setSubject(request.getSubject());
    doc.setSummary(request.getSummary());
    doc.setConfidentiality(request.getConfidentiality());
    doc.setPriority(request.getPriority());
    doc.setStatus(request.getStatus());
    doc.setRemarks(request.getRemarks());

    if (request.getDocumentTypeId() != null) {
      DocumentTypeModel type =
          documentTypeRepository
              .findById(request.getDocumentTypeId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "DocumentType", "id", request.getDocumentTypeId()));
      doc.setDocumentType(type);
    }

    if (request.getSenderOrganizationId() != null) {
      OrganizationModel senderOrg =
          organizationRepository
              .findById(request.getSenderOrganizationId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Organization", "id", request.getSenderOrganizationId()));
      doc.setSenderOrganization(senderOrg);
    } else {
      doc.setSenderOrganization(null);
    }

    if (request.getReceiverOrganizationId() != null) {
      OrganizationModel receiverOrg =
          organizationRepository
              .findById(request.getReceiverOrganizationId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Organization", "id", request.getReceiverOrganizationId()));
      doc.setReceiverOrganization(receiverOrg);
    } else {
      doc.setReceiverOrganization(null);
    }

    if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
      for (int i = 0; i < request.getFileIds().size(); i++) {
        Long fileId = request.getFileIds().get(i);
        UploadImageModel uploadImage =
            uploadImageRepository
                .findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("UploadImage", "id", fileId));

        boolean alreadyExists =
            doc.getFiles().stream()
                .anyMatch(
                    file ->
                        file.getUploadImage() != null
                            && file.getUploadImage().getId().equals(fileId));

        if (!alreadyExists) {
          DocumentFileModel docFile =
              DocumentFileModel.builder()
                  .document(doc)
                  .uploadImage(uploadImage)
                  .fileName(uploadImage.getFileName())
                  .filePath(uploadImage.getUrl())
                  .mimeType(extractMimeType(uploadImage.getFileName()))
                  .isPrimary(doc.getFiles().isEmpty() && i == 0)
                  .uploadedBy(creator)
                  .build();

          docFile = documentFileRepository.save(docFile);
          doc.getFiles().add(docFile);
        }
      }
    }

    doc = documentRepository.save(doc);

    return getInternalDocumentDetails(doc.getId());
  }
}
