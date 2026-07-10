package com.norton.backend.dto.responses;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

  private UUID uuid;
  private String fullName;
  private String role;
  private boolean enabled;
  private String imageUrl;
  private java.util.List<String> permissions;
}
