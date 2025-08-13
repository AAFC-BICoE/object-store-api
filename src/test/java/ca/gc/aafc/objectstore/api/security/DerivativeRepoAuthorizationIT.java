package ca.gc.aafc.objectstore.api.security;

import ca.gc.aafc.dina.exception.ResourceGoneException;
import ca.gc.aafc.dina.exception.ResourceNotFoundException;
import ca.gc.aafc.dina.jsonapi.JsonApiDocument;
import ca.gc.aafc.dina.repository.JsonApiModelAssistant;
import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.repository.DerivativeRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import ca.gc.aafc.objectstore.api.testsupport.fixtures.DerivativeTestFixture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import javax.inject.Inject;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "keycloak.enabled=true")
public class DerivativeRepoAuthorizationIT extends BaseIntegrationTest {

  @Inject
  private DerivativeRepository derivativeRepository;

  private static final String GROUP_1 = "CNC";
  private ObjectUpload uploadTest_1;
  private ObjectStoreMetadata acDerivedFrom;

  @BeforeEach
  void setUp() {
    uploadTest_1 = ObjectUploadFactory.newObjectUpload()
      .isDerivative(true)
      .bucket(GROUP_1)
      .evaluatedFileExtension(MediaType.IMAGE_JPEG_VALUE)
      .build();

    objectUploadService.create(uploadTest_1);

    ObjectUpload uploadTest_2 = ObjectUploadFactory.newObjectUpload().build();

    objectUploadService.create(uploadTest_2);

    acDerivedFrom = ObjectStoreMetadataFactory.newObjectStoreMetadata()
      .fileIdentifier(uploadTest_2.getFileIdentifier()).build();
    objectStoreMetaDataService.create(acDerivedFrom);
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"CNC:USER"})
  void create_WithValidGroup_DerivativeCreated()
    throws ResourceGoneException, ResourceNotFoundException {
    DerivativeDto dto = newDerivative(uploadTest_1.getFileIdentifier());
    JsonApiDocument docToCreate = DerivativeTestFixture.newJsonApiDocument(dto);

    UUID uuid = JsonApiModelAssistant.extractUUIDFromRepresentationModelLink(derivativeRepository.onCreate(docToCreate));
    assertNotNull(derivativeRepository.getOne(uuid, "").getDto());
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"INVALID_GROUP:USER"})
  void create_WithInvalidGroup_ThrowsAccessDenied() {
    DerivativeDto dto = newDerivative(uploadTest_1.getFileIdentifier());
    JsonApiDocument docToCreate = DerivativeTestFixture.newJsonApiDocument(dto);

    assertThrows(AccessDeniedException.class, () ->
      derivativeRepository.create(docToCreate, null));
  }

  private DerivativeDto newDerivative(UUID fileIdentifier) {
    DerivativeDto dto = new DerivativeDto();
    dto.setDcType(DcType.IMAGE);
    ObjectStoreMetadataDto from = new ObjectStoreMetadataDto();
    from.setUuid(acDerivedFrom.getUuid());
    dto.setAcDerivedFrom(from);
    dto.setBucket(GROUP_1);
    dto.setDerivativeType(Derivative.DerivativeType.THUMBNAIL_IMAGE);
    dto.setFileIdentifier(fileIdentifier);
    return dto;
  }
}
