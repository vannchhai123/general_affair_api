package com.norton.backend.services.officer;

import com.norton.backend.dto.responses.MeResponse;
import com.norton.backend.dto.responses.OfficerResponseDto;
import com.norton.backend.dto.responses.PageResponse;
import com.norton.backend.mapper.OfficerMapper;
import com.norton.backend.mapper.UserMapper;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.OfficerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OfficerServiceImpl implements OfficerService {

  private final UserMapper userMapper;
  private final OfficerRepository officerRepository;
  private final OfficerMapper officerMapper;

  @Override
  public MeResponse getMyProfile() {

    UserModel currentUser =
        (UserModel) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    return userMapper.toMeResponse(currentUser);
  }

  @Override
  public PageResponse<OfficerResponseDto> getAllOfficers(Pageable pageable) {

    Page<OfficerModel> officer = officerRepository.findAll(pageable);
    List<OfficerResponseDto> content =
        officer.getContent().stream().map(officerMapper::toResponse).toList();

    return PageResponse.<OfficerResponseDto>builder()
        .content(content)
        .page(officer.getNumber())
        .size(officer.getSize())
        .totalElements(officer.getTotalElements())
        .totalPages(officer.getTotalPages())
        .last(officer.isLast())
        .build();
  }
}
