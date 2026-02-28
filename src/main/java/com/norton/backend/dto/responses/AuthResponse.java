package com.norton.backend.dto.responses;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse<T> {
  private String accessToken;
  private String refreshToken;
  private T data;
}
