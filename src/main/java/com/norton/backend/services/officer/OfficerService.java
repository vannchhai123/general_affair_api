package com.norton.backend.services.officer;

import com.norton.backend.dto.request.CreateOfficerRequest;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.dto.responses.officers.CreateOfficerResponse;
import com.norton.backend.dto.responses.officers.MeResponse;
import com.norton.backend.dto.responses.officers.OfficerResponseDto;
import com.norton.backend.dto.responses.officers.OfficerStatsResponse;
import org.springframework.data.domain.Pageable;

public interface OfficerService {
  MeResponse getMyProfile();

  CreateOfficerResponse createOfficer(CreateOfficerRequest request);

  CreateOfficerResponse updateOfficer(Long id, CreateOfficerRequest request);

  PageResponse<OfficerResponseDto> getAllOfficers(Pageable pageable);

  OfficerStatsResponse getOfficerStats();
}
