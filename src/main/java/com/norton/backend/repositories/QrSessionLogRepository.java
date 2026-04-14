package com.norton.backend.repositories;

import com.norton.backend.models.QrSessionLogModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QrSessionLogRepository extends JpaRepository<QrSessionLogModel, Long> {}
