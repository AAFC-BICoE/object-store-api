package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.testsupport.security.WithMockKeycloakUser;
import ca.gc.aafc.objectstore.api.BaseIntegrationTest;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectUploadFactory;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import javax.inject.Inject;
import java.util.UUID;

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
  @WithMockKeycloakUser(groupRole = {"CNC:STAFF"})
  void create_WithValidGroup_DerivativeCreated() {
    Assertions.assertNotNull(derivativeRepository.findOne(derivativeRepository.create(newDerivative(
      uploadTest_1.getFileIdentifier())).getUuid(), new QuerySpec(DerivativeDto.class)));
  }

  @Test
  @WithMockKeycloakUser(groupRole = {"INVALID_GROUP:STAFF"})
  void create_WithInvalidGroup_ThrowsAccessDenied() {
    Assertions.assertThrows(AccessDeniedException.class, () ->
      derivativeRepository.create(newDerivative(uploadTest_1.getFileIdentifier())));
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
