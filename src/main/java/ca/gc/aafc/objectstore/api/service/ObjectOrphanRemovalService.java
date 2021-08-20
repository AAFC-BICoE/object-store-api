package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import java.util.Collections;
import java.util.List;

@Service
public class ObjectOrphanRemovalService extends DefaultDinaService<ObjectUpload> {

  public ObjectOrphanRemovalService(
    @NonNull BaseDAO baseDAO,
    @NonNull SmartValidator validator
  ) {
    super(baseDAO, validator);
  }

  public void removeObjectOrphans() {
    List<ObjectUpload> orphans = findOrphans();
    orphans.forEach(objectUpload -> {
      if (checkAge(objectUpload)) {
        deleteUpload(objectUpload);
        deleteMinioFile(objectUpload);
      }
    });
  }

  private List<ObjectUpload> findOrphans() {
    return Collections.emptyList();
  }

  private boolean checkAge(ObjectUpload objectUpload) {
    return false;
  }

  private void deleteUpload(ObjectUpload objectUpload) {

  }

  private void deleteMinioFile(ObjectUpload objectUpload) {

  }

}
