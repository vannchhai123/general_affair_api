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

  OfficerResponseDto getOfficerById(Long id);

  PageResponse<OfficerResponseDto> getAllOfficers(String search, Pageable pageable);

  java.util.List<OfficerResponseDto> getOfficersByOffice(Long officeId);

  java.util.List<OfficerResponseDto> getOfficersByPosition(Long positionId);

  java.util.List<com.norton.backend.dto.responses.officers.OfficerResponse>
      getEligibleInvitationParticipants(String keyword, Integer limit);

  OfficerStatsResponse getOfficerStats();

  void deleteOfficer(Long id);
}
