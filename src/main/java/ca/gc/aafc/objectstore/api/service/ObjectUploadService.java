package ca.gc.aafc.objectstore.api.service;

import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.service.DefaultDinaService;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.exif.ExifParser;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.SmartValidator;

@Service
public class ObjectUploadService extends DefaultDinaService<ObjectUpload> {

  private final ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService;
  private final BaseDAO baseDAO;

  public ObjectUploadService(
    @NonNull BaseDAO baseDAO,
    @NonNull ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService,
    @NonNull SmartValidator smartValidator
  ) {
    super(baseDAO, smartValidator);
    this.baseDAO = baseDAO;
    this.defaultValueSetterService = defaultValueSetterService;
  }

  @Override
  protected void preCreate(ObjectUpload entity) {
    if (entity.getExif() != null && !entity.getExif().isEmpty()) {
      ExifParser.parseDateTaken(entity.getExif())
        .ifPresent(dtd -> entity.setDateTimeDigitized(dtd.toString()));
    }

    entity.setDcType(defaultValueSetterService.dcTypeFromDcFormat(entity.getDetectedMediaType()));
  }

  //TODO: replace by defaultDinaService existsByProperty
  public boolean existsByProperty(String property, Object value) {
    return baseDAO.existsByProperty(ObjectUpload.class, property, value);
  }

}
