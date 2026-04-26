package com.norton.backend.services.shift;

import com.norton.backend.dto.request.ShiftUpsertRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.shifts.ShiftResponseDto;

public interface ShiftService {
  PageResponse<ShiftResponseDto> listShifts(String search, String status, int page, int size);

  ShiftResponseDto createShift(ShiftUpsertRequest request);

  ShiftResponseDto getShiftById(Long id);

  ShiftResponseDto updateShift(Long id, ShiftUpsertRequest request);

  ShiftResponseDto updateShiftStatus(Long id, String status);

  void deleteShift(Long id);
}
