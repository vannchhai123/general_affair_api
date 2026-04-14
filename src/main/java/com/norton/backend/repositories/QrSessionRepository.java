package com.norton.backend.repositories;

import com.norton.backend.models.QrSessionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrSessionRepository extends JpaRepository<QrSessionModel, Long> {}
