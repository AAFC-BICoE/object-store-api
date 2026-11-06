package ca.gc.aafc.objectstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import ca.gc.aafc.objectstore.api.entities.DcType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unit tests related to {@link DcTypeJsonSerDe}.
 *
 */
public class DcTypeJsonSerDeTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  public void before() {
    objectMapper = JsonMapper.builder()
        .addModule(DcTypeJsonSerDe.asModule())
        .build();
  }

  @Test
  public void serialization_onDcType_jsonContainsValue() throws JacksonException {
    DcTypeJsonSerDeTestClass tesObject = new DcTypeJsonSerDeTestClass("a", DcType.IMAGE);
    assertTrue(objectMapper.writeValueAsString(tesObject).contains(DcType.IMAGE.name()));
  }

  @Test
  public void deserialization_onValidString_matchingDcTypeReturned()
      throws JacksonException {
    DcTypeJsonSerDeTestClass tesObject = objectMapper.readValue("{\"a\":\"a\",\"b\":\"IMAGE\"}",
        DcTypeJsonSerDeTestClass.class);
    assertEquals(DcType.IMAGE, tesObject.getB());
  }

  @Test
  public void deserialization_onInvalidDcTypeString_IllegalArgumentException() {
    Throwable rootCause = null;
    try {
      objectMapper.readValue("{\"a\":\"a\",\"b\":\"EGAMI\"}", DcTypeJsonSerDeTestClass.class);
    } catch (JacksonException e) {
      rootCause = e.getCause();
    }
    assertEquals(IllegalArgumentException.class, rootCause.getClass());
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  static class DcTypeJsonSerDeTestClass {
    private String a;
    private DcType b;
  }

}
