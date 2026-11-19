package ca.gc.aafc.objectstore.api.testsupport.fixtures;

import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;

public class ObjectSubTypeTestFixture {

  public static final String CREATED_BY = "user";
  private static final String AC_SUBTYPE_PREFIX = "TEST SUBTYPE ";

  public static ObjectSubtypeDto newObjectSubType() {

    ObjectSubtypeDto objectSubType = new ObjectSubtypeDto();
    objectSubType.setCreatedBy(CREATED_BY);
    objectSubType.setDcType(DcType.DATASET);
    // Unique since tests going through the web server commit the data and it is not rolled back
    objectSubType.setAcSubtype(
      AC_SUBTYPE_PREFIX + TestableEntityFactory.generateRandomNameLettersOnly(12).toUpperCase());

    return objectSubType;
  }
}
