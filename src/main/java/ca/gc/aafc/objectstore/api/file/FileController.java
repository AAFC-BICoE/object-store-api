package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.mapper.DinaMappingLayer;
import ca.gc.aafc.dina.repository.meta.AttributeMetaInfoProvider;
import ca.gc.aafc.dina.repository.meta.AttributeMetaInfoProvider.DinaJsonMetaInfo;
import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.objectstore.api.dto.ObjectUploadDto;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.ObjectUpload;
import ca.gc.aafc.objectstore.api.exif.ExifParser;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.service.DerivativeService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetadataReadService;
import ca.gc.aafc.objectstore.api.service.ObjectUploadService;
import io.crnk.core.exception.UnauthorizedException;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.executable.ExecutableParser;
import org.apache.tika.parser.pkg.CompressorParser;
import org.apache.tika.parser.pkg.PackageParser;
import org.apache.tika.parser.pkg.RarParser;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1")
@Log4j2
public class FileController {

  public static final String DIGEST_ALGORITHM = "SHA-1";
  private static final int MAX_NUMBER_OF_ATTEMPT_RANDOM_UUID = 5;
  private static final int READ_AHEAD_BUFFER_SIZE = 10 * 1024;
  private static final Set<MediaType> SUPPORTED_MEDIA_TYPE = getSupportedMediaTypesFromParsers(
      new ExecutableParser(), new CompressorParser(), new PackageParser(), new RarParser());

  private final DinaMappingLayer<ObjectUploadDto, ObjectUpload> mappingLayer;
  private final ObjectUploadService objectUploadService;
  private final DerivativeService derivativeService;
  private final MinioFileService minioService;
  private final ObjectStoreMetadataReadService objectStoreMetadataReadService;
  private final MediaTypeDetectionStrategy mediaTypeDetectionStrategy;
  private final MessageSource messageSource;

  // request scoped bean
  private final DinaAuthenticatedUser authenticatedUser;

  @Inject
  public FileController(
    MinioFileService minioService,
    ObjectUploadService objectUploadService,
    DerivativeService derivativeService,
    ObjectStoreMetadataReadService objectStoreMetadataReadService,
    MediaTypeDetectionStrategy mediaTypeDetectionStrategy,
    DinaAuthenticatedUser authenticatedUser,
    MessageSource messageSource
  ) {
    this.minioService = minioService;
    this.objectUploadService = objectUploadService;
    this.objectStoreMetadataReadService = objectStoreMetadataReadService;
    this.mediaTypeDetectionStrategy = mediaTypeDetectionStrategy;
    this.authenticatedUser = authenticatedUser;
    this.messageSource = messageSource;
    this.derivativeService = derivativeService;
    this.mappingLayer = new DinaMappingLayer<>(
      ObjectUploadDto.class, objectUploadService,
      new DinaMapper<>(ObjectUploadDto.class));
  }

  @PostMapping("/file/{bucket}/derivative")
  @Transactional
  public ObjectUploadDto handleDerivativeUpload(
    @RequestParam("file") MultipartFile file,
    @PathVariable String bucket
  ) throws IOException, MimeTypeException, NoSuchAlgorithmException, ServerException, ErrorResponseException,
    InternalException, XmlParserException, InvalidResponseException,
    InsufficientDataException, InvalidKeyException {
    return handleUpload(file, bucket, true);
  }

  @PostMapping("/file/{bucket}")
  @Transactional
  public ObjectUploadDto handleFileUpload(
    @RequestParam("file") MultipartFile file,
    @PathVariable String bucket
  ) throws InvalidKeyException, NoSuchAlgorithmException, ErrorResponseException,
    InternalException, InsufficientDataException, InvalidResponseException, MimeTypeException, XmlParserException,
    IOException, ServerException {
    return handleUpload(file, bucket, false);
  }

  private ObjectUploadDto handleUpload(
    @NonNull MultipartFile file,
    @NonNull String bucket,
    boolean isDerivative
  ) throws IOException, MimeTypeException, NoSuchAlgorithmException, InvalidKeyException, ErrorResponseException,
    InsufficientDataException, InternalException, InvalidResponseException,
    XmlParserException, ServerException {
    //Authenticate before anything else
    handleAuthentication(bucket);

    // Safe get unique UUID
    UUID uuid = generateUUID();

    // We need access to the first bytes in a form that we can reset the InputStream
    ReadAheadInputStream prIs = ReadAheadInputStream.from(file.getInputStream(), READ_AHEAD_BUFFER_SIZE);

    MediaTypeDetectionStrategy.MediaTypeDetectionResult mtdr = mediaTypeDetectionStrategy
      .detectMediaType(prIs.getReadAheadBuffer(), file.getContentType(), file.getOriginalFilename());

    MediaType detectedMediaType = mtdr.getDetectedMediaType();
    if (SUPPORTED_MEDIA_TYPE.contains(detectedMediaType)) {
      throw new UnsupportedMediaTypeStatusException(messageSource.getMessage(
        "supportedMediaType.illegal", new String[]{detectedMediaType.getSubtype()}, LocaleContextHolder.getLocale()));
    }
    MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);

    storeFile(bucket, uuid, mtdr, new DigestInputStream(prIs.getInputStream(), md), isDerivative);

    return createObjectUpload(
      file,
      bucket,
      mtdr,
      uuid,
      DigestUtils.sha1Hex(md.digest()),
      extractExifData(file),
      isDerivative);
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
    @PathVariable UUID fileId
  ) throws IOException {
    ObjectStoreMetadata metadata = objectStoreMetadataReadService
      .loadObjectStoreMetadataByFileId(fileId)
      .orElseThrow(() -> buildNotFoundException(bucket, Objects.toString(fileId)));

    // For the download of an object use the originalFilename provided (if possible)
    return download(bucket, metadata.getFilename(),
        generateDownloadFilename(metadata.getOriginalFilename(), metadata.getFilename(), metadata.getFileExtension()),
        false, metadata.getDcFormat());
  }

  @GetMapping("/file/{bucket}/derivative/{fileId}")
  public ResponseEntity<InputStreamResource> downloadDerivative(
    @PathVariable String bucket,
    @PathVariable UUID fileId
  ) throws IOException {
    Derivative derivative = derivativeService.findByFileId(fileId)
      .orElseThrow(() -> buildNotFoundException(bucket, Objects.toString(fileId)));
    String fileName = derivative.getFileIdentifier() + derivative.getFileExtension();
    return download(bucket, fileName, fileName, true, derivative.getDcFormat());
  }

  @GetMapping("/file/{bucket}/{fileId}/thumbnail")
  public ResponseEntity<InputStreamResource> downloadThumbNail(
    @PathVariable String bucket,
    @PathVariable UUID fileId
  ) throws IOException {
    ObjectStoreMetadata objectStoreMetadata = objectStoreMetadataReadService
      .loadObjectStoreMetadataByFileId(fileId)
      .orElseThrow(() -> buildNotFoundException(bucket, Objects.toString(fileId)));

    Derivative derivative = derivativeService.findThumbnailDerivativeForMetadata(objectStoreMetadata)
      .orElseThrow(() -> buildNotFoundException(bucket, Objects.toString(fileId)));
    return downloadDerivative(bucket, derivative.getFileIdentifier());
  }

  /**
   * Internal download function.
   * @param bucket name of the bucket where to find the file
   * @param fileName filename of the file in Minio
   * @param downloadFilename filename to use for the download
   * @param isDerivative used to look in the right subfolder in Minio
   * @param mediaType media type to include in the headers of the download
   * @return
   * @throws IOException
   */
  private ResponseEntity<InputStreamResource> download(
    @NonNull String bucket,
    @NonNull String fileName,
    @NonNull String downloadFilename,
    boolean isDerivative,
    @NonNull String mediaType
  ) throws IOException {
    FileObjectInfo foi = minioService.getFileInfo(fileName, bucket, isDerivative)
      .orElseThrow(() -> buildNotFoundException(bucket, fileName));
    InputStream is = minioService.getFile(fileName, bucket, isDerivative)
      .orElseThrow(() -> buildNotFoundException(bucket, fileName));

    return new ResponseEntity<>(
      new InputStreamResource(is),
      buildHttpHeaders(downloadFilename, mediaType, foi.getLength()),
      HttpStatus.OK);
  }

  /**
   * Utility method to generate a NOT_FOUND ResponseStatusException based on the given parameters.
   *
   * @param bucket the bucket
   * @param filename the name of the file
   * @return a ResponseStatusException Not found
   */
  private ResponseStatusException buildNotFoundException(String bucket, String filename) {
    return new ResponseStatusException(
      HttpStatus.NOT_FOUND,
      messageSource.getMessage(
        "minio.file_or_bucket_not_found", new Object[]{filename, bucket}, LocaleContextHolder.getLocale()),
      null);
  }

  /**
   * Utility method to generate HttpHeaders based on the given parameters
   *
   * @param filename      name of the file
   * @param mediaType     media type of the file
   * @param contentLength length of the file
   * @return HttpHeaders based on the given parameters
   */
  private static HttpHeaders buildHttpHeaders(String filename, String mediaType, long contentLength) {
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
    InvalidResponseException, NoSuchAlgorithmException, XmlParserException, ServerException {
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
  private ObjectUploadDto createObjectUpload(
    MultipartFile file,
    String bucket,
    MediaTypeDetectionStrategy.MediaTypeDetectionResult mtdr,
    UUID uuid,
    String sha1Hex,
    Map<String, String> exifData,
    boolean isDerivative
  ) {
    DinaJsonMetaInfo meta = null;
    if (objectUploadService.existsByProperty("sha1Hex", sha1Hex)) {
      meta =
        AttributeMetaInfoProvider.DinaJsonMetaInfo.builder()
        .warnings(Collections.singletonMap("duplicate_found", messageSource.getMessage("warnings.duplicate.Sha1Hex", null, LocaleContextHolder.getLocale())))
        .build();
    }
    ObjectUpload objectUpload = objectUploadService.create(ObjectUpload.builder()
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
    ObjectUploadDto dto = mapObjectUpload(objectUpload);
    dto.setMeta(meta);
    return dto;
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

  private ObjectUploadDto mapObjectUpload(ObjectUpload objectUpload) {
    return mappingLayer.toDtoSimpleMapping(objectUpload);
  }

  /**
   * Make sure a valid filename is generated for the download.
   *
   * @param originalFilename filename provided by the client at upload time
   * @param internalFilename name internal to the system made from the identifier
   * @param fileExtension file extension determined by the system including the dot (.)
   * @return
   */
  private String generateDownloadFilename(String originalFilename, String internalFilename, String fileExtension) {
    // if there is no original file name of the filename is just an extension
    if (StringUtils.isEmpty(originalFilename) || StringUtils.isEmpty(FilenameUtils.getBaseName(originalFilename))) {
      return internalFilename;
    }
    // use the internal extension since we are also returning the internal media type
    return FilenameUtils.getBaseName(originalFilename) + fileExtension;
  }

  /**
   * Return an immutable set of all the supported media type of the provided parsers.
   * @param parsers
   * @return
   */
  private static Set<MediaType> getSupportedMediaTypesFromParsers(Parser... parsers) {
    Set<MediaType> collection = new HashSet<>();
    Stream.of(parsers).map( p -> p.getSupportedTypes(null)).forEach(collection::addAll);
 
    return Set.copyOf(collection);
  }

}
