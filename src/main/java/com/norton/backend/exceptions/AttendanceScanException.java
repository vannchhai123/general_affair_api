package com.norton.backend.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AttendanceScanException extends RuntimeException {
  private final HttpStatus status;
  private final String code;

  public AttendanceScanException(HttpStatus status, String message, String code) {
    super(message);
    this.status = status;
    this.code = code;
  }
}
