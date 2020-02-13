package ca.gc.aafc.objectstore.api.interfaces;

import java.time.OffsetDateTime;

public interface SoftDeletable {
  
  String DELETED_DATE_FIELD_NAME = "deletedDate";

  OffsetDateTime getDeletedDate();

  void setDeletedDate(OffsetDateTime deletedDate);

}
