package com.norton.backend.services.document;

import com.norton.backend.dto.request.document.CreateDocumentRequest;
import com.norton.backend.dto.responses.document.DocumentDetailsResponse;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.models.DocumentFileModel;
import com.norton.backend.models.DocumentLogModel;
import com.norton.backend.models.DocumentModel;
import com.norton.backend.models.DocumentTypeModel;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.OrganizationModel;
import com.norton.backend.models.UploadImageModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.DocumentFileRepository;
import com.norton.backend.repositories.DocumentLogRepository;
import com.norton.backend.repositories.DocumentRepository;
import com.norton.backend.repositories.DocumentTypeRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.OrganizationRepository;
import com.norton.backend.repositories.UploadImageRepository;
import com.norton.backend.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

  private final DocumentTypeRepository documentTypeRepository;
  private final DocumentRepository documentRepository;
  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final UploadImageRepository uploadImageRepository;
  private final DocumentFileRepository documentFileRepository;
  private final OfficerRepository officerRepository;
  private final DocumentLogRepository documentLogRepository;

  @Override
  @Transactional(readOnly = true)
  public DocumentDetailsResponse getDocumentDetails(Long id) {
    DocumentModel doc =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    return convertToResponse(doc);
  }

  private DocumentDetailsResponse convertToResponse(DocumentModel doc) {

    DocumentDetailsResponse.DocTypeDto typeDto = null;
    if (doc.getDocumentType() != null) {
      typeDto =
          DocumentDetailsResponse.DocTypeDto.builder()
              .id(doc.getDocumentType().getId())
              .name(doc.getDocumentType().getName())
              .code(doc.getDocumentType().getCode())
              .build();
    }

    DocumentDetailsResponse.OrgDto senderDto = null;
    if (doc.getSenderOrganization() != null) {
      senderDto =
          DocumentDetailsResponse.OrgDto.builder()
              .id(doc.getSenderOrganization().getId())
              .name(doc.getSenderOrganization().getName())
              .shortName(doc.getSenderOrganization().getShortName())
              .build();
    }

    DocumentDetailsResponse.OrgDto receiverDto = null;
    if (doc.getReceiverOrganization() != null) {
      receiverDto =
          DocumentDetailsResponse.OrgDto.builder()
              .id(doc.getReceiverOrganization().getId())
              .name(doc.getReceiverOrganization().getName())
              .shortName(doc.getReceiverOrganization().getShortName())
              .build();
    }

    DocumentDetailsResponse.CreatorDto creatorDto = null;
    if (doc.getCreatedBy() != null) {
      creatorDto =
          DocumentDetailsResponse.CreatorDto.builder()
              .id(doc.getCreatedBy().getId())
              .officerCode(doc.getCreatedBy().getOfficerCode())
              .firstName(doc.getCreatedBy().getFirstName())
              .lastName(doc.getCreatedBy().getLastName())
              .build();
    }

    List<DocumentDetailsResponse.FileDto> filesDto =
        doc.getFiles().stream()
            .map(
                file ->
                    DocumentDetailsResponse.FileDto.builder()
                        .id(file.getId())
                        .fileName(file.getFileName())
                        .mimeType(file.getMimeType())
                        .fileSize(file.getFileSize())
                        .isPrimary(file.getIsPrimary())
                        .fileUrl(
                            file.getUploadImage() != null ? file.getUploadImage().getUrl() : null)
                        .build())
            .collect(Collectors.toList());

    List<DocumentDetailsResponse.LogDto> logsDto = new ArrayList<>();
    try {
      logsDto =
          documentLogRepository.findByDocumentId(doc.getId()).stream()
              .map(
                  log ->
                      DocumentDetailsResponse.LogDto.builder()
                          .id(log.getId())
                          .officerName(
                              log.getOfficer() != null
                                  ? log.getOfficer().getFirstName()
                                      + " "
                                      + log.getOfficer().getLastName()
                                  : "Unknown")
                          .action(log.getAction())
                          .description(log.getDescription())
                          .createdAt(
                              log.getCreatedAt() != null ? log.getCreatedAt().toString() : null)
                          .build())
              .collect(Collectors.toList());
    } catch (Exception e) {
      // Ignore
    }

    return DocumentDetailsResponse.builder()
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
        .logs(logsDto)
        .build();
  }

  @Override
  @Transactional
  public DocumentDetailsResponse createDocument(
      CreateDocumentRequest request, String currentUsername) {
    UserModel user =
        userRepository
            .findByUsername(currentUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));

    OfficerModel creator = user.getOfficer();
    if (creator == null) {
      List<OfficerModel> officers = officerRepository.findAll();
      if (!officers.isEmpty()) {
        creator = officers.get(0);
      } else {
        throw new com.norton.backend.exceptions.BadRequestException(
            "Authenticated user is not associated with an officer profile, and no officer profiles exist in the database.");
      }
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
    } else if ("OUTGOING".equalsIgnoreCase(request.getDirection())) {
      // Default sender to NU (Norton University - ID 3) for Outgoing docs
      senderOrg = organizationRepository.findById(3L).orElse(null);
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
    } else if (request.getReceiverOrganizationName() != null
        && !request.getReceiverOrganizationName().isBlank()) {
      String name = request.getReceiverOrganizationName().trim();
      String shortName = name.length() > 100 ? name.substring(0, 97) + "..." : name;
      receiverOrg =
          organizationRepository
              .findByName(name)
              .orElseGet(
                  () -> {
                    OrganizationModel newOrg =
                        OrganizationModel.builder()
                            .name(name)
                            .shortName(shortName)
                            .status("ACTIVE")
                            .build();
                    return organizationRepository.save(newOrg);
                  });
    }

    // Check if document number is unique for this document type
    if (request.getDocumentNumber() != null && !request.getDocumentNumber().isBlank()) {
      if (documentRepository
          .findByDocumentNumberAndDocumentTypeId(
              request.getDocumentNumber(), request.getDocumentTypeId())
          .isPresent()) {
        throw new com.norton.backend.exceptions.BadRequestException(
            "Document number already exists for this document type: "
                + request.getDocumentNumber());
      }
    }

    DocumentModel doc =
        DocumentModel.builder()
            .direction(request.getDirection())
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

    // Create CREATE log
    DocumentLogModel createLog =
        DocumentLogModel.builder()
            .document(doc)
            .officer(creator)
            .action("CREATE")
            .description("បានចុះបញ្ជី និងផ្ទុកឡើងឯកសារថ្មី")
            .build();
    documentLogRepository.save(createLog);

    return getDocumentDetails(doc.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public List<DocumentDetailsResponse.DocTypeDto> getDocumentTypes() {
    return documentTypeRepository.findAll().stream()
        .map(
            type ->
                DocumentDetailsResponse.DocTypeDto.builder()
                    .id(type.getId())
                    .name(type.getName())
                    .code(type.getCode())
                    .build())
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<DocumentDetailsResponse> getAllDocuments(String status) {
    List<DocumentModel> docs;
    if (status != null && !status.isBlank()) {
      docs = documentRepository.findByStatus(status, Sort.by(Sort.Direction.DESC, "id"));
    } else {
      docs = documentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }
    return docs.stream().map(this::convertToResponse).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void deleteDocument(Long id) {
    DocumentModel doc =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    documentRepository.delete(doc);
  }

  @Override
  @Transactional
  public DocumentDetailsResponse updateDocument(
      Long id, CreateDocumentRequest request, String currentUsername) {
    DocumentModel doc =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

    UserModel user =
        userRepository
            .findByUsername(currentUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));
    OfficerModel creator = user.getOfficer();
    if (creator == null) {
      List<OfficerModel> officers = officerRepository.findAll();
      if (!officers.isEmpty()) {
        creator = officers.get(0);
      } else {
        throw new com.norton.backend.exceptions.BadRequestException(
            "No officer profiles exist in the database.");
      }
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

    if (request.getDirection() != null && !request.getDirection().isBlank()) {
      doc.setDirection(request.getDirection());
    }
    doc.setDocumentNumber(request.getDocumentNumber());
    doc.setDocumentDate(request.getDocumentDate());
    doc.setSubject(request.getSubject());
    doc.setSummary(request.getSummary());
    doc.setConfidentiality(request.getConfidentiality());
    doc.setPriority(request.getPriority());
    doc.setStatus(request.getStatus());
    doc.setRemarks(request.getRemarks());
    doc.setUpdatedBy(creator);

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
    } else if ("OUTGOING".equalsIgnoreCase(request.getDirection())) {
      OrganizationModel senderOrg = organizationRepository.findById(3L).orElse(null);
      doc.setSenderOrganization(senderOrg);
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
    } else if (request.getReceiverOrganizationName() != null
        && !request.getReceiverOrganizationName().isBlank()) {
      String name = request.getReceiverOrganizationName().trim();
      String shortName = name.length() > 100 ? name.substring(0, 97) + "..." : name;
      OrganizationModel receiverOrg =
          organizationRepository
              .findByName(name)
              .orElseGet(
                  () -> {
                    OrganizationModel newOrg =
                        OrganizationModel.builder()
                            .name(name)
                            .shortName(shortName)
                            .status("ACTIVE")
                            .build();
                    return organizationRepository.save(newOrg);
                  });
      doc.setReceiverOrganization(receiverOrg);
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

    // Create UPDATE log
    DocumentLogModel updateLog =
        DocumentLogModel.builder()
            .document(doc)
            .officer(creator)
            .action("UPDATE")
            .description("បានធ្វើបច្ចុប្បន្នភាពព័ត៌មានឯកសារ")
            .build();
    documentLogRepository.save(updateLog);

    return convertToResponse(doc);
  }

  @Override
  @Transactional
  public DocumentDetailsResponse updateStatus(Long id, String status, String currentUsername) {
    DocumentModel doc =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

    UserModel user =
        userRepository
            .findByUsername(currentUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUsername));
    OfficerModel officer = user.getOfficer();
    if (officer == null) {
      List<OfficerModel> officers = officerRepository.findAll();
      if (!officers.isEmpty()) {
        officer = officers.get(0);
      } else {
        throw new com.norton.backend.exceptions.BadRequestException(
            "Authenticated user is not associated with an officer profile, and no officer profiles exist in the database.");
      }
    }

    String oldStatus = doc.getStatus();
    String newStatus = status != null ? status.trim().toUpperCase() : "";

    doc.setStatus(newStatus);
    doc.setUpdatedBy(officer);
    doc = documentRepository.save(doc);

    DocumentLogModel statusLog =
        DocumentLogModel.builder()
            .document(doc)
            .officer(officer)
            .action("STATUS_CHANGE")
            .description("ស្ថានភាពត្រូវបានផ្លាស់ប្តូរពី " + oldStatus + " ទៅជា " + newStatus)
            .build();
    documentLogRepository.save(statusLog);

    return convertToResponse(doc);
  }

  private String extractMimeType(String fileName) {
    if (fileName == null) return "application/octet-stream";
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".pdf")) return "application/pdf";
    if (lower.endsWith(".png")) return "image/png";
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
    if (lower.endsWith(".webp")) return "image/webp";
    return "application/octet-stream";
  }
}
