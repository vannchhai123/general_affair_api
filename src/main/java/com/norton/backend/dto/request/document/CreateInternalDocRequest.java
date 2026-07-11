package com.norton.backend.dto.request.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInternalDocRequest {

  @NotNull(message = "Document type ID is required")
  private Long documentTypeId;

  private Long senderOrganizationId;

  private Long receiverOrganizationId;

  @NotBlank(message = "Document number is required")
  private String documentNumber;

  @NotNull(message = "Document date is required")
  private LocalDate documentDate;

  @NotBlank(message = "Subject is required")
  private String subject;

  private String summary;

  private String confidentiality;

  private String priority;

  private String status;

  private String remarks;

  private List<Long> fileIds;
}
