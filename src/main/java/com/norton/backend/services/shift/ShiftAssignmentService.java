package com.norton.backend.services.shift;

import com.norton.backend.dto.request.ShiftAssignmentTemplateRequest;
import com.norton.backend.dto.responses.shifts.ShiftAssignmentResponseDto;
import com.norton.backend.dto.responses.shifts.ShiftAssignmentTemplateResponseDto;
import java.util.List;

public interface ShiftAssignmentService {
  List<ShiftAssignmentResponseDto> listAssignments(String scope, Long id);

  ShiftAssignmentTemplateResponseDto saveTemplate(ShiftAssignmentTemplateRequest request);
}
