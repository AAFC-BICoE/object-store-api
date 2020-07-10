package ca.gc.aafc.objectstore.api.respository;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.MetadataManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import lombok.NonNull;

@Repository
public class MetadataManagedAttributeRepository
    extends DinaRepository<MetadataManagedAttributeDto, MetadataManagedAttribute> {

  public MetadataManagedAttributeRepository(
    @NonNull DinaService<MetadataManagedAttribute> dinaService,
    @NonNull DinaFilterResolver filterResolver
  ) {
    super(
      dinaService,
      new DinaMapper<>(MetadataManagedAttributeDto.class),
      MetadataManagedAttributeDto.class,
      MetadataManagedAttribute.class,
      filterResolver);
  }

}
