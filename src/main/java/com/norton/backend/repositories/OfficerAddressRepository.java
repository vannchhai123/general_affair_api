package com.norton.backend.repositories;

import com.norton.backend.models.OfficerAddressModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficerAddressRepository extends JpaRepository<OfficerAddressModel, Long> {
  List<OfficerAddressModel> findByOfficer_Id(Long officerId);
}
