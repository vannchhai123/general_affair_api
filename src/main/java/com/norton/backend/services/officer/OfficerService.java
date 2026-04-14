package com.norton.backend.services.officer;

import com.norton.backend.dto.responses.MeResponse;
import com.norton.backend.dto.responses.OfficerResponseDto;
import com.norton.backend.dto.responses.PageResponse;
import org.springframework.data.domain.Pageable;

public interface OfficerService {
  MeResponse getMyProfile();

  PageResponse<OfficerResponseDto> getAllOfficers(Pageable pageable);
}
