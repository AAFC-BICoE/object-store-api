package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.validation.MetadataManagedAttributeValidator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import javax.inject.Inject;

@Service
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
public class MetaManagedAttributeService extends DefaultDinaService<MetadataManagedAttribute> {

  @Inject
  private MetadataManagedAttributeValidator metadataManagedAttributeValidator;

  public MetaManagedAttributeService(@NonNull BaseDAO baseDAO,
      @NonNull ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService) {
    super(baseDAO);
  }

  public void validateMetaManagedAttribute(MetadataManagedAttribute entity) {
    Errors errors = new BeanPropertyBindingResult(entity, entity.getUuid().toString());
    metadataManagedAttributeValidator.validate(entity, errors);
    if (errors != null) {
      String errorMsg = errors.getFieldError() != null ? errors.getFieldError().getDefaultMessage()
            : errors.getAllErrors() != null && errors.getAllErrors().size() > 0
                ? errors.getAllErrors().get(0).getDefaultMessage()
                : null;
      if (!StringUtils.isEmpty(errorMsg)) {
        throw new IllegalArgumentException(errorMsg);
      }
    }
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
