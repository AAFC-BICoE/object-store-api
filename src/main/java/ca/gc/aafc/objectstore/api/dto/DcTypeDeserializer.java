package ca.gc.aafc.objectstore.api.dto;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata.DcType;

@JsonComponent
public class DcTypeDeserializer extends JsonDeserializer<DcType> {

    @Override
    public DcType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
      return DcType.fromValue(jsonParser.getValueAsString()).orElse(null);
    }

}
