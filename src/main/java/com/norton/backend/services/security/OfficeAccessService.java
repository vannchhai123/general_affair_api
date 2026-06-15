package com.norton.backend.services.security;

import com.norton.backend.exceptions.UnauthorizedException;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.OfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OfficeAccessService {

  private final OfficerRepository officerRepository;

  public UserModel currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof UserModel currentUser)) {
      throw new UnauthorizedException("Unauthorized access");
    }
    return currentUser;
  }

  public boolean isSuperAdmin() {
    return hasRole("ROLE_SUPER_ADMIN");
  }

  public boolean isAdmin() {
    return hasRole("ROLE_ADMIN");
  }

  @Transactional(readOnly = true)
  public Long currentOfficeScopeIdOrNull() {
    if (isSuperAdmin()) {
      return null;
    }

    UserModel currentUser = currentUser();
    return officerRepository
        .findByUserIdWithPosition(currentUser.getId())
        .map(OfficeAccessService::officeIdOf)
        .orElseThrow(() -> new UnauthorizedException("Admin is not assigned to an office"));
  }

  public void assertCanAccessOfficer(OfficerModel officer) {
    Long scopeOfficeId = currentOfficeScopeIdOrNull();
    if (scopeOfficeId == null) {
      return;
    }

    Long officerOfficeId = officeIdOf(officer);
    if (!scopeOfficeId.equals(officerOfficeId)) {
      throw new UnauthorizedException("You can only access officers in your own office");
    }
  }

  public void assertCanAccessOffice(Long officeId) {
    Long scopeOfficeId = currentOfficeScopeIdOrNull();
    if (scopeOfficeId != null && !scopeOfficeId.equals(officeId)) {
      throw new UnauthorizedException("You can only manage your own office");
    }
  }

  private boolean hasRole(String roleName) {
    UserModel currentUser = currentUser();
    return currentUser.getRole() != null && roleName.equals(currentUser.getRole().getRoleName());
  }

  private static Long officeIdOf(OfficerModel officer) {
    if (officer == null) {
      return null;
    }
    if (officer.getOffice() != null) {
      return officer.getOffice().getId();
    }
    if (officer.getPosition() == null || officer.getPosition().getDepartment() == null) {
      return null;
    }
    return officer.getPosition().getDepartment().getId();
  }
}
