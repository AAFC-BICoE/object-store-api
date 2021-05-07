package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.repository.external.ExternalResourceProvider;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.service.DerivativeService;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.MethodNotAllowedException;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import javax.validation.ValidationException;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DerivativeRepository extends DinaRepository<DerivativeDto, Derivative> {

  private final DerivativeService derivativeService;
  private final DinaAuthenticatedUser authenticatedUser;

  public DerivativeRepository(
    @NonNull DerivativeService derivativeService,
    ExternalResourceProvider externalResourceProvider,
    @NonNull BuildProperties buildProperties,
    @NonNull DinaAuthenticatedUser authenticatedUser
  ) {
    super(
      derivativeService,
      Optional.empty(),
      Optional.empty(),
      new DinaMapper<>(DerivativeDto.class),
      DerivativeDto.class,
      Derivative.class,
      null,
      externalResourceProvider,
      buildProperties);
    this.derivativeService = derivativeService;
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public <S extends DerivativeDto> S create(S resource) {
    UUID fileIdentifier = resource.getFileIdentifier();
    // File id required on submission
    if (fileIdentifier == null) {
      throw new ValidationException("fileIdentifier should be provided");
    }

    ObjectUpload objectUpload = derivativeService.findOne(
      fileIdentifier,
      ObjectUpload.class);

    // Object Upload must be present, signals a real file has been previously uploaded.
    if (objectUpload == null) {
      throw new ValidationException("Upload with fileIdentifier:" + fileIdentifier + " not found");
    }

    // Object Upload must be a derivative
    if (!objectUpload.getIsDerivative()) {
      throw new BadRequestException("Upload with fileIdentifier:" + fileIdentifier + " is not a derivative");
    }

    // Auto populated fields based on object upload for given File Id
    resource.setFileExtension(objectUpload.getEvaluatedFileExtension());
    resource.setAcHashValue(objectUpload.getSha1Hex());
    resource.setAcHashFunction(FileController.DIGEST_ALGORITHM);
    resource.setBucket(objectUpload.getBucket());
    if (StringUtils.isBlank(resource.getDcFormat())) { // Auto populate if not submitted
      resource.setDcFormat(objectUpload.getEvaluatedMediaType());
    }
    resource.setCreatedBy(authenticatedUser.getUsername());

    return super.create(resource);
  }

  @Override
  public <S extends DerivativeDto> S save(S resource) {
    throw new MethodNotAllowedException("Updating a derivative is prohibited");
  }

}
