package com.norton.backend.repositories;

import com.norton.backend.models.UserInvitationModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInvitationRepository extends JpaRepository<UserInvitationModel, Long> {

  List<UserInvitationModel> findAllByOrderByCreatedAtDesc();

  Optional<UserInvitationModel> findByEmail(String email);

  Optional<UserInvitationModel> findByToken(String token);
}
