package com.norton.backend.repositories;

import com.norton.backend.models.UploadImageModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadImageRepository extends JpaRepository<UploadImageModel, Long> {}
