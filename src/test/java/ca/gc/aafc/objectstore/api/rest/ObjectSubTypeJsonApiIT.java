package ca.gc.aafc.objectstore.api.rest;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.TestConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.Agent;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import ca.gc.aafc.objectstore.api.mapper.CycleAvoidingMappingContext;
import ca.gc.aafc.objectstore.api.mapper.ObjectStoreMetadataMapper;
import ca.gc.aafc.objectstore.api.mapper.ObjectSubtypeMapper;
import ca.gc.aafc.objectstore.api.testsupport.factories.AgentFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectSubtypeFactory;
import io.restassured.response.ValidatableResponse;

public class ObjectSubTypeJsonApiIT extends BaseJsonApiIntegrationTest {

  private final ObjectSubtypeMapper mapper = ObjectSubtypeMapper.INSTANCE;
  
  private ObjectSubtype objectSubtype;  
  
  @Override
  protected String getResourceUnderTest() {
    return "object-subtype";
  }

  @Override
  protected String getGetOneSchemaFilename() {
    return "getOneObjectSubtypeSchema.json";
  }

  @Override
  protected String getGetManySchemaFilename() {
    return null;
  }

  @Override
  protected Map<String, Object> buildCreateAttributeMap() {   
      
    objectSubtype = ObjectSubtypeFactory.newObjectSubtype()
       .uuid(null)
       .dcType(DcType.SOUND)
       .acSubtype("MusicalNotation")
       .build();

    ObjectSubtypeDto objectSubtypedto = mapper.toDto(objectSubtype);
    return toAttributeMap(objectSubtypedto);
  }

  @Override
  protected Map<String, Object> buildUpdateAttributeMap() {

    objectSubtype.setAcSubtype("MultimediaLearningObject");
    objectSubtype.setDcType(DcType.MOVING_IMAGE);
    ObjectSubtypeDto objectSubtypeDto = mapper.toDto(objectSubtype);
    return toAttributeMap(objectSubtypeDto);
  }
 

}
