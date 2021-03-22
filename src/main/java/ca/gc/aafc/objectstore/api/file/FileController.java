package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.exif.ExifParser;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetadataReadService;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;
import io.crnk.core.exception.UnauthorizedException;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Log4j2
public class FileController {

  public static final String DIGEST_ALGORITHM = "SHA-1";
  private static final int MAX_NUMBER_OF_ATTEMPT_RANDOM_UUID = 5;
  private static final int READ_AHEAD_BUFFER_SIZE = 10 * 1024;

  private final ObjectUploadService objectUploadService;
  private final MinioFileService minioService;
  private final ObjectStoreMetadataReadService objectStoreMetadataReadService;
  private final MediaTypeDetectionStrategy mediaTypeDetectionStrategy;
  private final ThumbnailService thumbnailService;
  private final MessageSource messageSource;
  private final TransactionTemplate transactionTemplate;

  // request scoped bean
  private final DinaAuthenticatedUser authenticatedUser;

  @Inject
  public FileController(
    MinioFileService minioService,
    ObjectUploadService objectUploadService,
    ObjectStoreMetadataReadService objectStoreMetadataReadService,
    MediaTypeDetectionStrategy mediaTypeDetectionStrategy,
    ThumbnailService thumbnailService,
    DinaAuthenticatedUser authenticatedUser,
    MessageSource messageSource,
    TransactionTemplate transactionTemplate
  ) {
    this.minioService = minioService;
    this.objectUploadService = objectUploadService;
    this.objectStoreMetadataReadService = objectStoreMetadataReadService;
    this.mediaTypeDetectionStrategy = mediaTypeDetectionStrategy;
    this.thumbnailService = thumbnailService;
    this.authenticatedUser = authenticatedUser;
    this.messageSource = messageSource;
    this.transactionTemplate = transactionTemplate;
  }

  @PostMapping("/file/{bucket}/derivative")
  @Transactional
  public ObjectUpload handleDerivativeUpload(
    @RequestParam("file") MultipartFile file,
    @PathVariable String bucket
  ) throws IOException, MimeTypeException, NoSuchAlgorithmException, ServerException, ErrorResponseException,
    InternalException, XmlParserException, InvalidResponseException, InvalidBucketNameException,
    InsufficientDataException, InvalidKeyException {
    //Authenticate before anything else
    handleAuthentication(bucket);

    // Safe get unique UUID
    UUID uuid = safeGenerateUuid();

    // We need access to the first bytes in a form that we can reset the InputStream
    ReadAheadInputStream prIs = ReadAheadInputStream.from(file.getInputStream(), READ_AHEAD_BUFFER_SIZE);

    MediaTypeDetectionStrategy.MediaTypeDetectionResult mtdr = mediaTypeDetectionStrategy
      .detectMediaType(prIs.getReadAheadBuffer(), file.getContentType(), file.getOriginalFilename());

    MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);

    storeFile(bucket, uuid, mtdr, new DigestInputStream(prIs.getInputStream(), md), true);

    return createObjectUpload(
      file,
      bucket,
      mtdr,
      uuid,
      DigestUtils.sha1Hex(md.digest()),
      extractExifData(file),
      true);
  }

  @PostMapping("/file/{bucket}")
  public ObjectUpload handleFileUpload(
    @RequestParam("file") MultipartFile file,
    @PathVariable String bucket
  ) throws InvalidKeyException, NoSuchAlgorithmException, InvalidBucketNameException, ErrorResponseException,
    InternalException, InsufficientDataException, InvalidResponseException, MimeTypeException, XmlParserException,
    IOException, ServerException {
    //Authenticate before anything else
    handleAuthentication(bucket);

    // Safe get unique UUID
    UUID uuid = safeGenerateUuid();

    // We need access to the first bytes in a form that we can reset the InputStream
    ReadAheadInputStream prIs = ReadAheadInputStream.from(file.getInputStream(), READ_AHEAD_BUFFER_SIZE);

    MediaTypeDetectionStrategy.MediaTypeDetectionResult mtdr = mediaTypeDetectionStrategy
      .detectMediaType(prIs.getReadAheadBuffer(), file.getContentType(), file.getOriginalFilename());

    MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);

    storeFile(bucket, uuid, mtdr, new DigestInputStream(prIs.getInputStream(), md), false);

    String sha1Hex = DigestUtils.sha1Hex(md.digest());
    String fileExtension = mtdr.getEvaluatedMediaType();
    Map<String, String> exifData = extractExifData(file);

    boolean thumbnailIsSupported = thumbnailService.isSupported(fileExtension);

    ObjectUpload createdObjectUpload = transactionTemplate.execute(transactionStatus -> {
      // record the uploaded object to ensure we eventually get the metadata for it
      ObjectUpload objectUpload = createObjectUpload(file, bucket, mtdr, uuid, sha1Hex, exifData, false);

      if (thumbnailIsSupported) {
        UUID thumbnailID = generateUUID();
        objectUpload.setThumbnailIdentifier(thumbnailID);
        objectUploadService.update(objectUpload);
      }
      return objectUpload;
    });

    if (thumbnailIsSupported && createdObjectUpload != null) {
      log.info("Generating a thumbnail for file with UUID of: {}", createdObjectUpload::getFileIdentifier);
      // Create the thumbnail asynchronously so the client doesn't have to wait during file upload:
      thumbnailService.generateThumbnail(uuid, uuid.toString() + mtdr.getEvaluatedExtension(), fileExtension);
    }

    return createdObjectUpload;
  }

  /**
   * Triggers a download of a file. Note that the file requires a metadata entry in the database to be
   * available for download.
   *
   * @param bucket the bucket
   * @param fileId the file id
   * @return a response entity
   */
  @GetMapping("/file/{bucket}/{fileId}")
  public ResponseEntity<InputStreamResource> downloadObject(
    @PathVariable String bucket,
    @PathVariable String fileId
  ) {

    boolean thumbnailRequested = fileId.endsWith(".thumbnail");
    String fileUuidString = thumbnailRequested ? fileId.replaceAll(".thumbnail$", "") : fileId;
    UUID fileUuid = UUID.fromString(fileUuidString);

    try {
      Optional<ObjectStoreMetadata> loadedMetadata = objectStoreMetadataReadService
        .loadObjectStoreMetadataByFileId(fileUuid);

      ObjectStoreMetadata metadata = loadedMetadata
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
          "No metadata found for FileIdentifier " + fileUuid + " or bucket " + bucket, null));

      String filename = thumbnailRequested ?
        metadata.getFileIdentifier() + ".thumbnail" + ThumbnailService.THUMBNAIL_EXTENSION
        : metadata.getFilename();

      FileObjectInfo foi = minioService.getFileInfo(filename, bucket, false).orElseThrow(() -> {
        String errorMsg = messageSource.getMessage("minio.file_or_bucket_not_found",
          new Object[]{fileUuid, bucket}, LocaleContextHolder.getLocale());
        return new ResponseStatusException(HttpStatus.NOT_FOUND, errorMsg, null);
      });

      HttpHeaders respHeaders = getHttpHeaders(
        filename,
        thumbnailRequested ? "image/jpeg" : metadata.getDcFormat(),
        foi.getLength());

      InputStream is = minioService.getFile(filename, bucket, false)
        .orElseThrow(() -> getNotFoundException(bucket, fileId));

      InputStreamResource isr = new InputStreamResource(is);
      return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    } catch (IOException e) {
      log.warn("Can't download object", e);
    }

    throw new ResponseStatusException(
      HttpStatus.INTERNAL_SERVER_ERROR, null);
  }

  @GetMapping("/file/{bucket}/derivative/{fileId}")
  @Transactional
  public ResponseEntity<InputStreamResource> downloadDerivative(
    @PathVariable String bucket,
    @PathVariable String fileId
  ) throws IOException {
    UUID uuid = UUID.fromString(fileId);

    if (!objectUploadService.exists(ObjectUpload.class, uuid)) {
      throw getNotFoundException(bucket, fileId);
    }

    ObjectUpload uploadRecord = objectUploadService.findOne(uuid, ObjectUpload.class);

    String fileName = uploadRecord.getFileIdentifier() + uploadRecord.getEvaluatedFileExtension();
    InputStream is = minioService.getFile(fileName, bucket, true)
      .orElseThrow(() -> getNotFoundException(bucket, fileId));

    HttpHeaders headers = getHttpHeaders(
      fileName, uploadRecord.getDetectedMediaType(), uploadRecord.getSizeInBytes());
    return new ResponseEntity<>(new InputStreamResource(is), headers, HttpStatus.OK);
  }

  /**
   * Utility method to generate a NOT_FOUND ResponseStatusException based on the given parameters.
   *
   * @param bucket the bucket
   * @param fileId the file id
   * @return a ResponseStatusException Not found
   */
  private static ResponseStatusException getNotFoundException(String bucket, String fileId) {
    return new ResponseStatusException(
      HttpStatus.NOT_FOUND, "FileIdentifier " + fileId + " or bucket " + bucket + " Not Found", null);
  }

  /**
   * Utility method to generate HttpHeaders based on the given parameters
   *
   * @param filename      name of the file
   * @param mediaType     media type of the file
   * @param contentLength length of the file
   * @return HttpHeaders based on the given parameters
   */
  private static HttpHeaders getHttpHeaders(String filename, String mediaType, long contentLength) {
    HttpHeaders respHeaders = new HttpHeaders();
    respHeaders.setContentType(
      org.springframework.http.MediaType.parseMediaType(
        mediaType
      )
    );
    respHeaders.setContentLength(contentLength);
    respHeaders.setContentDispositionFormData("attachment", filename);
    return respHeaders;
  }

  /**
   * Stores a given input stream
   *
   * @param bucket       bucket to store the object
   * @param uuid         uuid of the object
   * @param mtdr         detected media result of the object
   * @param iStream      input stream of the object
   * @param isDerivative boolean indicating if the stored file is a derivative, this alters the object path to
   *                     be prefixed with /derivative.
   */
  private void storeFile(
    String bucket,
    UUID uuid,
    MediaTypeDetectionStrategy.MediaTypeDetectionResult mtdr,
    InputStream iStream,
    boolean isDerivative
  ) throws IOException, InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException,
    InvalidBucketNameException, InvalidResponseException, NoSuchAlgorithmException, XmlParserException, ServerException {
    // make bucket if it does not exist
    minioService.ensureBucketExists(bucket);

    minioService.storeFile(
      uuid.toString() + mtdr.getEvaluatedExtension(),
      iStream,
      mtdr.getEvaluatedMediaType(),
      bucket,
      isDerivative);
  }

  /**
   * Persists and returns an object upload based on the given parameters.
   *
   * @param file         file of the object upload
   * @param bucket       bucket of the file
   * @param mtdr         detected media result
   * @param uuid         uui of the file
   * @param sha1Hex      sha 1 hex of the file
   * @param exifData     exif data
   * @param isDerivative boolean indicating if the object was a derivative.
   * @return the persisted object upload
   */
  private ObjectUpload createObjectUpload(
    MultipartFile file,
    String bucket,
    MediaTypeDetectionStrategy.MediaTypeDetectionResult mtdr,
    UUID uuid,
    String sha1Hex,
    Map<String, String> exifData,
    boolean isDerivative
  ) {
    return objectUploadService.create(ObjectUpload.builder()
      .fileIdentifier(uuid)
      .createdBy(authenticatedUser.getUsername())
      .originalFilename(file.getOriginalFilename())
      .sha1Hex(sha1Hex)
      .receivedMediaType(file.getContentType())
      .detectedMediaType(Objects.toString(mtdr.getDetectedMediaType()))
      .detectedFileExtension(mtdr.getDetectedMimeType().getExtension())
      .evaluatedMediaType(mtdr.getEvaluatedMediaType())
      .evaluatedFileExtension(mtdr.getEvaluatedExtension())
      .sizeInBytes(file.getSize())
      .bucket(bucket)
      .exif(exifData)
      .isDerivative(isDerivative)
      .build());
  }

  /**
   * Returns a map of exif data if extraction is possible, otherwise an empty map is returned.
   *
   * @param file file to extract from
   * @return returns a map of exif data, or empty map.
   * @throws IOException if an error occurs reading the file
   */
  private Map<String, String> extractExifData(MultipartFile file) throws IOException {
    Map<String, String> exifData;
    try (InputStream exifIs = file.getInputStream()) {
      exifData = ExifParser.extractExifTags(exifIs);
    }
    return exifData;
  }

  /**
   * Ensures an authenticated user is present and authorized for a given bucket.
   *
   * @param bucket bucket to authorize.
   */
  private void handleAuthentication(String bucket) {
    checkAuthenticatedUser();
    authenticateBucket(bucket);
  }

  /**
   * Returns a generated UUID.
   *
   * @return Returns a generated UUID.
   */
  private UUID safeGenerateUuid() {
    UUID uuid = transactionTemplate.execute(transactionStatus -> generateUUID());
    if (uuid == null) {
      throw new IllegalStateException("Can't assign unique UUID.");
    }
    return uuid;
  }

  /**
   * Even if it's almost impossible, we need to make sure that the UUID is not already in use otherwise we
   * will overwrite the previous file.
   *
   * @return the generated UUID
   * @throws IllegalStateException if a uuid cannot be assigned.
   */
  private UUID generateUUID() throws IllegalStateException {
    int numberOfAttempt = 0;
    while (numberOfAttempt < MAX_NUMBER_OF_ATTEMPT_RANDOM_UUID) {
      UUID uuid = UUID.randomUUID();
      // this would be better with an exists() function
      if (objectUploadService.findOne(uuid, ObjectUpload.class) == null) {
        return uuid;
      }
      log.warn("Could not get a unique uuid for file");
      numberOfAttempt++;
    }
    throw new IllegalStateException("Can't assign unique UUID. Giving up.");
  }

  /**
   * Checks that there is an authenticatedUser available or throw a {@link AccessDeniedException}.
   */
  private void checkAuthenticatedUser() {
    if (authenticatedUser == null) {
      throw new AccessDeniedException("no authenticatedUser found");
    }
  }

  /**
   * Authenticates the DinaAuthenticatedUser for a given bucket.
   *
   * @param bucket - bucket to validate.
   * @throws UnauthorizedException If the DinaAuthenticatedUser does not have access to the given bucket
   */
  private void authenticateBucket(String bucket) {
    if (!authenticatedUser.getGroups().contains(bucket)) {
      throw new UnauthorizedException(
        "You are not authorized for bucket: " + bucket
          + ". Expected buckets: " + StringUtils.join(authenticatedUser.getGroups(), ", "));
    }
  }

}
