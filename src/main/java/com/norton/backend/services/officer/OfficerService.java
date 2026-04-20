package com.norton.backend.services.officer;

import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.officers.MeResponse;
import com.norton.backend.dto.responses.officers.OfficerResponseDto;
import com.norton.backend.dto.responses.officers.OfficerStatsResponse;
import org.springframework.data.domain.Pageable;

public interface OfficerService {
  MeResponse getMyProfile();

  PageResponse<OfficerResponseDto> getAllOfficers(Pageable pageable);

  OfficerStatsResponse getOfficerStats();
}
