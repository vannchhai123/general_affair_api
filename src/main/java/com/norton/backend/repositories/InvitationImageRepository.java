package com.norton.backend.repositories;

import com.norton.backend.models.InvitationImageModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationImageRepository extends JpaRepository<InvitationImageModel, Long> {

  boolean existsByInvitation_IdAndUploadImage_Id(Long invitationId, Long uploadImageId);

  void deleteByInvitation_IdAndUploadImage_Id(Long invitationId, Long uploadImageId);
}
