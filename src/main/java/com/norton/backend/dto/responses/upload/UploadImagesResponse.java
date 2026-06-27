package com.norton.backend.dto.responses.upload;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadImagesResponse {

  private boolean success;
  private String message;
  private List<UploadImageDataResponse> data;
}
