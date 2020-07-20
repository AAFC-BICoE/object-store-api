package ca.gc.aafc.objectstore.api.respository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import lombok.NonNull;

@Repository
@Transactional
public class ObjectSubtypeResourceRepository
    extends DinaRepository<ObjectSubtypeDto, ObjectSubtype> {

  public ObjectSubtypeResourceRepository(
    @NonNull DinaService<ObjectSubtype> dinaService,
    @NonNull DinaFilterResolver filterResolver
  ) {
    super(
      dinaService,
      Optional.ofNullable(null),
      new DinaMapper<>(ObjectSubtypeDto.class),
      ObjectSubtypeDto.class,
      ObjectSubtype.class,
      filterResolver);
  }

}
