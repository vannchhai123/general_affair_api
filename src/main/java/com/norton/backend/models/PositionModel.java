package com.norton.backend.models;

import com.norton.backend.enums.PositionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "position")
public class PositionModel extends BaseIdModel {

  @Column(name = "uuid", unique = true, length = 36)
  private String uuid;

  @NotBlank(message = "Position name is required")
  @Size(max = 100, message = "Position name must not exceed 100 characters")
  @Column(nullable = false, length = 100)
  private String name;

  @Size(max = 50, message = "Position code must not exceed 50 characters")
  @Column(name = "code", length = 50, unique = true)
  private String code;

  @NotNull(message = "Department is required")
  @ManyToOne
  @JoinColumn(name = "department_id", nullable = false)
  private DepartmentModel department;

  private String description;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private PositionStatus status;

  @OneToMany(mappedBy = "position", cascade = CascadeType.ALL)
  private List<OfficerModel> officers;

  @PrePersist
  public void generateUuid() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID().toString();
    }
  }
}
