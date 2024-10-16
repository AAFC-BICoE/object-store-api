package ca.gc.aafc.objectstore.api.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import ca.gc.aafc.dina.validation.DinaBaseValidator;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.file.ThumbnailGenerator;

@Component
public class DerivativeValidator extends DinaBaseValidator<Derivative> {

  public static final String VALID_THUMB_DC_FORMAT_KEY = "validation.constraint.violation.requiredThumbnailDcFormat";
  public static final String VALID_THUMB_SUPPORTED_KEY = "validation.constraint.violation.thumbnailDcFormatNotSupported";

  private static final String VALID_THUMBNAIL_MEDIA_TYPE = ThumbnailGenerator.THUMB_DC_FORMAT;

  public DerivativeValidator(MessageSource messageSource) {
    super(Derivative.class, messageSource);
  }

  @Override
  public void validateTarget(Derivative target, Errors errors) {
    validateDerivativeForThumbnail(target, errors);
  }

  private void validateDerivativeForThumbnail(Derivative entity, Errors errors) {
    boolean isDerivativeForThumbnail = entity.getDerivativeType() != null
      && entity.getDerivativeType() == Derivative.DerivativeType.THUMBNAIL_IMAGE;

    if (isDerivativeForThumbnail) {
      if (StringUtils.isBlank(entity.getDcFormat())) {
        errors.reject(VALID_THUMB_DC_FORMAT_KEY, getMessage(VALID_THUMB_DC_FORMAT_KEY));
      } else {
        if (!entity.getDcFormat().equalsIgnoreCase(VALID_THUMBNAIL_MEDIA_TYPE)) {
          errors.reject(VALID_THUMB_SUPPORTED_KEY, getMessage(VALID_THUMB_SUPPORTED_KEY));
        }
      }
    }
  }
}
