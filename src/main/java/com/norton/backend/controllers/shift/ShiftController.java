package com.norton.backend.controllers.shift;

import com.norton.backend.dto.request.ShiftStatusPatchRequest;
import com.norton.backend.dto.request.ShiftUpsertRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.shifts.ShiftResponseDto;
import com.norton.backend.services.shift.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ShiftController.BASE_URL)
public class ShiftController {
  public static final String BASE_URL = "/api/v1/shifts";

  private final ShiftService shiftService;

  @GetMapping
  @PreAuthorize(
      "hasRole('SUPER_ADMIN') and hasAuthority(T(com.norton.backend.security.Permissions).SHIFT_VIEW)")
  public ResponseEntity<PageResponse<ShiftResponseDto>> listShifts(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(shiftService.listShifts(search, status, page, size));
  }

  @PostMapping
  @PreAuthorize(
      "hasRole('SUPER_ADMIN') and hasAuthority(T(com.norton.backend.security.Permissions).SHIFT_CREATE)")
  public ResponseEntity<ShiftResponseDto> createShift(
      @Valid @RequestBody ShiftUpsertRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(shiftService.createShift(request));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      "hasRole('SUPER_ADMIN') and hasAuthority(T(com.norton.backend.security.Permissions).SHIFT_VIEW)")
  public ResponseEntity<ShiftResponseDto> getShiftById(@PathVariable Long id) {
    return ResponseEntity.ok(shiftService.getShiftById(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      "hasRole('SUPER_ADMIN') and hasAuthority(T(com.norton.backend.security.Permissions).SHIFT_UPDATE)")
  public ResponseEntity<ShiftResponseDto> updateShift(
      @PathVariable Long id, @Valid @RequestBody ShiftUpsertRequest request) {
    return ResponseEntity.ok(shiftService.updateShift(id, request));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize(
      "hasRole('SUPER_ADMIN') and hasAuthority(T(com.norton.backend.security.Permissions).SHIFT_UPDATE)")
  public ResponseEntity<ShiftResponseDto> patchStatus(
      @PathVariable Long id, @Valid @RequestBody ShiftStatusPatchRequest request) {
    return ResponseEntity.ok(shiftService.updateShiftStatus(id, request.getStatus()));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(
      "hasRole('SUPER_ADMIN') and hasAuthority(T(com.norton.backend.security.Permissions).SHIFT_DELETE)")
  public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
    shiftService.deleteShift(id);
    return ResponseEntity.noContent().build();
  }
}
