package com.norton.backend.dto.responses.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalDocResponse {
  private Long id;
  private String title;
  private String documentNumber;
  private String description;
  private String documentDate;
  private String type;
}
