package com.norton.backend.dto.responses.officers;

import lombok.Data;

@Data
public class MeResponse {

  private String uuid;
  private String username;
  private String fullName;
  private String role;

  private OfficerResponse officer;
}
