package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.repository.external.ExternalResourceProvider;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.auth.DinaAuthorizationService;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.service.DerivativeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Repository;

import javax.validation.ValidationException;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DerivativeRepository extends DinaRepository<DerivativeDto, Derivative> {

  private final DinaAuthenticatedUser authenticatedUser;

  public DerivativeRepository(
    @NonNull DerivativeService derivativeService,
    ExternalResourceProvider externalResourceProvider,
    DinaAuthorizationService groupAuthorizationService,
    @NonNull BuildProperties buildProperties,
    @NonNull DinaAuthenticatedUser authenticatedUser,
    @NonNull ObjectMapper objMapper
  ) {
    super(
      derivativeService,
      groupAuthorizationService,
      Optional.empty(),
      new DinaMapper<>(DerivativeDto.class),
      DerivativeDto.class,
      Derivative.class,
      null,
      externalResourceProvider,
      buildProperties, objMapper);
    this.authenticatedUser = authenticatedUser;
  }

  @Override
  public <S extends DerivativeDto> S create(S resource) {
    UUID fileIdentifier = resource.getFileIdentifier();
    // File id required on submission
    if (fileIdentifier == null) {
      throw new ValidationException("fileIdentifier should be provided");
    }
    resource.setCreatedBy(authenticatedUser.getUsername());

    return super.create(resource);
  }

  /**
   * Protection against CT_CONSTRUCTOR_THROW
   */
  @Override
  protected final void finalize(){
    // no-op
  }
}
