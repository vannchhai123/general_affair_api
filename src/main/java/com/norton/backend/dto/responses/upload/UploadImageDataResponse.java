package com.norton.backend.dto.responses.upload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadImageDataResponse {

  private Long id;

  @JsonProperty("fileName")
  private String fileName;

  private String url;
}
