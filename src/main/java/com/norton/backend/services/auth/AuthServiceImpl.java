package com.norton.backend.services.auth;

import com.norton.backend.dto.request.ChangePasswordRequest;
import com.norton.backend.dto.request.ForgotPasswordResetRequest;
import com.norton.backend.dto.request.ForgotPasswordVerifyEmailRequest;
import com.norton.backend.dto.request.ForgotPasswordVerifyOtpRequest;
import com.norton.backend.dto.request.LoginRequest;
import com.norton.backend.dto.responses.AuthResponse;
import com.norton.backend.dto.responses.UserDto;
import com.norton.backend.dto.responses.officers.MeResponse;
import com.norton.backend.exceptions.BadRequestException;
import com.norton.backend.exceptions.ResourceNotFoundException;
import com.norton.backend.mapper.UserMapper;
import com.norton.backend.models.PasswordResetOtpModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.PasswordResetOtpRepository;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.security.CustomUserDetailsService;
import com.norton.backend.security.JwtService;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private static final String FORGOT_PASSWORD_GENERIC_MESSAGE =
      "If an account exists for that email, password reset instructions have been sent.";

  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final PasswordResetOtpRepository passwordResetOtpRepository;
  private final AuthenticationManager authenticationManager;
  private final CustomUserDetailsService customUserDetailsService;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final ObjectProvider<JavaMailSender> mailSenderProvider;

  @Value("${app.password-reset.otp-expiry-minutes:10}")
  private int otpExpiryMinutes;

  @Value("${app.password-reset.mail-from:no-reply@general-affair.local}")
  private String mailFrom;

  @Override
  public AuthResponse<UserDto> login(LoginRequest request) {

    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    UserModel user = (UserModel) customUserDetailsService.loadUserByUsername(request.getUsername());
    String accessToken = jwtService.generateToken(Map.of(), user);
    String refreshToken = jwtService.generateRefreshToken(user);

    UserDto userDto = userMapper.toDto(user);

    return AuthResponse.<UserDto>builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .data(userDto)
        .build();
  }

  @Override
  public AuthResponse<UserDto> refreshToken(String refreshToken) {

    if (!jwtService.isRefreshTokenValid(refreshToken)) {
      throw new RuntimeException("Invalid refresh token");
    }

    String username = jwtService.extractUsername(refreshToken);
    UserModel user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

    String accessToken = jwtService.generateToken(Map.of(), user);
    String newRefreshToken = jwtService.generateRefreshToken(user);

    return AuthResponse.<UserDto>builder()
        .accessToken(accessToken)
        .refreshToken(newRefreshToken)
        .data(userMapper.toDto(user))
        .build();
  }

  @Override
  public MeResponse getMyProfile() {
    UserModel currentUser =
        (UserModel) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    return userMapper.toMeResponse(currentUser);
  }

  @Override
  @Transactional
  public Map<String, String> changePassword(ChangePasswordRequest request) {
    UserModel currentUser =
        userRepository
            .findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", "current user"));

    if (!"ROLE_ADMIN".equals(currentUser.getRole().getRoleName())) {
      throw new BadRequestException("Only admin users can change password");
    }

    if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPasswordHash())) {
      throw new BadRequestException("Current password is incorrect");
    }

    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new BadRequestException("New password and confirm password do not match");
    }

    currentUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(currentUser);

    return Map.of("message", "Password changed successfully");
  }

  @Override
  @Transactional
  public Map<String, String> forgotPasswordVerifyEmail(ForgotPasswordVerifyEmailRequest request) {
    String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

    userRepository
        .findByEmailIgnoreCase(normalizedEmail)
        .ifPresent(this::createAndSendResetOtpSilently);

    return Map.of("message", FORGOT_PASSWORD_GENERIC_MESSAGE);
  }

  @Override
  @Transactional
  public Map<String, String> forgotPasswordVerifyOtp(ForgotPasswordVerifyOtpRequest request) {
    PasswordResetOtpModel otpRecord = getValidOtpRecord(request.getEmail(), request.getOtp());
    if (otpRecord == null) {
      throw new BadRequestException("Invalid or expired OTP");
    }

    return Map.of("message", "OTP verified successfully");
  }

  @Override
  @Transactional
  public Map<String, String> forgotPasswordReset(ForgotPasswordResetRequest request) {
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new BadRequestException("New password and confirm password do not match");
    }

    PasswordResetOtpModel otpRecord = getValidOtpRecord(request.getEmail(), request.getOtp());
    if (otpRecord == null) {
      throw new BadRequestException("Invalid or expired OTP");
    }

    UserModel user = otpRecord.getUser();
    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    otpRecord.setUsed(true);
    passwordResetOtpRepository.save(otpRecord);

    return Map.of("message", "Password reset successfully");
  }

  private void createAndSendResetOtpSilently(UserModel user) {
    String otp = generateOtp();

    passwordResetOtpRepository.deleteByUserAndUsedFalse(user);
    passwordResetOtpRepository.save(
        PasswordResetOtpModel.builder()
            .user(user)
            .otpHash(passwordEncoder.encode(otp))
            .expiresAt(Instant.now().plus(otpExpiryMinutes, ChronoUnit.MINUTES))
            .used(false)
            .build());

    try {
      sendForgotPasswordEmail(user.getEmail(), otp);
    } catch (MailException ex) {
      log.warn("Failed to send password reset email to {}", user.getEmail(), ex);
    }
  }

  private String generateOtp() {
    SecureRandom secureRandom = new SecureRandom();
    int value = secureRandom.nextInt(900000) + 100000;
    return String.valueOf(value);
  }

  private void sendForgotPasswordEmail(String toEmail, String otp) {
    JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
    if (mailSender == null) {
      log.info("Skipping reset email send because JavaMailSender is not configured");
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(mailFrom);
    message.setTo(toEmail);
    message.setSubject("Password reset instructions");
    message.setText(
        "Use this OTP to reset your password: "
            + otp
            + "\n\nThis code expires in "
            + otpExpiryMinutes
            + " minutes.\nIf you did not request a password reset, please ignore this email.");
    mailSender.send(message);
  }

  private PasswordResetOtpModel getValidOtpRecord(String email, String otp) {
    String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
    UserModel user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
    if (user == null) {
      return null;
    }

    PasswordResetOtpModel otpRecord =
        passwordResetOtpRepository.findTopByUserAndUsedFalseOrderByIdDesc(user).orElse(null);
    if (otpRecord == null) {
      return null;
    }

    if (otpRecord.getExpiresAt().isBefore(Instant.now())) {
      return null;
    }

    if (!passwordEncoder.matches(otp, otpRecord.getOtpHash())) {
      return null;
    }

    return otpRecord;
  }
}
