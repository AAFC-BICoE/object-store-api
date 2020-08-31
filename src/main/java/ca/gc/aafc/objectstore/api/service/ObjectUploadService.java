package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class ObjectUploadService extends DinaService<ObjectUpload> {

  public ObjectUploadService(@NonNull BaseDAO baseDAO) {
    super(baseDAO);
  }
}
