package ca.gc.aafc.objectstore.api.respository;

import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.transaction.Transactional;
import javax.validation.ValidationException;
import javax.ws.rs.BadRequestException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.entity.SoftDeletable;
import ca.gc.aafc.dina.filter.FilterHandler;
import ca.gc.aafc.dina.filter.RsqlFilterHandler;
import ca.gc.aafc.dina.filter.SimpleFilterHandler;
import ca.gc.aafc.dina.jpa.BaseDAO;
import ca.gc.aafc.dina.repository.GoneException;
import ca.gc.aafc.dina.repository.JpaDtoRepository;
import ca.gc.aafc.dina.repository.JpaResourceRepository;
import ca.gc.aafc.dina.repository.meta.JpaMetaInformationProvider;
import ca.gc.aafc.objectstore.api.ObjectStoreConfiguration;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.file.FileInformationService;
import ca.gc.aafc.objectstore.api.file.FileMetaEntry;
import ca.gc.aafc.objectstore.api.file.ThumbnailService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetadataDefaultValueSetterService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetadataReadService;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
@Transactional
public class ObjectStoreResourceRepository extends JpaResourceRepository<ObjectStoreMetadataDto>
    implements ObjectStoreMetadataReadService {

  public ObjectStoreResourceRepository(
    JpaDtoRepository dtoRepository,
    SimpleFilterHandler simpleFilterHandler,
    RsqlFilterHandler rsqlFilterHandler,
    JpaMetaInformationProvider metaInformationProvider,
    ObjectStoreConfiguration config,
    BaseDAO dao,
    FileInformationService fileInformationService,
    ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService
  ) {
    super(
      ObjectStoreMetadataDto.class,
      dtoRepository,
      Arrays.asList(simpleFilterHandler, rsqlFilterHandler, softDeletedFilterHandler),
      metaInformationProvider
    );
    this.dao = dao;
    this.fileInformationService = fileInformationService;
    this.defaultValueSetterService = defaultValueSetterService;
  }

  private final BaseDAO dao;
  private final FileInformationService fileInformationService;
  private final ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService;

  private static PathSpec DELETED_PATH_SPEC = PathSpec.of("softDeleted");

  /**
   * @param resource to save
   * @return saved resource
   */
  @Override
  public <S extends ObjectStoreMetadataDto> S save(S resource) {
    handleFileRelatedData(resource);
    S dto = super.save(resource);

    return (S) this.findOne(
      dto.getUuid(),
      new QuerySpec(ObjectStoreMetadataDto.class)
    );
  }

  @Override
  public ObjectStoreMetadataDto findOne(Serializable id, QuerySpec querySpec) {
    // Omit "managedAttributeMap" from the JPA include spec, because it is a generated object, not on the JPA model.
    QuerySpec jpaFriendlyQuerySpec = querySpec.clone();
    jpaFriendlyQuerySpec.getIncludedRelations()
      .removeIf(include -> include.getPath().toString().equals("managedAttributeMap"));

    ObjectStoreMetadataDto dto = super.findOne(id, jpaFriendlyQuerySpec);

    if( dto.getDeletedDate() != null &&
        !jpaFriendlyQuerySpec.findFilter(DELETED_PATH_SPEC).isPresent() ) {
      throw new GoneException("Deleted", "ID " + id + " deleted");
    }

    return dto;
  }

  @Override
  public ResourceList<ObjectStoreMetadataDto> findAll(QuerySpec querySpec) {
    // Omit "managedAttributeMap" from the JPA include spec, because it is a generated object, not on the JPA model.
    QuerySpec jpaFriendlyQuerySpec = querySpec.clone();
    jpaFriendlyQuerySpec.getIncludedRelations()
      .removeIf(include -> include.getPath().toString().equals("managedAttributeMap"));

    return super.findAll(jpaFriendlyQuerySpec);
  }

  @Override
  public Optional<ObjectStoreMetadata> loadObjectStoreMetadata(UUID id) {
    return Optional.ofNullable(dao.findOneByNaturalId(id, ObjectStoreMetadata.class));
  }

  @Override
  public Optional<ObjectStoreMetadata> loadObjectStoreMetadataByFileId(UUID fileId) {
    return Optional.ofNullable(dao.findOneByProperty(ObjectStoreMetadata.class, "fileIdentifier", fileId));
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectStoreMetadataDto create(ObjectStoreMetadataDto resource) {
    Function<ObjectStoreMetadataDto, ObjectStoreMetadataDto> handleFileDataFct = this::handleFileRelatedData;

    // same as assignDefaultValues(handleFileRelatedData(handleDefaultValues)) but easier to follow in my option (C.G.)
    handleFileDataFct.andThen(defaultValueSetterService::assignDefaultValues).apply(resource);

    ObjectStoreMetadataDto created = super.create(resource);

    handleThumbNailMetaEntry(created);

    return this.findOne(
      created.getUuid(),
      new QuerySpec(ObjectStoreMetadataDto.class)
    );
  }
  
  /**
   * Soft-delete using setDeletedDate instead of a hard delete.
   */
  @Override
  public void delete(Serializable id) {
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
  private ObjectStoreMetadataDto handleFileRelatedData(ObjectStoreMetadataDto objectMetadata)
      throws ValidationException {
    // we need to validate at least that bucket name and fileIdentifier are there
    if (StringUtils.isBlank(objectMetadata.getBucket())
        || StringUtils.isBlank(Objects.toString(objectMetadata.getFileIdentifier(), ""))) {
      throw new ValidationException("fileIdentifier and bucket should be provided");
    }

    FileMetaEntry fileMetaEntry = getFileMetaEntry(objectMetadata);

    objectMetadata.setFileExtension(fileMetaEntry.getEvaluatedFileExtension());
    objectMetadata.setOriginalFilename(fileMetaEntry.getOriginalFilename());
    objectMetadata.setDcFormat(fileMetaEntry.getDetectedMediaType());
    objectMetadata.setAcHashValue(fileMetaEntry.getSha1Hex());
    objectMetadata.setAcHashFunction(FileController.DIGEST_ALGORITHM);

    return objectMetadata;
  }

  /**
   * Returns the {@link FileMetaEntry} for the resource of the given
   * {@link ObjectStoreMetadataDto}
   * 
   * @param objectMetadata - meta data for the resource
   * @return {@link FileMetaEntry} for the resource
   */
  private FileMetaEntry getFileMetaEntry(ObjectStoreMetadataDto objectMetadata) {
    try {
      return fileInformationService
          .getJsonFileContentAs(
              objectMetadata.getBucket(),
              objectMetadata.getFileIdentifier().toString() + FileMetaEntry.SUFFIX,
              FileMetaEntry.class)
          .orElseThrow(() -> new BadRequestException(
              this.getClass().getSimpleName() + " with ID " + objectMetadata.getFileIdentifier() + " Not Found."));
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new BadRequestException("Can't process " + objectMetadata.getFileIdentifier());
    }
  }

  /**
   * Shows only non-soft-deleted records by default.
   * Shows only soft-deleted records if DELETED_PATH_SPEC is present.
   */
  private static FilterHandler softDeletedFilterHandler = (querySpec, root, query,
      cb) -> !querySpec.findFilter(DELETED_PATH_SPEC).isPresent()
          ? cb.isNull(root.get(SoftDeletable.DELETED_DATE_FIELD_NAME))
          : cb.isNotNull(root.get(SoftDeletable.DELETED_DATE_FIELD_NAME));

  /**
   * Persists a thumbnail Metadata based off a given resource if the resource has
   * an associated thumbnail.
   * 
   * @param resource - parent resource metadata of the thumbnail
   */
  private void handleThumbNailMetaEntry(ObjectStoreMetadataDto resource) {
    FileMetaEntry fileMetaEntry = getFileMetaEntry(resource);
    if (fileMetaEntry.getThumbnailIdentifier() != null) {
      ObjectStoreMetadataDto thumbnailMetadataDto = ThumbnailService.generateThumbMetaData(
          resource,
          fileMetaEntry.getThumbnailIdentifier());

      super.create(thumbnailMetadataDto);
    }
  }
}
