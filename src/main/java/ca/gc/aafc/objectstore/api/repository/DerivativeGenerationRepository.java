package ca.gc.aafc.objectstore.api.repository;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.objectstore.api.dto.DerivativeGenerationDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.service.DerivativeGenerationService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetaDataService;

import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public class DerivativeGenerationRepository implements
  ResourceRepository<DerivativeGenerationDto, Serializable> {

  private final ObjectStoreMetaDataService metadataService;
  private final DerivativeGenerationService derivativeGenerationService;

  public DerivativeGenerationRepository(ObjectStoreMetaDataService metadataService,
                                        DerivativeGenerationService derivativeGenerationService) {
    this.metadataService = metadataService;
    this.derivativeGenerationService = derivativeGenerationService;
  }

  @Override
  public <S extends DerivativeGenerationDto> S create(S s) {

    if(s.getDerivativeType() != Derivative.DerivativeType.THUMBNAIL_IMAGE) {
      throw new IllegalArgumentException("DerivativeType can only be THUMBNAIL_IMAGE");
    }

    ObjectStoreMetadata metadata = metadataService.findOne(s.getMetadataUuid(),
      ObjectStoreMetadata.class, Set.of(ObjectStoreMetadata.DERIVATIVES_PROP));

    // Check if we have a thumbnail entity already
    Optional<Derivative> existingThumbnail = metadata.getDerivatives().stream()
      .filter(d -> d.getDerivativeType().equals(Derivative.DerivativeType.THUMBNAIL_IMAGE))
      .findFirst();

    // if the thumbnail entity exists, try to fix and return.
    if(existingThumbnail.isPresent()) {
      derivativeGenerationService.fixIncompleteThumbnail(existingThumbnail.get());
      s.setUuid(existingThumbnail.get().getUuid());
      return s;
    }

    // Check the source to use for the thumbnail
    if(s.getDerivedFromType() != null) {
      Derivative derivative = metadata.getDerivatives().stream()
        .filter(d -> d.getDerivativeType().equals(s.getDerivedFromType()))
        .findFirst().orElseThrow(() -> new IllegalStateException("DerivedFromType not found for metadata"));
      derivativeGenerationService.handleThumbnailGeneration(derivative);
    } else {
      derivativeGenerationService.handleThumbnailGeneration(metadata);
    }

    metadataService.refresh(metadata);

    Optional<Derivative> createdThumbnail = metadata.getDerivatives().stream()
      .filter(d -> d.getDerivativeType().equals(Derivative.DerivativeType.THUMBNAIL_IMAGE))
      .findFirst();

    s.setUuid(createdThumbnail.map(Derivative::getUuid).orElse(null));
    return s;
  }

  @Override
  public Class<DerivativeGenerationDto> getResourceClass() {
    return DerivativeGenerationDto.class;
  }

  @Override
  public DerivativeGenerationDto findOne(Serializable serializable, QuerySpec querySpec) {
    return null;
  }

  @Override
  public ResourceList<DerivativeGenerationDto> findAll(QuerySpec querySpec) {
    throw new MethodNotAllowedException("GET");
  }

  @Override
  public ResourceList<DerivativeGenerationDto> findAll(Collection<Serializable> collection, QuerySpec querySpec) {
    throw new MethodNotAllowedException("GET");
  }

  @Override
  public <S extends DerivativeGenerationDto> S save(S s) {
    throw new MethodNotAllowedException("PUT/PATCH");
  }

  @Override
  public void delete(Serializable serializable) {
    throw new MethodNotAllowedException("DELETE");
  }
}

