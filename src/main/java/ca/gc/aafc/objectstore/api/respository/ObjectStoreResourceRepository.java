package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.dina.entity.SoftDeletable;
import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.repository.GoneException;
import ca.gc.aafc.dina.repository.external.ExternalResourceProvider;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.DinaRole;
import ca.gc.aafc.dina.service.AuditService;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.file.FileController;
import ca.gc.aafc.objectstore.api.file.ThumbnailService;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.respository.managedattributemap.MetadataToManagedAttributeMapRepository;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetadataDefaultValueSetterService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetadataReadService;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import javax.validation.ValidationException;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Log4j2
@Repository
@Transactional
public class ObjectStoreResourceRepository
  extends DinaRepository<ObjectStoreMetadataDto, ObjectStoreMetadata>
  implements ObjectStoreMetadataReadService {

  private final DinaService<ObjectStoreMetadata> dinaService;
  private final DinaAuthenticatedUser authenticatedUser;
  private final MinioFileService minioService;
  private final ObjectUploadService objectUploadService;
  private final AuditService auditService;
  private final ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService;
  private static final PathSpec DELETED_PATH_SPEC = PathSpec.of("softDeleted");
  private static final PathSpec DELETED_DATE = PathSpec.of(SoftDeletable.DELETED_DATE_FIELD_NAME);
  private static final FilterSpec SOFT_DELETED = DELETED_DATE.filter(FilterOperator.NEQ, null);
  private static final FilterSpec NOT_DELETED_FILTER = DELETED_DATE.filter(FilterOperator.EQ, null);

  public ObjectStoreResourceRepository(
    @NonNull DinaService<ObjectStoreMetadata> dinaService,
    @NonNull DinaFilterResolver filterResolver,
    @NonNull ObjectStoreMetadataDefaultValueSetterService defaultValueSetterService,
    @NonNull ExternalResourceProvider externalResourceProvider,
    @NonNull DinaAuthenticatedUser authenticatedUser,
    @NonNull AuditService auditService,
    @NonNull MinioFileService minioService,
    @NonNull ObjectUploadService objectUploadService
  ) {
    super(
      dinaService,
      Optional.empty(),
      Optional.of(auditService),
      new DinaMapper<>(ObjectStoreMetadataDto.class),
      ObjectStoreMetadataDto.class,
      ObjectStoreMetadata.class,
      filterResolver,
      externalResourceProvider);
    this.dinaService = dinaService;
    this.auditService = auditService;
    this.defaultValueSetterService = defaultValueSetterService;
    this.authenticatedUser = authenticatedUser;
    this.minioService = minioService;
    this.objectUploadService = objectUploadService;
  }

  /**
   * @param resource to save
   * @return saved resource
   */
  @Override
  @SuppressWarnings("unchecked")
  public <S extends ObjectStoreMetadataDto> S save(S resource) {
    handleFileRelatedData(resource);
    loadObjectStoreMetadata(resource.getUuid()).ifPresent(objectStoreMetadata ->
      resource.setManagedAttributeMap(
        MetadataToManagedAttributeMapRepository.getAttributeMapFromMetadata(objectStoreMetadata)));
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

    if (dto.getDeletedDate() != null &&
        jpaFriendlyQuerySpec.findFilter(DELETED_PATH_SPEC).isEmpty()) {
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
      jpaFriendlyQuerySpec.addFilter(SOFT_DELETED);
    } else {
      jpaFriendlyQuerySpec.addFilter(NOT_DELETED_FILTER);
    }
    jpaFriendlyQuerySpec.getFilters().removeIf(f -> f.getPath().equals(DELETED_PATH_SPEC));

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
      (cb, root) -> new Predicate[]{cb.equal(root.get("fileIdentifier"), fileId)}
      , null, 0, 1)
      .stream().findFirst();
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectStoreMetadataDto create(ObjectStoreMetadataDto resource) {
    Function<ObjectStoreMetadataDto, ObjectStoreMetadataDto> handleFileDataFct = this::handleFileRelatedData;

    // same as assignDefaultValues(handleFileRelatedData(handleDefaultValues)) but easier to follow in my option (C.G.)
    handleFileDataFct.andThen(defaultValueSetterService::assignDefaultValues).apply(resource);

    resource.setCreatedBy(authenticatedUser.getUsername());
    ObjectStoreMetadataDto created = super.create(resource);

    handleThumbNailMetaEntry(created);

    return this.findOne(
      created.getUuid(),
      new QuerySpec(ObjectStoreMetadataDto.class)
    );
  }

  /**
   * Soft-delete using setDeletedDate instead of a hard delete. Unless the user is a collection
   * manager, then a hard delete is triggered. hard delete removes original file, metadata, and
   * object upload record.
   */
  @Override
  public void delete(Serializable id) {
    ObjectStoreMetadata objectStoreMetadata = dinaService.findOne(id, ObjectStoreMetadata.class);
    if (objectStoreMetadata != null) {
      if (objectStoreMetadata.getDeletedDate() != null && isUserCollectionManager()) {
        hardDelete(objectStoreMetadata);
      } else {
        objectStoreMetadata.setDeletedDate(OffsetDateTime.now());
      }
    }
  }

  /**
   * Hard deletes a given metadata hard delete removes original file, metadata, object upload
   * record, and thumb nail.
   *
   * @param objectStoreMetadata - metadata to remove
   */
  private void hardDelete(@NonNull ObjectStoreMetadata objectStoreMetadata) {
    ObjectUpload objectUpload = dinaService.findOne(
      objectStoreMetadata.getFileIdentifier(),
      ObjectUpload.class);

    if (objectUpload != null) {
      //Remove File from minio
      removeFileFromMinio(
        objectUpload.getBucket(),
        objectUpload.getFileIdentifier().toString(),
        objectUpload.getEvaluatedFileExtension());

      //Remove upload record
      objectUploadService.delete(objectUpload);

      //Remove thumbnail and thumb nail meta data
      if (objectUpload.getThumbnailIdentifier() != null) {
        loadObjectStoreMetadataByFileId(objectUpload.getThumbnailIdentifier()).ifPresent(thumb -> {
          removeFileFromMinio(
            objectUpload.getBucket(),
            thumb.getFileIdentifier() + ".thumbnail",
            ThumbnailService.THUMBNAIL_EXTENSION);
          dinaService.delete(thumb);
        });
      }
    }

    //Delete Meta
    dinaService.delete(objectStoreMetadata);

    //Delete audit info
    auditService.removeSnapshots(AuditService.AuditInstance.builder()
      .type(ObjectStoreMetadataDto.TYPENAME)
      .id(objectStoreMetadata.getUuid().toString())
      .build());
  }

  /**
   * Method responsible for dealing with validation and setting of data related to files.
   *
   * @param objectMetadata - The metadata of the data to set.
   * @throws ValidationException If a file identifier was not provided.
   */
  private ObjectStoreMetadataDto handleFileRelatedData(ObjectStoreMetadataDto objectMetadata)
    throws ValidationException {
    // we need to validate at least that bucket name and fileIdentifier are there
    if (StringUtils.isBlank(objectMetadata.getBucket())
        || StringUtils.isBlank(Objects.toString(objectMetadata.getFileIdentifier(), ""))) {
      throw new ValidationException("fileIdentifier and bucket should be provided");
    }

    ObjectUpload objectUpload = dinaService.findOne(
      objectMetadata.getFileIdentifier(),
      ObjectUpload.class);

    if (objectUpload == null) {
      throw new ValidationException("fileIdentifier not found");
    }

    objectMetadata.setFileExtension(objectUpload.getEvaluatedFileExtension());
    objectMetadata.setOriginalFilename(objectUpload.getOriginalFilename());
    objectMetadata.setDcFormat(objectUpload.getDetectedMediaType());
    objectMetadata.setAcHashValue(objectUpload.getSha1Hex());
    objectMetadata.setAcHashFunction(FileController.DIGEST_ALGORITHM);

    return objectMetadata;
  }

  /**
   * Persists a thumbnail Metadata based off a given resource if the resource has an associated
   * thumbnail.
   *
   * @param resource - parent resource metadata of the thumbnail
   */
  private void handleThumbNailMetaEntry(ObjectStoreMetadataDto resource) {
    ObjectUpload objectUpload = dinaService.findOne(
      resource.getFileIdentifier(),
      ObjectUpload.class);
    if (objectUpload.getThumbnailIdentifier() != null) {
      ObjectStoreMetadataDto thumbnailMetadataDto = ThumbnailService.generateThumbMetaData(
        resource,
        objectUpload.getThumbnailIdentifier());
      super.create(thumbnailMetadataDto);
    }
  }

  /**
   * Removes a file from the minio file service.
   *
   * @param bucket                 - bucket of the file
   * @param fileIdentifier         - file identifier
   * @param fileExtension - extension of the file
   */
  @SneakyThrows
  private void removeFileFromMinio(String bucket, String fileIdentifier, String fileExtension) {
    minioService.removeFile(
      bucket,
      fileIdentifier + StringUtils.prependIfMissing(fileExtension, "."));
  }

  /**
   * Returns true if the authenticated user has a collection manager role in one of it's groups.
   *
   * @return true if the authenticated user has a collection manager role in one of it's groups.
   */
  private boolean isUserCollectionManager() {
    return authenticatedUser.getRolesPerGroup().values().stream().anyMatch(
      dinaRoles -> dinaRoles.stream().anyMatch(role -> role.equals(DinaRole.COLLECTION_MANAGER)));
  }
}
