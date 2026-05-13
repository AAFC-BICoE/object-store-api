package ca.gc.aafc.objectstore.api.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.OffsetDateTime;
import lombok.Data;

/**
 * Internal structure to record information about the system to ease
 * migrations between versions.
 */
@Entity
@Table(name = "migrations_hints")
@Data
public class MigrationHint {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "hint_key", nullable = false, unique = true)
  private String hintKey;

  @Column(name = "created_on")
  private OffsetDateTime createdOn;
}
