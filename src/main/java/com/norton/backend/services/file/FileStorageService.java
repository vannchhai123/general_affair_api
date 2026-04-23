package com.norton.backend.services.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
  String storeImage(MultipartFile file);
}
