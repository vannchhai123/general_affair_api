package com.norton.backend.dto.responses;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

  private int status;
  private String message;
  private String error;
  private String path;
  private LocalDateTime timestamp;
  private Map<String, String> errors;
  private String traceId;

  public static ErrorResponse of(HttpStatus status, String message, String path) {
    ErrorResponse response = new ErrorResponse();
    response.setTimestamp(LocalDateTime.now());
    response.setStatus(status.value());
    response.setError(message);
    response.setMessage(message);
    response.setPath(path);
    return response;
  }
}
