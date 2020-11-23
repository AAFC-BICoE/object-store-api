package ca.gc.aafc.objectstore.api.service;

import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import lombok.NonNull;

@Service
public class ObjectUploadService extends DefaultDinaService<ObjectUpload> {

  public ObjectUploadService(@NonNull BaseDAO baseDAO) {
    super(baseDAO);
  }

}
