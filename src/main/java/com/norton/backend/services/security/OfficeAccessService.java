package com.norton.backend.services.security;

import com.norton.backend.exceptions.UnauthorizedException;
import com.norton.backend.models.OfficerModel;
import com.norton.backend.models.UserModel;
import com.norton.backend.repositories.OfficerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
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

  public boolean isAdmin() {
    return hasRole("ROLE_ADMIN");
  }

  public boolean isHeadOffice() {
    return hasRole("ROLE_HEAD_OFFICE");
  }

  public boolean isManager() {
    return hasRole("ROLE_MANAGER");
  }

  public boolean hasGlobalOfficeAccess() {
    UserModel currentUser = currentUser();
    if (currentUser.getRole() == null) {
      return false;
    }
    Integer level = currentUser.getRole().getHierarchyLevel();
    if (level != null && level <= 5) {
      return true;
    }
    return isAdmin() || isHeadOffice() || isManager();
  }

  @Transactional(readOnly = true)
  public Long currentOfficeScopeIdOrNull() {
    if (hasGlobalOfficeAccess()) {
      return null;
    }

    UserModel currentUser = currentUser();
    Long currentUserId = currentUser.getId();
    Long scopeOfficeId =
        officerRepository
            .findByUserIdWithPosition(currentUser.getId())
            .map(OfficeAccessService::officeIdOf)
            .orElseThrow(() -> new UnauthorizedException("Admin is not assigned to an office"));

    log.debug("currentUser={} currentUserScopeOfficeId={}", currentUserId, scopeOfficeId);
    return scopeOfficeId;
  }

  public void assertCanAccessOfficer(OfficerModel officer) {
    UserModel currentUser = currentUser();
    String currentRole = currentUser.getRole().getRoleName();
    if ("ROLE_OFFICER".equals(currentRole)) {
      OfficerModel self =
          officerRepository.findByUserIdWithPosition(currentUser.getId()).orElse(null);
      if (self == null || !self.getId().equals(officer.getId())) {
        throw new UnauthorizedException("You can only access your own officer details");
      }
      return;
    }

    Long scopeOfficeId = currentOfficeScopeIdOrNull();
    if (scopeOfficeId == null) {
      return;
    }

    Long officerOfficeId = officeIdOf(officer);
    log.debug(
        "assertCanAccessOfficer: scopeOfficeId={} officerId={} officerOfficeId={}",
        scopeOfficeId,
        officer != null ? officer.getId() : null,
        officerOfficeId);
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
