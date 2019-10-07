package ca.gc.aafc.objectstore.api.dto;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata.DcType;

@JsonComponent
public class DcTypeSerializer extends JsonSerializer<DcType> {

  @Override
  public void serialize(DcType value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    if(value != null) {
      gen.writeString(value.getValue());
    }
    else {
      gen.writeNull();
    }
    
  }

}
