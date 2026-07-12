package com.norton.backend.dto.responses.document;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDetailsResponse {
  private Long id;
  private String uuid;
  private String direction;
  private String documentNumber;
  private String documentDate;
  private String receivedDate;
  private String subject;
  private String summary;
  private String confidentiality;
  private String priority;
  private String status;
  private String remarks;

  private DocTypeDto documentType;
  private OrgDto senderOrganization;
  private OrgDto receiverOrganization;
  private CreatorDto createdBy;
  private List<FileDto> files;
  private List<LogDto> logs;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class LogDto {
    private Long id;
    private String officerName;
    private String action;
    private String description;
    private String createdAt;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class DocTypeDto {
    private Long id;
    private String name;
    private String code;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class OrgDto {
    private Long id;
    private String name;
    private String shortName;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CreatorDto {
    private Long id;
    private String officerCode;
    private String firstName;
    private String lastName;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class FileDto {
    private Long id;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private Boolean isPrimary;
    private String fileUrl;
  }
}
