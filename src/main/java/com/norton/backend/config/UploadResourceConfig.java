package com.norton.backend.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class UploadResourceConfig implements WebMvcConfigurer {

  private final FileStorageProperties fileStorageProperties;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path uploadPath = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
    String uploadUri = uploadPath.toUri().toString();
    if (!uploadUri.endsWith("/")) {
      uploadUri += "/";
    }
    String filePathLocation = "file:" + uploadPath.toString().replace('\\', '/') + "/";

    registry.addResourceHandler("/uploads/**").addResourceLocations(uploadUri, filePathLocation);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOriginPatterns(
            "http://localhost:[*]", "http://127.0.0.1:[*]", "https://*.vercel.app", "*")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}
