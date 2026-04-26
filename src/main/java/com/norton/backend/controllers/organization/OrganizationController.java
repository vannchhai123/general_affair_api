package com.norton.backend.controllers.organization;

import com.norton.backend.dto.request.DepartmentUpsertRequest;
import com.norton.backend.dto.request.PositionUpsertRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.organization.DepartmentResponseDto;
import com.norton.backend.dto.responses.organization.OrganizationSummaryResponse;
import com.norton.backend.dto.responses.organization.PositionResponseDto;
import com.norton.backend.services.organization.OrganizationService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(OrganizationController.BASE_URL)
@RequiredArgsConstructor
public class OrganizationController {

  public static final String BASE_URL = "/api/v1/organizations";
  //  public static final String LEGACY_BASE_URL = "/api/v1/organizatioins";
  private final OrganizationService organizationService;

  @GetMapping("/department")
  public ResponseEntity<PageResponse<DepartmentResponseDto>> listDepartments(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(organizationService.listDepartments(search, status, pageable));
  }

  @PostMapping("/department")
  public ResponseEntity<DepartmentResponseDto> createDepartment(
      @Valid @RequestBody DepartmentUpsertRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(organizationService.createDepartment(request));
  }

  @GetMapping("/department/{id}")
  public ResponseEntity<DepartmentResponseDto> getDepartment(@PathVariable Long id) {
    return ResponseEntity.ok(organizationService.getDepartmentById(id));
  }

  @PutMapping("/department/{id}")
  public ResponseEntity<DepartmentResponseDto> updateDepartment(
      @PathVariable Long id, @Valid @RequestBody DepartmentUpsertRequest request) {
    return ResponseEntity.ok(organizationService.updateDepartment(id, request));
  }

  @DeleteMapping("/department/{id}")
  public ResponseEntity<Map<String, String>> deleteDepartment(@PathVariable Long id) {
    organizationService.deleteDepartment(id);
    return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
  }

  @GetMapping("/position")
  public ResponseEntity<PageResponse<PositionResponseDto>> listPositions(
      @RequestParam(required = false) String search,
      @RequestParam(required = false, name = "department_id") Long departmentId,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(
        organizationService.listPositions(search, departmentId, status, pageable));
  }

  @PostMapping("/position")
  public ResponseEntity<PositionResponseDto> createPosition(
      @Valid @RequestBody PositionUpsertRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(organizationService.createPosition(request));
  }

  @GetMapping("/position/{id}")
  public ResponseEntity<PositionResponseDto> getPosition(@PathVariable Long id) {
    return ResponseEntity.ok(organizationService.getPositionById(id));
  }

  @PutMapping("/position/{id}")
  public ResponseEntity<PositionResponseDto> updatePosition(
      @PathVariable Long id, @Valid @RequestBody PositionUpsertRequest request) {
    return ResponseEntity.ok(organizationService.updatePosition(id, request));
  }

  @DeleteMapping("/position/{id}")
  public ResponseEntity<Map<String, String>> deletePosition(@PathVariable Long id) {
    organizationService.deletePosition(id);
    return ResponseEntity.ok(Map.of("message", "Position deleted successfully"));
  }

  @GetMapping("/organization/summary")
  public ResponseEntity<OrganizationSummaryResponse> getOrganizationSummary() {
    return ResponseEntity.ok(organizationService.getSummary());
  }
}
