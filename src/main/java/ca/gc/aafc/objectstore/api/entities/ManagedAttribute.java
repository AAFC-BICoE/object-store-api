package ca.gc.aafc.objectstore.api.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@TypeDefs({
    @TypeDef(name = "pgsql_enum", typeClass = PostgreSQLEnumType.class),
    @TypeDef(name = "string-array", typeClass = StringArrayType.class) 
})
@AllArgsConstructor
@Builder
@RequiredArgsConstructor
@SuppressFBWarnings(justification = "ok for Hibernate Entity", value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class ManagedAttribute {

  private Integer id;
  private String name;
  private ManagedAttributeType managedAttributeType;

  private String[] acceptedValues;

  public enum ManagedAttributeType {
    INTEGER, STRING
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @NotNull
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @NotNull
  @Type(type = "pgsql_enum")
  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  public ManagedAttributeType getManagedAttributeType() {
    return managedAttributeType;
  }

  public void setManagedAttributeType(ManagedAttributeType type) {
    this.managedAttributeType = type;
  }

  @Type(type = "string-array")
  @Column(columnDefinition = "text[]")
  public String[] getAcceptedValues() {
    return acceptedValues;
  }

  public void setAcceptedValues(String[] acceptedValues) {
    this.acceptedValues = acceptedValues;
  }

}
