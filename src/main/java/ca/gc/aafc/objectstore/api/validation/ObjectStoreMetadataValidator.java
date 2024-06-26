package ca.gc.aafc.objectstore.api.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ObjectStoreMetadataValidator implements Validator {

  public static final String VALID_FILE_ID_OR_RESOURCE_EXTERNAL = "validation.constraint.violation.fileIdOrResourceExternal";
  public static final String NO_FILE_ID_OR_RESOURCE_EXTERNAL = "validation.constraint.violation.noFileIdOrResourceExternal";

  private final MessageSource messageSource;

  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return ObjectStoreMetadata.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {
    if (!supports(target.getClass())) {
      throw new IllegalArgumentException(
        "this validator only supports the " + ObjectStoreMetadata.class.getSimpleName() + " class.");
    }

    ObjectStoreMetadata entity = (ObjectStoreMetadata) target;
    checkOnlyFileIdentifierOrResourceExternalURI(errors, entity);
  }

  private void checkOnlyFileIdentifierOrResourceExternalURI(@NonNull Errors errors, @NonNull ObjectStoreMetadata entity) {
    // Report an error if both are set.
    if (entity.getFileIdentifier() != null && StringUtils.isNotBlank(entity.getResourceExternalURL())) {
      loadErrorMessageForKey(errors, VALID_FILE_ID_OR_RESOURCE_EXTERNAL);
    }

    // Report an error if none of them are setup.
    if (entity.getFileIdentifier() == null && StringUtils.isBlank(entity.getResourceExternalURL())) {
      loadErrorMessageForKey(errors, NO_FILE_ID_OR_RESOURCE_EXTERNAL);
    }
  }

  private void loadErrorMessageForKey(Errors errors, String key) {
    errors.reject(key, messageSource.getMessage(
      key,
      null,
      LocaleContextHolder.getLocale()));
  }
  
}
