package com.norton.backend.services.superadmin;

import com.norton.backend.dto.responses.superadmin.SuperAdminStatsResponse;
import com.norton.backend.enums.UserStatus;
import com.norton.backend.repositories.DepartmentRepository;
import com.norton.backend.repositories.OfficerRepository;
import com.norton.backend.repositories.PermissionRepository;
import com.norton.backend.repositories.PositionRepository;
import com.norton.backend.repositories.UserRepository;
import com.norton.backend.repositories.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuperAdminDashboardServiceImpl implements SuperAdminDashboardService {

  private final UserRepository userRepository;
  private final OfficerRepository officerRepository;
  private final UserRoleRepository userRoleRepository;
  private final PermissionRepository permissionRepository;
  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;

  @Override
  public SuperAdminStatsResponse getSystemStats() {
    long totalUsers = userRepository.count();
    long activeUsers = userRepository.countByUserStatus(UserStatus.ACTIVE);
    long bannedUsers = userRepository.countByUserStatus(UserStatus.BANNED);
    long totalOfficers = officerRepository.count();
    long totalRoles = userRoleRepository.count();
    long totalPermissions = permissionRepository.count();
    long totalDepartments = departmentRepository.count();
    long totalPositions = positionRepository.count();

    return SuperAdminStatsResponse.builder()
        .totalUsers(totalUsers)
        .activeUsers(activeUsers)
        .bannedUsers(bannedUsers)
        .totalOfficers(totalOfficers)
        .totalRoles(totalRoles)
        .totalPermissions(totalPermissions)
        .totalDepartments(totalDepartments)
        .totalPositions(totalPositions)
        .build();
  }
}
