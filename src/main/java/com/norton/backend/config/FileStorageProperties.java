package com.norton.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {
  private String uploadDir = "uploads/images";
  private String baseUrl = "http://localhost:8080/uploads";
}
