package com.norton.backend.services.attendance;

import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.attendances.AttendanceResponse;
import com.norton.backend.repositories.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

  private final AttendanceRepository attendanceRepository;

  @Override
  public PageResponse<AttendanceResponse> getAllAttendance(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

    Page<AttendanceResponse> result = attendanceRepository.findAllAttendance(pageable);

    return PageResponse.<AttendanceResponse>builder()
        .content(result.getContent())
        .page(result.getNumber())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .last(result.isLast())
        .build();
  }
}
