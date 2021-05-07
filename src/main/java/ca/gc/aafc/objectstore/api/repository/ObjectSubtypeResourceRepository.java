package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import lombok.NonNull;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public class ObjectSubtypeResourceRepository
    extends DinaRepository<ObjectSubtypeDto, ObjectSubtype> {

  private final DinaService<ObjectSubtype> dinaService;
  private final MessageSource messageSource;
  private Optional<DinaAuthenticatedUser> authenticatedUser;

  public ObjectSubtypeResourceRepository(
    @NonNull DinaService<ObjectSubtype> dinaService,
    MessageSource messageSource,
    Optional<DinaAuthenticatedUser> authenticatedUser,
    @NonNull BuildProperties props
  ) {
    super(
      dinaService,
      Optional.empty(),
      Optional.empty(),
      new DinaMapper<>(ObjectSubtypeDto.class),
      ObjectSubtypeDto.class,
      ObjectSubtype.class,
      null,
      null,
      props);
    this.dinaService = dinaService;
    this.messageSource = messageSource;
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
