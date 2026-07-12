package com.norton.backend.repositories;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class DocumentRepositoryTest {

  @Test
  void repositoryExposesTypeAwareSearchMethod() {
    DocumentRepository repository =
        (DocumentRepository)
            Proxy.newProxyInstance(
                DocumentRepository.class.getClassLoader(),
                new Class<?>[] {DocumentRepository.class},
                (proxy, method, args) -> {
                  if ("searchInternalDocsByType".equals(method.getName())) {
                    return org.springframework.data.domain.Page.empty(
                        (org.springframework.data.domain.Pageable) args[2]);
                  }
                  return null;
                });

    assertDoesNotThrow(
        () -> repository.searchInternalDocsByType("memo", "NU-MEMO", PageRequest.of(0, 20)));
  }
}
