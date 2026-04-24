package com.norton.backend.repositories;

import com.norton.backend.models.PasswordResetOtpModel;
import com.norton.backend.models.UserModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtpModel, Long> {

  void deleteByUserAndUsedFalse(UserModel user);

  Optional<PasswordResetOtpModel> findTopByUserAndUsedFalseOrderByIdDesc(UserModel user);
}
