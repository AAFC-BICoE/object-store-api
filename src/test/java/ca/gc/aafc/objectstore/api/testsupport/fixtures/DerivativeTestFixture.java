package ca.gc.aafc.objectstore.api.testsupport.fixtures;

import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.jsonapi.JsonApiDocuments;
import ca.gc.aafc.dina.testsupport.jsonapi.JsonAPITestHelper;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

public class DerivativeTestFixture {

  public static final String CREATED_BY = "user";

  public static DerivativeDto newDerivative(UUID fileIdentifier) {
    DerivativeDto dto = new DerivativeDto();
    dto.setDcType(DcType.IMAGE);
    dto.setDerivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE);
    dto.setFileExtension(".jpg");
    dto.setAcHashFunction("abcFunction");
    dto.setAcHashValue("abc");
    dto.setDcFormat(MediaType.IMAGE_JPEG_VALUE);
    dto.setFileIdentifier(fileIdentifier);
    dto.setCreatedBy(CREATED_BY);
    dto.setPubliclyReleasable(true);
    dto.setNotPubliclyReleasableReason("Classified");
    dto.setAcTags(new String[] {"tag 1"});
    return dto;
  }

  /**
   * Will also set acDerivedFrom from dto.getAcDerivedFrom()
   * @param dto
   * @return
   */
  public static JsonApiDocument newJsonApiDocument(DerivativeDto dto) {
    return JsonApiDocuments.createJsonApiDocumentWithRelToOne(
      dto.getJsonApiId(), dto.getJsonApiType(),
      JsonAPITestHelper.toAttributeMap(dto),
      Map.of("acDerivedFrom", JsonApiDocument.ResourceIdentifier.builder()
        .type(ObjectStoreMetadataDto.TYPENAME).id(dto.getAcDerivedFrom().getUuid()).build())
    );
  }
}
