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
@Table(name = "offices")
public class DepartmentModel extends BaseIdModel {

  @Column(name = "uuid", unique = true, length = 36)
  private String uuid;

  @NotBlank(message = "Office name is required")
  @Size(max = 100, message = "Office name must not exceed 100 characters")
  @Column(nullable = false, unique = true, length = 100)
  private String name;

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
