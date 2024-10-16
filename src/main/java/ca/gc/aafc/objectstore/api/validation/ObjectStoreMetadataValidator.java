package ca.gc.aafc.objectstore.api.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import ca.gc.aafc.dina.validation.DinaBaseValidator;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

@Component
public class ObjectStoreMetadataValidator extends DinaBaseValidator<ObjectStoreMetadata> {

  public static final String VALID_FILE_ID_OR_RESOURCE_EXTERNAL = "validation.constraint.violation.fileIdOrResourceExternal";
  public static final String NO_FILE_ID_OR_RESOURCE_EXTERNAL = "validation.constraint.violation.noFileIdOrResourceExternal";

  public ObjectStoreMetadataValidator(MessageSource messageSource) {
    super(ObjectStoreMetadata.class, messageSource);
  }

  @Override
  public void validateTarget(ObjectStoreMetadata target, Errors errors) {
    checkOnlyFileIdentifierOrResourceExternalURI(target, errors);
  }

  private void checkOnlyFileIdentifierOrResourceExternalURI(ObjectStoreMetadata entity, Errors errors) {
    // Report an error if both are set.
    if (entity.getFileIdentifier() != null && StringUtils.isNotBlank(entity.getResourceExternalURL())) {
      errors.reject(VALID_FILE_ID_OR_RESOURCE_EXTERNAL, getMessage(VALID_FILE_ID_OR_RESOURCE_EXTERNAL));
    }

    // Report an error if none of them are setup.
    if (entity.getFileIdentifier() == null && StringUtils.isBlank(entity.getResourceExternalURL())) {
      errors.reject(NO_FILE_ID_OR_RESOURCE_EXTERNAL, getMessage(NO_FILE_ID_OR_RESOURCE_EXTERNAL));
    }
  }
}
