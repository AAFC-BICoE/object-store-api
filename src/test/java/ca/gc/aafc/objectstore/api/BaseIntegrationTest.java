package ca.gc.aafc.objectstore.api;

import javax.inject.Inject;

import ca.gc.aafc.objectstore.api.minio.MinioTestContainerInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import ca.gc.aafc.dina.testsupport.DatabaseSupportService;
import ca.gc.aafc.dina.testsupport.PostgresTestContainerInitializer;
import ca.gc.aafc.objectstore.api.service.DerivativeService;
import ca.gc.aafc.objectstore.api.service.ManagedAttributeService;
import ca.gc.aafc.objectstore.api.service.MetaManagedAttributeService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetaDataService;
import ca.gc.aafc.objectstore.api.service.ObjectSubTypeService;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;

@SpringBootTest(classes = ObjectStoreApiLauncher.class)
@TestPropertySource(properties = "spring.config.additional-location=classpath:application-test.yml")
@Transactional
@ContextConfiguration(initializers = {PostgresTestContainerInitializer.class, MinioTestContainerInitializer.class})
public abstract class BaseIntegrationTest {

  @Inject
  protected DatabaseSupportService service;

  @Inject
  protected DerivativeService derivativeService;

  @Inject
  protected ManagedAttributeService managedAttributeService;

  @Inject
  protected MetaManagedAttributeService metaManagedAttributeService;

  @Inject
  protected ObjectStoreMetaDataService objectStoreMetaDataService;

  @Inject
  protected ObjectSubTypeService objectSubTypeService;

  @Inject
  protected ObjectUploadService objectUploadService;

}
