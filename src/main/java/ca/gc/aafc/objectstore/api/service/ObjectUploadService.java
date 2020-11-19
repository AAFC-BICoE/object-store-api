package ca.gc.aafc.objectstore.api.service;

import javax.transaction.Transactional;

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

  @Transactional
  @Override
  public ObjectUpload create(ObjectUpload entity) {
    return super.create(entity);
  }
  
  @Transactional
  @Override
  public ObjectUpload update(ObjectUpload entity) {
    return super.update(entity);
  }
  
  @Transactional
  @Override
  public <T> T findOne(Object naturalId, Class<T> entityClass) {
    return super.findOne(naturalId, entityClass);
  }

}
