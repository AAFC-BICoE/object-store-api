package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.repository.external.ExternalResourceProvider;
import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.service.DerivativeService;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;

import java.util.Optional;

public class DerivativeRepository  extends DinaRepository<DerivativeDto, Derivative> {
  public DerivativeRepository(
    @NonNull DerivativeService derivativeService,
    ExternalResourceProvider externalResourceProvider,
    @NonNull BuildProperties buildProperties
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
  }
}
