package com.norton.backend.dto.responses.upload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadImageResponse {

  private boolean success;
  private String message;
  private UploadImageDataResponse data;
}
