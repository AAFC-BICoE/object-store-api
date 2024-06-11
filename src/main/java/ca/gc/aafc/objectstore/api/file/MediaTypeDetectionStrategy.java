package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.objectstore.api.config.MediaTypeConfiguration;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * A MediaTypeDetectionStrategy allows to determine the media type based on an InputStream and information
 * provided by the user.
 * 
 * This class could be turned into an interface later if more than 1 strategy is implemented.
 * 
 * The current strategy will always detect the MediaType from the InputStream and the file extension will be changed 
 * accordingly unless the detected media type is too generic (OCTET_STREAM or TEXT_PLAIN). In such case, the provided MediaType/file extension will
 * be used (if provided).
 *
 */
@Service
public class MediaTypeDetectionStrategy {

  private static final Detector TIKA_DETECTOR = new DefaultDetector();

  private final MediaTypeConfiguration mediaTypeConfiguration;

  public MediaTypeDetectionStrategy(@NonNull MediaTypeConfiguration mediaTypeConfiguration) {
    this.mediaTypeConfiguration = mediaTypeConfiguration;
  }
  
  @Builder
  @Getter
  public static class MediaTypeDetectionResult {

    private final String receivedMediaType;
    private final String receivedFileName;

    private final org.apache.tika.mime.MediaType detectedMediaType;
    private final org.apache.tika.mime.MimeType detectedMimeType;
    
    private final String evaluatedMediaType;
    private final String evaluatedExtension;

    public boolean isKnownExtensionForMediaType() {
      return MediaTypeDetectionStrategy.isKnownExtensionForMediaType(receivedFileName, detectedMimeType);
    }
  }

  private static boolean isKnownExtensionForMediaType(String receivedFileName, MimeType detectedMimeType) {
    if (extractFileExtension(receivedFileName) == null || detectedMimeType == null) {
      return false;
    }
    return detectedMimeType.getExtensions().stream()
        .anyMatch(s -> s.equalsIgnoreCase(extractFileExtension(receivedFileName)));
  }

  /**
   * Tries to extract the extension from the provided filename and return null if not possible
   * @param filename filename with extension. null-safe
   * @return
   */
  private static String extractFileExtension(String filename) {
    if (StringUtils.isBlank(filename) || StringUtils.isBlank(FilenameUtils.getExtension(filename))) {
      return null;
    }
    return "." + FilenameUtils.getExtension(filename).toLowerCase();
  }
  
  /**
   * Detect the MediaType and MimeType of an InputStream by reading the beginning of the stream.
   * A new InputStream is returned to make sure the caller can read the entire stream from the beginning.
   * 
   * @param is will be consumed but not closed
   * @param receivedMediaType
   * @param originalFilename
   * @return MediaTypeDetectionResult
   * @throws IOException
   * @throws MimeTypeException
   */
  public MediaTypeDetectionResult detectMediaType(@NonNull InputStream is,
      String receivedMediaType, String originalFilename)
      throws IOException, MimeTypeException {

    Metadata metadata = new Metadata();
    if (StringUtils.isNotBlank(originalFilename)) {
      metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, originalFilename);
    }

    MediaType detectedMediaType = TIKA_DETECTOR.detect(TikaInputStream.get(is), metadata);
    MimeType detectedMimeType = mediaTypeConfiguration.mimeTypeFromName(detectedMediaType.toString());

    MediaTypeDetectionResult.MediaTypeDetectionResultBuilder mtdrBldr = MediaTypeDetectionResult
        .builder()
        .receivedMediaType(receivedMediaType)
        .receivedFileName(originalFilename)
        .detectedMediaType(detectedMediaType)
        .detectedMimeType(detectedMimeType);
    
    // Decide on the MediaType and extension that should be used    
    mtdrBldr.evaluatedMediaType(evaluateMediaType(receivedMediaType, originalFilename, detectedMediaType ,
        detectedMimeType));
    
    mtdrBldr.evaluatedExtension(
        extractFileExtension(originalFilename) == null ? detectedMimeType.getExtension()
            : extractFileExtension(originalFilename));
    
    return mtdrBldr.build();
  }

  /**
   * Method responsible to find the best media type possible.
   * @param receivedMediaType
   * @param receivedFilename
   * @param detectedMediaType
   * @param detectedMimeType
   * @return
   */
  private static String evaluateMediaType(String receivedMediaType, String receivedFilename, MediaType detectedMediaType,
      MimeType detectedMimeType) {
    // if we received nothing, use the detected
    if (StringUtils.isBlank(receivedMediaType)) {
      return detectedMediaType.toString();
    }

    // if the received type is generic but we detect something more specific and the extension is matching
    if(MediaType.OCTET_STREAM.toString().equals(receivedMediaType) && !MediaType.OCTET_STREAM.equals(detectedMediaType)) {
      if(isKnownExtensionForMediaType(receivedFilename, detectedMimeType)) {
        return detectedMediaType.toString();
      }
    }

    return receivedMediaType;
  }

  /**
   * Protection against CT_CONSTRUCTOR_THROW
   */
  @Override
  protected final void finalize(){
    // no-op
  }

}
