package com.norton.backend.repositories;

import com.norton.backend.enums.ShiftAssignmentScope;
import com.norton.backend.models.ShiftAssignmentModel;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignmentModel, Long> {

  List<ShiftAssignmentModel> findByScopeAndScopeIdOrderByDayOfWeekAscIdAsc(
      ShiftAssignmentScope scope, Long scopeId);

  List<ShiftAssignmentModel> findAllByOrderByScopeAscScopeIdAscDayOfWeekAscIdAsc();

  @Query(
      """
      select count(distinct sa.scopeId)
      from ShiftAssignmentModel sa
      where sa.shift.id = :shiftId and sa.scope = :scope
      """)
  long countDistinctScopeIdsByShiftIdAndScope(
      @Param("shiftId") Long shiftId, @Param("scope") ShiftAssignmentScope scope);

  @Modifying
  @Query(
      """
      delete from ShiftAssignmentModel sa
      where sa.scope = :scope
        and sa.scopeId = :scopeId
        and sa.effectiveFrom = :effectiveFrom
        and ((:effectiveTo is null and sa.effectiveTo is null) or sa.effectiveTo = :effectiveTo)
      """)
  void deleteTemplateRows(
      @Param("scope") ShiftAssignmentScope scope,
      @Param("scopeId") Long scopeId,
      @Param("effectiveFrom") LocalDate effectiveFrom,
      @Param("effectiveTo") LocalDate effectiveTo);
}
