package com.norton.backend.models;

import com.norton.backend.enums.DepartmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "department")
public class DepartmentModel extends BaseIdModel {

  @Column(name = "uuid", unique = true, length = 36)
  private String uuid;

  @NotBlank(message = "Department name is required")
  @Size(max = 100, message = "Department name must not exceed 100 characters")
  @Column(nullable = false, length = 100)
  private String name;

  @Size(max = 50, message = "Department code must not exceed 50 characters")
  @Column(name = "code", length = 50, unique = true)
  private String code;

  @Size(max = 255, message = "Manager must not exceed 255 characters")
  @Column(name = "manager", length = 255)
  private String manager;

  @Size(max = 500, message = "Description must not exceed 500 characters")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private DepartmentStatus status;

  @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
  private List<PositionModel> positions;

  @PrePersist
  public void generateUuid() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID().toString();
    }
  }
}
