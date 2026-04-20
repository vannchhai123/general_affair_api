package com.norton.backend.dto.responses.officers;

import com.norton.backend.enums.GenderEnum;
import com.norton.backend.enums.OfficerStatus;
import lombok.Data;

@Data
public class OfficerResponse {
  private String uuid;
  private String officerCode;
  private String firstName;
  private String lastName;
  private GenderEnum gender;
  private String phone;
  private String email;
  private String imageUrl;
  private OfficerStatus status;
  private String positionName;
  private String departmentName;
}
