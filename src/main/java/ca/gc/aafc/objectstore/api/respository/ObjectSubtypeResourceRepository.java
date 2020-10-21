package ca.gc.aafc.objectstore.api.respository;

import java.util.Optional;

import javax.transaction.Transactional;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import lombok.NonNull;

@Repository
@Transactional
public class ObjectSubtypeResourceRepository
    extends DinaRepository<ObjectSubtypeDto, ObjectSubtype> {

  private Optional<DinaAuthenticatedUser> authenticatedUser;

  public ObjectSubtypeResourceRepository(
    @NonNull DinaService<ObjectSubtype> dinaService,
    @NonNull DinaFilterResolver filterResolver,
    Optional<DinaAuthenticatedUser> authenticatedUser
  ) {
    super(
      dinaService,
      Optional.empty(),
      Optional.empty(),
      new DinaMapper<>(ObjectSubtypeDto.class),
      ObjectSubtypeDto.class,
      ObjectSubtype.class,
      filterResolver);
    this.authenticatedUser = authenticatedUser;
  }


  @Override
  public <S extends ObjectSubtypeDto> S create(S resource) {
    if (authenticatedUser.isPresent()) {
      resource.setCreatedBy(authenticatedUser.get().getUsername());
    }
    return super.create(resource);
  }

}
