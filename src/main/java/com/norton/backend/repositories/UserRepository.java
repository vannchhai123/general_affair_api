package com.norton.backend.repositories;

import com.norton.backend.models.UserModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
  boolean existsByUsername(String username);

  Optional<UserModel> findByUsername(String username);
}
