package ca.gc.aafc.objectstore.api.repository;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import org.springframework.boot.jackson.JacksonComponent;

import ca.gc.aafc.objectstore.api.entities.DcType;

public final class DcTypeJsonSerDe {
  
  // hidden constructor, utility class
  private DcTypeJsonSerDe() { }

  @JacksonComponent
  public static class DcTypeDeserializer extends ValueDeserializer<DcType> {

    @Override
    public DcType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws JacksonException {
      String valueAsString = jsonParser.getValueAsString();
      return DcType.fromValue(valueAsString)
          .orElseThrow(() -> new IllegalArgumentException("'" + valueAsString + "' is not a valid dc type"));
    }
  }

  @JacksonComponent
  public static class DcTypeSerializer extends ValueSerializer<DcType> {

    @Override
    public void serialize(DcType value, JsonGenerator gen, SerializationContext serializers)
        throws JacksonException {
      if (value != null) {
        gen.writeString(value.name());
      } else {
        gen.writeNull();
      }
    }
  }

  /**
   * Return the DcType json Serializer and Deserializer as Module. If at some point we add more
   * SerDe, the should be removed in favor of an ObjectStoreModule.
   * 
   * @return
   */
  public static JacksonModule asModule() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(DcType.class, new DcTypeSerializer());
    module.addDeserializer(DcType.class, new DcTypeDeserializer());
    return module;
  }

}
