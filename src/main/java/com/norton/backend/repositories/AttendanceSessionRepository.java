package com.norton.backend.repositories;

import com.norton.backend.models.AttendanceSessionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSessionModel, Long> {}
