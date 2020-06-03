package ca.gc.aafc.objectstore.api.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.stereotype.Service;

import lombok.Builder;
import lombok.Getter;

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
  
  private static final TikaConfig TIKA_CONFIG = TikaConfig.getDefaultConfig();
  private static final Detector TIKA_DETECTOR = new DefaultDetector();
  
  @Builder
  @Getter
  public static class MediaTypeDetectionResult {
    private InputStream inputStream;

    private String receivedMediaType;
    private String receivedFileName;

    private org.apache.tika.mime.MediaType detectedMediaType;
    private org.apache.tika.mime.MimeType detectedMimeType;
    
    private String evaluatedMediatype;
    private String evaluatedExtension;

    public boolean isKnownExtensionForMediaType() {
      if (StringUtils.isBlank(getFileExtension(receivedFileName)) || detectedMimeType == null) {
        return false;
      }
      return detectedMimeType.getExtensions().stream()
          .filter(s -> s.equalsIgnoreCase(getFileExtension(receivedFileName))).findFirst().isPresent();
    }
  }
  
  private static String getFileExtension(@Nullable String filename) {
    return  StringUtils.isBlank(filename) ? null : "." + FilenameUtils.getExtension(filename);
  }
  
  /**
   * Detect the MediaType and MimeType of an InputStream by reading the beginning of the stream.
   * A new InputStream is returned to make sure the caller can read the entire stream from the beginning.
   * 
   * @param is
   * @param receivedMediaType
   * @param originalFilename
   * @return
   * @throws IOException
   * @throws MimeTypeException
   */
  public MediaTypeDetectionResult detectMediaType(InputStream is,
      @Nullable String receivedMediaType, @Nullable String originalFilename)
      throws IOException, MimeTypeException {
    
    Objects.requireNonNull(is);

    // Read the beginning of the Stream to allow Tika to detect the mediaType
    byte[] buffer = new byte[10 * 1024];
    int lenght = is.read(buffer);
    ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, lenght);
    
    Metadata metadata = new Metadata();
    if (StringUtils.isNotBlank(originalFilename)) {
      metadata.set(Metadata.RESOURCE_NAME_KEY, originalFilename);
    }

    MediaType detectedMediaType = TIKA_DETECTOR.detect(TikaInputStream.get(bais), metadata);
    MimeType detectedMimeType = TIKA_CONFIG.getMimeRepository().forName(detectedMediaType.toString());

    MediaTypeDetectionResult.MediaTypeDetectionResultBuilder mtdrBldr = MediaTypeDetectionResult
        .builder()
        .inputStream(new SequenceInputStream(bais, is))
        .receivedMediaType(receivedMediaType)
        .receivedFileName(originalFilename)
        .detectedMediaType(detectedMediaType)
        .detectedMimeType(detectedMimeType);
    
    // Decide on the MediaType and extension that should be used    
    mtdrBldr.evaluatedMediatype(
        StringUtils.isBlank(receivedMediaType) ? detectedMediaType.toString() : receivedMediaType);
    
    mtdrBldr.evaluatedExtension(
        getFileExtension(originalFilename) == null ? detectedMimeType.getExtension()
            : getFileExtension(originalFilename));
    
    return mtdrBldr.build();
  }

}
