package ca.gc.aafc.objectstore.api.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.xmlpull.v1.XmlPullParserException;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetadataReadService;
import io.crnk.core.exception.UnauthorizedException;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/v1")
@Log4j2
public class FileController {

  public static final String DIGEST_ALGORITHM = "SHA-1";
  private static final int MAX_NUMBER_OF_ATTEMPT_RANDOM_UUID = 5;

  private final MinioFileService minioService;
  private final ObjectStoreMetadataReadService objectStoreMetadataReadService;
  private final MediaTypeDetectionStrategy mediaTypeDetectionStrategy;
  private final ObjectMapper objectMapper;
  private final ThumbnailService thumbnailService;
  private Optional<DinaAuthenticatedUser> authenticatedUser;

  @Inject
  public FileController(MinioFileService minioService, ObjectStoreMetadataReadService objectStoreMetadataReadService, 
      MediaTypeDetectionStrategy mediaTypeDetectionStrategy,
      Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder,
      ThumbnailService thumbnailService,
      Optional<DinaAuthenticatedUser> authenticatedUser
  ) {
    this.minioService = minioService;
    this.objectStoreMetadataReadService = objectStoreMetadataReadService;
    this.mediaTypeDetectionStrategy = mediaTypeDetectionStrategy;
    this.thumbnailService = thumbnailService;
    this.authenticatedUser = authenticatedUser;
    this.objectMapper = jackson2ObjectMapperBuilder.build();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @PostMapping("/file/{bucket}")
  public FileMetaEntry handleFileUpload(@RequestParam("file") MultipartFile file,
      @PathVariable String bucket) throws InvalidKeyException, NoSuchAlgorithmException,
      InvalidBucketNameException, NoResponseException, ErrorResponseException, InternalException,
      InvalidArgumentException, InsufficientDataException, InvalidResponseException,
      RegionConflictException, InvalidEndpointException, InvalidPortException, IOException,
      XmlPullParserException, URISyntaxException, MimeTypeException {

    // Temporary, we will need to check if the user is an admin
    minioService.ensureBucketExists(bucket);

    // Check that the UUID is not already assigned.
    UUID uuid = getNewUUID(bucket);

    authenticateBucket(bucket);

    MediaTypeDetectionStrategy.MediaTypeDetectionResult mtdr = mediaTypeDetectionStrategy
        .detectMediaType(file.getInputStream(), file.getContentType(), file.getOriginalFilename());

    FileMetaEntry fileMetaEntry = new FileMetaEntry(uuid);
    fileMetaEntry.setOriginalFilename(file.getOriginalFilename());
    fileMetaEntry.setReceivedMediaType(file.getContentType());
    
    fileMetaEntry.setDetectedMediaType(Objects.toString(mtdr.getDetectedMediaType()));
    fileMetaEntry.setDetectedFileExtension(mtdr.getDetectedMimeType().getExtension());
    
    fileMetaEntry.setEvaluatedMediaType(mtdr.getEvaluatedMediatype());
    fileMetaEntry.setEvaluatedFileExtension(mtdr.getEvaluatedExtension());

    fileMetaEntry.setSizeInBytes(file.getSize());

    // Decorate the InputStream in order to compute the hash
    MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
    DigestInputStream dis = new DigestInputStream(mtdr.getInputStream(), md);
    
    minioService.storeFile(
      uuid.toString() + mtdr.getEvaluatedExtension(),
      dis,
      mtdr.getEvaluatedMediatype(),
      bucket,
      null
    );
    
    String sha1Hex = DigestUtils.sha1Hex(md.digest());
    fileMetaEntry.setSha1Hex(sha1Hex);

    UUID thumbUuid = generateThumbNail(
      uuid,
      file.getInputStream(),
      bucket,
      mtdr.getEvaluatedMediatype());
    fileMetaEntry.setThumbnailIdentifier(thumbUuid);

    storeFileMetaEntry(fileMetaEntry, bucket);

    return fileMetaEntry;
  }

  /**
   * Stores a generated thumbnail and returns the Identifier or Null if a
   * thumbnail could not be generated.
   * 
   * @param fileID
   *                        - UUID of the original file the thumbnail uses.
   * 
   * @param in
   *                        - image input stream
   * @param bucket
   *                        - bucket to store thumbnail
   * @param fileExtension
   *                        - file extension of the image
   * @return - UUID of the stored thumbnail, or null
   */
  @SneakyThrows
  private UUID generateThumbNail(UUID fileID, InputStream in, String bucket, String fileExtension) {
    if (thumbnailService.isSupported(fileExtension)) {
      log.info("Generating a thumbnail for file with UUID of: {}", () -> fileID);

      try (InputStream thumbnail = thumbnailService.generateThumbnail(in)) {
        UUID thumbID = getNewUUID(bucket);
        String fileName = thumbID.toString() + ".thumbnail" + ThumbnailService.THUMBNAIL_EXTENSION;
        minioService.storeFile(fileName, thumbnail, "image/jpeg", bucket, null);
        return thumbID;
      } catch (IOException e) {
        log.warn(() -> "A thumbnail could not be generated for file " + fileID, e);
      }
    }
    return null;
  }

  /**
   * Store a {@link FileMetaEntry} in Minio as a json file.
   * 
   * @param fileMetaEntry
   * @param bucket
   * @throws InvalidKeyException
   * @throws NoSuchAlgorithmException
   * @throws InvalidBucketNameException
   * @throws NoResponseException
   * @throws ErrorResponseException
   * @throws InternalException
   * @throws InvalidArgumentException
   * @throws InsufficientDataException
   * @throws InvalidResponseException
   * @throws RegionConflictException
   * @throws InvalidEndpointException
   * @throws InvalidPortException
   * @throws IOException
   * @throws XmlPullParserException
   * @throws URISyntaxException
   */
  private void storeFileMetaEntry(FileMetaEntry fileMetaEntry, String bucket)
      throws InvalidKeyException, NoSuchAlgorithmException, InvalidBucketNameException,
      NoResponseException, ErrorResponseException, InternalException, InvalidArgumentException,
      InsufficientDataException, InvalidResponseException, RegionConflictException,
      InvalidEndpointException, InvalidPortException, IOException, XmlPullParserException,
      URISyntaxException {
    
    String jsonContent = objectMapper.writeValueAsString(fileMetaEntry);
    InputStream inputStream = new ByteArrayInputStream(
        jsonContent.getBytes(StandardCharsets.UTF_8));
    minioService.storeFile(fileMetaEntry.getFileMetaEntryFilename().toString(), 
        inputStream, fileMetaEntry.getDetectedMediaType(), bucket, null);
  }
  
  /**
   * Triggers a download of a file. Note that the file requires a metadata entry in the database to
   * be available for download.
   * 
   * @param bucket
   * @param fileId
   * @return
   * @throws IOException
   */
  @GetMapping("/file/{bucket}/{fileId}")
  public ResponseEntity<InputStreamResource> downloadObject(@PathVariable String bucket,
      @PathVariable String fileId) throws IOException {

    authenticateBucket(bucket);

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
    
      FileObjectInfo foi = minioService.getFileInfo(filename, bucket)
        .orElseThrow(() -> new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          fileUuid + " or bucket " + bucket + " Not Found",
          null
        ));
      
      HttpHeaders respHeaders = new HttpHeaders();
      respHeaders.setContentType(
        org.springframework.http.MediaType.parseMediaType(
          thumbnailRequested ? "image/jpeg" : metadata.getDcFormat()
        )
      );
      respHeaders.setContentLength(foi.getLength());
      respHeaders.setContentDispositionFormData("attachment", filename);

      InputStream is = minioService.getFile(filename, bucket).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
          "FileIdentifier " + fileUuid + " or bucket " + bucket + " Not Found", null));

      InputStreamResource isr = new InputStreamResource(is);
      return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
    } catch (IOException e) {
      log.warn("Can't download object", e);
    }
   
    throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR, null);
  }
  
  private UUID getNewUUID(String bucketName) throws IllegalStateException, IOException {
    int numberOfAttempt = 0;
    while (numberOfAttempt < MAX_NUMBER_OF_ATTEMPT_RANDOM_UUID) {
      UUID uuid = UUID.randomUUID();
      if (!minioService.isFileWithPrefixExists(bucketName, uuid.toString())) {
        return uuid;
      }
      log.warn("Could not get a uuid for file in bucket :{}", () -> bucketName);
      numberOfAttempt++;
    }
    throw new IllegalStateException("Can't assign unique UUID. Giving up.");
  }

  /**
   * Authenticates the DinaAuthenticatedUser for a given bucket.
   * 
   * @param bucket
   *                 - bucket to validate.
   * @throws UnauthorizedException
   *                                 If the DinaAuthenticatedUser does not have
   *                                 access to the given bucket
   */
  private void authenticateBucket(String bucket) {
    if (authenticatedUser.isPresent() && !authenticatedUser.get().getGroups().contains(bucket)) {
      throw new UnauthorizedException(
          "You are not authorized for bucket: " + bucket
          + ". Expected buckets: " + StringUtils.join(authenticatedUser.get().getGroups(), ", "));
    }
  }

}
