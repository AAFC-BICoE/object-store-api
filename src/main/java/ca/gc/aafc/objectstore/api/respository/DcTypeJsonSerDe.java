package ca.gc.aafc.objectstore.api.respository;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.springframework.boot.jackson.JsonComponent;

import ca.gc.aafc.objectstore.api.entities.DcType;

public final class DcTypeJsonSerDe {
  
  // hidden constructor, utility class
  private DcTypeJsonSerDe() {}

  @JsonComponent
  public static class DcTypeDeserializer extends JsonDeserializer<DcType> {

    @Override
    public DcType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException {
      String valueAsString = jsonParser.getValueAsString();
      return DcType.fromValue(valueAsString)
          .orElseThrow(() -> new IllegalArgumentException("'" + valueAsString + "' is not a valid dc type"));
    }
  }

  @JsonComponent
  public static class DcTypeSerializer extends JsonSerializer<DcType> {

    @Override
    public void serialize(DcType value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
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
  public static Module asModule() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(DcType.class, new DcTypeSerializer());
    module.addDeserializer(DcType.class, new DcTypeDeserializer());
    return module;
  }

}
