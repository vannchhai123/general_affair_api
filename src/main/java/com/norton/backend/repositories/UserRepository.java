package com.norton.backend.repositories;

import com.norton.backend.models.UserModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, Long id);

  boolean existsByUsernameAndIdNot(String username, Long id);

  Optional<UserModel> findByUsername(String username);

  Optional<UserModel> findByEmailIgnoreCase(String email);

  long countByUserStatus(com.norton.backend.enums.UserStatus userStatus);

  @org.springframework.data.jpa.repository.Query(
      "SELECT u FROM UserModel u WHERE "
          + "(:keyword IS NULL OR :keyword = '' "
          + "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) "
          + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) "
          + "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  org.springframework.data.domain.Page<UserModel> searchUsers(
      @org.springframework.data.repository.query.Param("keyword") String keyword,
      org.springframework.data.domain.Pageable pageable);
}
