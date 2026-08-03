package ca.gc.aafc.objectstore.api.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.ControlledVocabularyService;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreControlledVocabulary;

@Service
public class ObjectStoreControlledVocabularyService extends ControlledVocabularyService<ObjectStoreControlledVocabulary> {

  public ObjectStoreControlledVocabularyService(BaseDAO baseDAO,
                                               SmartValidator smartValidator) {
    super(baseDAO, smartValidator, ObjectStoreControlledVocabulary.class);
  }
}
