package com.norton.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResetRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Size(max = 255, message = "Email must not exceed 255 characters")
  private String email;

  @NotBlank(message = "OTP is required")
  @Pattern(regexp = "^\\d{6}$", message = "OTP must be exactly 6 digits")
  private String otp;

  @NotBlank(message = "New password is required")
  @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).+$",
      message =
          "New password must include at least one uppercase letter, one lowercase letter, one number, and one special character")
  private String newPassword;

  @NotBlank(message = "Confirm password is required")
  private String confirmPassword;
}
