package com.norton.backend.controllers.mobile;

import com.norton.backend.dto.responses.mobile.MobileHomeResponse;
import com.norton.backend.services.mobile.MobileHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(MobileHomeController.BASE_URL)
public class MobileHomeController {

  public static final String BASE_URL = "/api/v1/mobile";

  private final MobileHomeService mobileHomeService;

  @GetMapping("/home")
  public ResponseEntity<MobileHomeResponse> getHomeData() {
    return ResponseEntity.ok(mobileHomeService.getHomeData());
  }
}
