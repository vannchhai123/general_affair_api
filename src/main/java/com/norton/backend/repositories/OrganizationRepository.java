package com.norton.backend.repositories;

import com.norton.backend.models.OrganizationModel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationModel, Long> {
  Optional<OrganizationModel> findByUuid(UUID uuid);

  Optional<OrganizationModel> findByName(String name);
}
