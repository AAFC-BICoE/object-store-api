package ca.gc.aafc.objecstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.respository.DcTypeJsonSerDe;
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
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(DcTypeJsonSerDe.asModule());
  }

  @Test
  public void serialization_onDcType_jsonContainsValue() throws JsonProcessingException {
    DcTypeJsonSerDeTestClass tesObject = new DcTypeJsonSerDeTestClass("a", DcType.IMAGE);
    assertTrue(objectMapper.writeValueAsString(tesObject).contains(DcType.IMAGE.name()));
  }

  @Test
  public void deserialization_onValidString_matchingDcTypeReturned()
      throws JsonProcessingException {
    DcTypeJsonSerDeTestClass tesObject = objectMapper.readValue("{\"a\":\"a\",\"b\":\"IMAGE\"}",
        DcTypeJsonSerDeTestClass.class);
    assertEquals(DcType.IMAGE, tesObject.getB());
  }

  @Test
  public void deserialization_onInvalidDcTypeString_IllegalArgumentException() {
    Throwable rootCause = null;
    try {
      objectMapper.readValue("{\"a\":\"a\",\"b\":\"EGAMI\"}", DcTypeJsonSerDeTestClass.class);
    } catch (JsonProcessingException e) {
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
