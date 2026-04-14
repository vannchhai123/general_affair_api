package com.norton.backend.repositories;

import com.norton.backend.models.QrSessionCheckInModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrSessionCheckInRepository extends JpaRepository<QrSessionCheckInModel, Long> {}
