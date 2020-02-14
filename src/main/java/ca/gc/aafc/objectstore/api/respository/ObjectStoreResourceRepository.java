package ca.gc.aafc.objectstore.api.respository;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.objectstore.api.ObjectStoreConfiguration;
import ca.gc.aafc.objectstore.api.dao.BaseDAO;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.Agent;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata.DcType;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.file.FileInformationService;
import ca.gc.aafc.objectstore.api.file.FileMetaEntry;
import ca.gc.aafc.objectstore.api.filter.RsqlFilterHandler;
import ca.gc.aafc.objectstore.api.interfaces.SoftDeletable;
import ca.gc.aafc.objectstore.api.mapper.CycleAvoidingMappingContext;
import ca.gc.aafc.objectstore.api.mapper.ObjectStoreMetadataMapper;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetadataReadService;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.jpa.query.JpaQueryExecutor;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQuery;
import io.crnk.data.jpa.query.criteria.JpaCriteriaQueryFactory;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
@Transactional
public class ObjectStoreResourceRepository extends ResourceRepositoryBase<ObjectStoreMetadataDto, UUID>
    implements ObjectStoreMetadataReadService {

  private final ObjectStoreConfiguration config;
  private final BaseDAO dao;
  private final ObjectStoreMetadataMapper mapper;
  private final FileInformationService fileInformationService;
  private final RsqlFilterHandler rsqlFilterHandler;

  private JpaCriteriaQueryFactory queryFactory;

  private static PathSpec DELETED_PATH_SPEC = PathSpec.of(SoftDeletable.DELETED_DATE_FIELD_NAME);
  private static FilterSpec DELETED_DATE_IS_NULL = new FilterSpec(DELETED_PATH_SPEC, FilterOperator.EQ, null);

  @Inject
  public ObjectStoreResourceRepository(ObjectStoreConfiguration config, BaseDAO dao, ObjectStoreMetadataMapper mapper,
      FileInformationService fileInformationService, RsqlFilterHandler rsqlFilterHandler) {
    super(ObjectStoreMetadataDto.class);
    this.config = config;
    this.dao = dao;
    this.mapper = mapper;
    this.fileInformationService = fileInformationService;
    this.rsqlFilterHandler = rsqlFilterHandler;
  }

  @PostConstruct
  void setup() {
    queryFactory = dao.createWithEntityManager(JpaCriteriaQueryFactory::newInstance);
  }

  /**
   * @param resource to save
   * @return saved resource
   */
  @Override
  public <S extends ObjectStoreMetadataDto> S save(S resource) {
    ObjectStoreMetadataDto dto = (ObjectStoreMetadataDto) resource;
    ObjectStoreMetadata objectMetadata = dao.findOneByNaturalId(dto.getUuid(), ObjectStoreMetadata.class);
    mapper.updateObjectStoreMetadataFromDto(dto, objectMetadata);

    objectMetadata = handleFileRelatedData(objectMetadata);

    // relationships
    if (resource.getAcMetadataCreator() != null) {
      objectMetadata
          .setAcMetadataCreator(dao.getReferenceByNaturalId(Agent.class, resource.getAcMetadataCreator().getUuid()));
    }

    dao.save(objectMetadata);
    return resource;
  }

  @Override
  public ObjectStoreMetadataDto findOne(UUID uuid, QuerySpec querySpec) {
    ObjectStoreMetadata objectStoreMetadata = loadObjectStoreMetadata(uuid).orElseThrow(
        () -> new ResourceNotFoundException(this.getClass().getSimpleName() + " with ID " + uuid + " Not Found."));

    if( objectStoreMetadata.getDeletedDate() != null &&
        !querySpec.findFilter(DELETED_PATH_SPEC).isPresent() ) {
      throw new GoneException("ID " + uuid + " deleted");
    }

    return mapper.toDto(objectStoreMetadata, fieldName -> dao.isLoaded(objectStoreMetadata, fieldName),
        new CycleAvoidingMappingContext());
  }

  @Override
  public Optional<ObjectStoreMetadata> loadObjectStoreMetadata(UUID id) {
    return Optional.ofNullable(dao.findOneByNaturalId(id, ObjectStoreMetadata.class));
  }

  @Override
  public Optional<ObjectStoreMetadata> loadObjectStoreMetadataByFileId(UUID fileId) {
    return Optional.ofNullable(dao.findOneByProperty(ObjectStoreMetadata.class, "fileIdentifier", fileId));
  }

  @Override
  public ResourceList<ObjectStoreMetadataDto> findAll(QuerySpec querySpec) {
    JpaCriteriaQuery<ObjectStoreMetadata> jq = queryFactory.query(ObjectStoreMetadata.class);
    // Omit "managedAttributeMap" from the JPA include spec, because it is a generated object, not on the JPA model.
    QuerySpec jpaFriendlyQuerySpec = querySpec.clone();
    jpaFriendlyQuerySpec.getIncludedRelations()
      .removeIf(include -> include.getPath().toString().equals("managedAttributeMap"));

    if (!querySpec.findFilter(DELETED_PATH_SPEC).isPresent()) {
      jpaFriendlyQuerySpec.addFilter(DELETED_DATE_IS_NULL);
    }

    Consumer<JpaQueryExecutor<?>> rsqlApplier = rsqlFilterHandler.getRestrictionApplier(jpaFriendlyQuerySpec);
    JpaQueryExecutor<ObjectStoreMetadata> executor = jq.buildExecutor(jpaFriendlyQuerySpec);
    rsqlApplier.accept(executor);
  
    List<ObjectStoreMetadataDto> l = executor.getResultList().stream()
      .map( objectStoreMetadata -> mapper.toDto(objectStoreMetadata, fieldName -> dao.isLoaded(objectStoreMetadata, fieldName), new CycleAvoidingMappingContext()))
      .collect(Collectors.toList());
    
    return new DefaultResourceList<ObjectStoreMetadataDto>(l, null, null);
  }

  @Override
  public <S extends ObjectStoreMetadataDto> S create(S resource) {
    ObjectStoreMetadataDto dto = (ObjectStoreMetadataDto) resource;
    if (dto.getUuid() == null) {
      dto.setUuid(UUID.randomUUID());
    }
    
    ObjectStoreMetadata objectMetadata = mapper
        .toEntity((ObjectStoreMetadataDto) resource);
    
    Function<ObjectStoreMetadata, ObjectStoreMetadata> handleFileDataFct = this::handleFileRelatedData;

    // same as assignDefaultValues(handleFileRelatedData(handleDefaultValues)) but easier to follow in my option (C.G.)
    objectMetadata = handleFileDataFct.andThen(this::assignDefaultValues)
        .apply(objectMetadata);
   
    // relationships
    if (resource.getAcMetadataCreator() != null) {
      objectMetadata.setAcMetadataCreator(
          dao.getReferenceByNaturalId(Agent.class, resource.getAcMetadataCreator().getUuid()));
    }
    
    dao.save(objectMetadata);
    return resource;
  }
  
  @Override
  public void delete(UUID id) {
    ObjectStoreMetadata objectStoreMetadata = dao.findOneByNaturalId(id, ObjectStoreMetadata.class);
    if(objectStoreMetadata != null) {
      objectStoreMetadata.setDeletedDate(OffsetDateTime.now());
    }
  }
  
  /**
   * Method responsible for dealing with validation and setting of data related to 
   * files.
   * 
   * @param objectMetadata
   * @throws ValidationException
   */
  private ObjectStoreMetadata handleFileRelatedData(ObjectStoreMetadata objectMetadata)
      throws ValidationException {
    // we need to validate at least that bucket name and fileIdentifier are there
    if (StringUtils.isBlank(objectMetadata.getBucket())
        || StringUtils.isBlank(Objects.toString(objectMetadata.getFileIdentifier(), ""))) {
      throw new ValidationException("fileIdentifier and bucket should be provided");
    }

    try {
      FileMetaEntry fileMetaEntry = fileInformationService.getJsonFileContentAs(
          objectMetadata.getBucket(),
          objectMetadata.getFileIdentifier().toString() + FileMetaEntry.SUFFIX,
          FileMetaEntry.class).orElseThrow( () -> new BadRequestException(
              this.getClass().getSimpleName() + " with ID " + objectMetadata.getFileIdentifier() + " Not Found."));

      objectMetadata.setFileExtension(fileMetaEntry.getEvaluatedFileExtension());
      objectMetadata.setOriginalFilename(fileMetaEntry.getOriginalFilename());
      objectMetadata.setDcFormat(fileMetaEntry.getDetectedMediaType());
      objectMetadata.setAcHashValue(fileMetaEntry.getSha1Hex());
      objectMetadata.setAcHashFunction(FileController.DIGEST_ALGORITHM);

      return objectMetadata;

    } catch (IOException e) {
      log.error(e.getMessage());
      throw new BadRequestException("Can't process " + objectMetadata.getFileIdentifier());
    }

  }
  
  /**
   * Method to assign default values to mandatory fields not provided at creation time.
   * If no value can be found, null with be used.
   * @param objectMetadata
   * @return the provided object with default values set (if required)
   */
  private ObjectStoreMetadata assignDefaultValues(ObjectStoreMetadata objectMetadata) {
    if (objectMetadata.getDcType() == null) {
      objectMetadata.setDcType(DcType.fromDcFormat(objectMetadata.getDcFormat()).orElse(ObjectStoreMetadata.DcType.UNDETERMINED));
    }
    
    if (objectMetadata.getXmpRightsWebStatement() == null) {
      objectMetadata.setXmpRightsWebStatement(config.getDefaultLicenceURL());
    }

    if (objectMetadata.getDcRights() == null) {
      objectMetadata.setDcRights(config.getDefaultCopyright());
    }
    
    if( objectMetadata.getXmpRightsOwner() == null) {
      objectMetadata.setXmpRightsOwner(config.getDefaultCopyrightOwner());
    }
    
    return objectMetadata;
  }

}
