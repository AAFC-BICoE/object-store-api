package ca.gc.aafc.objectstore.api.testsupport.fixtures;

import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.DcType;

public class ObjectSubTypeTestFixture {

  public static final String CREATED_BY = "user";
  public static final String AC_SUBTYPE = "EXPORT TEMPLATE";

  public static ObjectSubtypeDto newObjectSubType() {

    ObjectSubtypeDto objectSubType = new ObjectSubtypeDto();
    objectSubType.setCreatedBy(CREATED_BY);
    objectSubType.setDcType(DcType.DATASET);
    objectSubType.setAcSubtype(AC_SUBTYPE);

    return objectSubType;
  }
}
