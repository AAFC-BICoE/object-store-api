package ca.gc.aafc.objectstore.api.crud;

import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;

import java.util.UUID;

public class DerivativeCRUDIT extends BaseEntityCRUDIT {

  @Override
  public void testSave() {
    Derivative derivative = Derivative.builder()
      .uuid(UUID.randomUUID())
      .fileIdentifier(UUID.randomUUID())
      .fileExtension(".jpg")
      .bucket("mybucket")
      .acHashValue("abc")
      .dcType(DcType.IMAGE)
      .createdBy(RandomStringUtils.random(4))
      .build();
    service.save(derivative);
    Assertions.assertNotNull(derivative.getCreatedOn());
    Assertions.assertNotNull(derivative.getId());
  }

  @Override
  public void testFind() {

  }

  @Override
  public void testRemove() {

  }

}
