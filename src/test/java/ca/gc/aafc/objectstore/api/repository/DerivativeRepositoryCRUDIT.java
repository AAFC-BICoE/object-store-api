package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.dto.DerivativeDto;
import ca.gc.aafc.objectstore.api.respository.DerivativeRepository;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

public class DerivativeRepositoryCRUDIT extends BaseRepositoryTest {

  @Inject
  private DerivativeRepository derivativeRepository;

  @Test
  void create() {
    //TODO cannot create without a upload present, handle file related data
    DerivativeDto resource =  derivativeRepository.create(DerivativeDto.builder()
      .bucket("dina bucket")
      .fileIdentifier(UUID.randomUUID())
      .build());
    DerivativeDto result = derivativeRepository.findOne(resource.getUuid(), new QuerySpec(DerivativeDto.class));
    Assertions.assertEquals(resource.getBucket(),result.getBucket());
  }
}
