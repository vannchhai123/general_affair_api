package com.norton.backend.repositories;

import com.norton.backend.models.DocumentModel;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentModel, Long> {

  Optional<DocumentModel> findByUuid(UUID uuid);

  Optional<DocumentModel> findByDocumentNumber(String documentNumber);

  Page<DocumentModel> findByDirection(String direction, Pageable pageable);

  @org.springframework.data.jpa.repository.Query(
      """
      select d from DocumentModel d
      where d.direction = 'INTERNAL'
        and (lower(d.documentNumber) like lower(concat(:query, '%'))
             or lower(d.subject) like lower(concat('%', :query, '%'))
             or lower(d.summary) like lower(concat('%', :query, '%')))
      """)
  Page<DocumentModel> searchInternalDocs(
      @org.springframework.data.repository.query.Param("query") String query, Pageable pageable);

  @org.springframework.data.jpa.repository.Query(
      """
      select d from DocumentModel d
      join d.documentType dt
      where d.direction = 'INTERNAL'
        and (lower(d.documentNumber) like lower(concat(:query, '%'))
             or lower(d.subject) like lower(concat('%', :query, '%'))
             or lower(d.summary) like lower(concat('%', :query, '%')))
        and (lower(dt.name) = lower(:docType)
             or lower(dt.code) = lower(:docType)
             or lower(dt.name) like lower(concat('%', :docType, '%')))
      """)
  Page<DocumentModel> searchInternalDocsByType(
      @org.springframework.data.repository.query.Param("docType") String docType,
      @org.springframework.data.repository.query.Param("query") String query,
      Pageable pageable);

  @org.springframework.data.jpa.repository.Query(
      """
      select d from DocumentModel d
      where d.direction = 'INTERNAL'
        and d.documentDate >= :startDate
        and d.documentDate <= :endDate
      """)
  Page<DocumentModel> findInternalDocsByDateRange(
      @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
      @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate,
      Pageable pageable);
}
