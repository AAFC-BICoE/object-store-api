package ca.gc.aafc.objectstore.api.respository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import lombok.NonNull;

@Repository
public class ManagedAttributeResourceRepository
    extends DinaRepository<ManagedAttributeDto, ManagedAttribute> {

  public ManagedAttributeResourceRepository(
    @NonNull DinaService<ManagedAttribute> dinaService,
    @NonNull DinaFilterResolver filterResolver
  ) {
    super(
      dinaService,
      Optional.ofNullable(null),
      new DinaMapper<>(ManagedAttributeDto.class),
      ManagedAttributeDto.class,
      ManagedAttribute.class, filterResolver);
  }

}
