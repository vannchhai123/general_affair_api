package com.norton.backend.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class UploadResourceConfig implements WebMvcConfigurer {

  private final FileStorageProperties fileStorageProperties;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path uploadPath = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

    registry.addResourceHandler("/uploads/**").addResourceLocations(uploadPath.toUri().toString());
  }
}
