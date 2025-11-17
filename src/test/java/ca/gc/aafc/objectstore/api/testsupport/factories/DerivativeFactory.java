package ca.gc.aafc.objectstore.api.testsupport.factories;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.MediaType;

import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.dina.util.UUIDHelper;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

public class DerivativeFactory implements TestableEntityFactory<Derivative> {

  @Override
  public Derivative getEntityInstance() {
    return newDerivative(null, null).build();
  }

  public static Derivative.DerivativeBuilder<?,?> newDerivative(ObjectStoreMetadata derivedFrom, UUID fileIdentifier) {
    return Derivative.builder()
      .uuid(UUIDHelper.generateUUIDv7())
      .fileIdentifier(fileIdentifier)
      .fileExtension(".jpg")
      .bucket("mybucket")
      .dcFormat(MediaType.IMAGE_JPEG_VALUE)
      .acHashValue("abc")
      .acHashFunction("abcFunction")
      .dcType(DcType.IMAGE)
      .createdBy(RandomStringUtils.random(4))
      .acDerivedFrom(derivedFrom)
      .derivativeType(Derivative.DerivativeType.LARGE_IMAGE);
  }

}
