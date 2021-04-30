package ca.gc.aafc.objectstore.api.validation;

import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.file.ThumbnailGenerator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class DerivativeValidator implements Validator {

  public static final String VALID_THUMB_DC_FORMAT_KEY = "validation.constraint.violation.requiredThumbnailDcFormat";
  public static final String VALID_THUMB_SUPPORTED_KEY = "validation.constraint.violation.thumbnailDcFormatNotSupported";

  private final MessageSource messageSource;

  @Override
  public boolean supports(@NonNull Class<?> aClass) {
    return Derivative.class.isAssignableFrom(aClass);
  }

  @Override
  public void validate(@NonNull Object o, @NonNull Errors errors) {
    if (!supports(o.getClass())) {
      throw new IllegalArgumentException(
        "this validator only supports the " + Derivative.class.getSimpleName() + " class.");
    }

    Derivative entity = (Derivative) o;

    validateDerivativeForThumbnail(errors, entity);
  }

  private void validateDerivativeForThumbnail(@NonNull Errors errors, @NonNull Derivative entity) {
    boolean isDerivativeForThumbnail = entity.getDerivativeType() != null
      && entity.getDerivativeType() == Derivative.DerivativeType.THUMBNAIL_IMAGE;

    if (isDerivativeForThumbnail) {
      if (StringUtils.isBlank(entity.getDcFormat())) {
        loadErrorMessageForKey(errors, VALID_THUMB_DC_FORMAT_KEY);
      } else {
        if (!ThumbnailGenerator.isSupported(entity.getDcFormat())) {
          loadErrorMessageForKey(errors, VALID_THUMB_SUPPORTED_KEY);
        }
      }
    }
  }

  private void loadErrorMessageForKey(Errors errors, String key) {
    errors.reject(key, messageSource.getMessage(
      VALID_THUMB_DC_FORMAT_KEY,
      null,
      LocaleContextHolder.getLocale()));
  }
}
