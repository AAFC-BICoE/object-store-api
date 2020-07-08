package ca.gc.aafc.objectstore.api.respository;

import java.io.IOException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import javax.ws.rs.BadRequestException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.entity.SoftDeletable;
import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.repository.GoneException;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.file.FileInformationService;
import ca.gc.aafc.objectstore.api.file.FileMetaEntry;
import ca.gc.aafc.objectstore.api.file.ThumbnailService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetadataDefaultValueSetterService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetadataReadService;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
@Transactional
public class ObjectStoreResourceRepository
    extends DinaRepository<ObjectStoreMetadataDto, ObjectStoreMetadata>
    implements ObjectStoreMetadataReadService {

  public ObjectStoreResourceRepository(
    @NonNull DinaService<ObjectStoreMetadata> dinaService,
    @NonNull DinaFilterResolver filterResolver,
    FileInformationService fileInformationService,
    ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService
  ) {
    super(
      dinaService,
      new DinaMapper<>(ObjectStoreMetadataDto.class),
      ObjectStoreMetadataDto.class,
      ObjectStoreMetadata.class,
      filterResolver);
    this.dinaService = dinaService;
    this.fileInformationService = fileInformationService;
    this.defaultValueSetterService = defaultValueSetterService;
  }

  private final DinaService<ObjectStoreMetadata> dinaService;
  private final FileInformationService fileInformationService;
  private final ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService;
  private static PathSpec DELETED_PATH_SPEC = PathSpec.of("softDeleted");

  private static final PathSpec DELETED_DATE = PathSpec.of(SoftDeletable.DELETED_DATE_FIELD_NAME);
  private static final FilterSpec SOFT_DELETED_FILTER = DELETED_DATE.filter(FilterOperator.NEQ, null);
  private static final FilterSpec NOT_DELETED_FILTER = DELETED_DATE.filter(FilterOperator.EQ, null);

  /**
   * @param resource to save
   * @return saved resource
   */
  @Override
  @SuppressWarnings("unchecked")
  public <S extends ObjectStoreMetadataDto> S save(S resource) {
    handleFileRelatedData(resource);
    S dto = super.save(resource);
    return (S) this.findOne(dto.getUuid(), new QuerySpec(ObjectStoreMetadataDto.class));
  }

  @Override
  public ObjectStoreMetadataDto findOne(Serializable id, QuerySpec querySpec) {
    // Omit "managedAttributeMap" from the JPA include spec, because it is a generated object, not on the JPA model.
    QuerySpec jpaFriendlyQuerySpec = querySpec.clone();
    jpaFriendlyQuerySpec.getIncludedRelations()
      .removeIf(include -> include.getPath().toString().equals("managedAttributeMap"));

    ObjectStoreMetadataDto dto = super.findOne(id, jpaFriendlyQuerySpec);

    if ( dto.getDeletedDate() != null &&
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

    if (jpaFriendlyQuerySpec.findFilter(DELETED_PATH_SPEC).isPresent()) {
      jpaFriendlyQuerySpec.addFilter(SOFT_DELETED_FILTER);
    } else {
      jpaFriendlyQuerySpec.addFilter(NOT_DELETED_FILTER);
    }
    jpaFriendlyQuerySpec.getFilters().removeIf(f->f.getAttributePath().contains("softDeleted"));

    return super.findAll(jpaFriendlyQuerySpec);
  }

  @Override
  public Optional<ObjectStoreMetadata> loadObjectStoreMetadata(UUID id) {
    return Optional.ofNullable(dinaService.findOne(id, ObjectStoreMetadata.class));
  }

  @Override
  public Optional<ObjectStoreMetadata> loadObjectStoreMetadataByFileId(UUID fileId) {
    return dinaService.findAll(
        ObjectStoreMetadata.class,
        (cb, root) -> new Predicate[] { cb.equal(root.get("fileIdentifier"), fileId) }
        , null, 0, 1)
      .stream().findFirst();
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
    ObjectStoreMetadata objectStoreMetadata = dinaService.findOne(id, ObjectStoreMetadata.class);
    if (objectStoreMetadata != null) {
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
