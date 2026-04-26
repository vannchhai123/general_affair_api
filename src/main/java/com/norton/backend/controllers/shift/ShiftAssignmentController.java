package com.norton.backend.controllers.shift;

import com.norton.backend.dto.request.ShiftAssignmentTemplateRequest;
import com.norton.backend.dto.responses.shifts.ShiftAssignmentResponseDto;
import com.norton.backend.dto.responses.shifts.ShiftAssignmentTemplateResponseDto;
import com.norton.backend.services.shift.ShiftAssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ShiftAssignmentController.BASE_URL)
public class ShiftAssignmentController {
  public static final String BASE_URL = "/api/v1/shift-assignments";

  private final ShiftAssignmentService shiftAssignmentService;

  @GetMapping
  public ResponseEntity<List<ShiftAssignmentResponseDto>> listAssignments(
      @RequestParam(required = false) String scope, @RequestParam(required = false) Long id) {
    return ResponseEntity.ok(shiftAssignmentService.listAssignments(scope, id));
  }

  @PostMapping
  public ResponseEntity<ShiftAssignmentTemplateResponseDto> saveTemplate(
      @Valid @RequestBody ShiftAssignmentTemplateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(shiftAssignmentService.saveTemplate(request));
  }
}
