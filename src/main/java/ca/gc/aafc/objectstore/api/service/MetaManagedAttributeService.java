package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.validation.MetadataManagedAttributeValidator;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import java.util.Optional;

@Service
public class MetaManagedAttributeService extends DefaultDinaService<MetadataManagedAttribute> {

  private final MetadataManagedAttributeValidator metadataManagedAttributeValidator;

  public MetaManagedAttributeService(@NonNull BaseDAO baseDAO, @NonNull MetadataManagedAttributeValidator metadataManagedAttributeValidator) {
    super(baseDAO);
    this.metadataManagedAttributeValidator = metadataManagedAttributeValidator;
  }

  public void validateMetaManagedAttribute(MetadataManagedAttribute entity) {
    Errors errors = new BeanPropertyBindingResult(entity, entity.getUuid().toString());
    metadataManagedAttributeValidator.validate(entity, errors);

    if (!errors.hasErrors()) {
      return;
    }

    Optional<String> errorMsg = errors.getAllErrors().stream().map(ObjectError::getDefaultMessage).findAny();
    errorMsg.ifPresent(msg -> {
      throw new IllegalArgumentException(msg);
    });
  }

  @Override
  protected void preCreate(MetadataManagedAttribute entity) {
    validateMetaManagedAttribute(entity);
  }

  @Override
  protected void preUpdate(MetadataManagedAttribute entity) {
    validateMetaManagedAttribute(entity);
  }

}
