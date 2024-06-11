package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.objectstore.api.MainConfiguration;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@AllArgsConstructor
@Log4j2
public class ThumbnailGenerator {

  public static final int THUMBNAIL_WIDTH = 200;
  public static final int THUMBNAIL_HEIGHT = 200;
  public static final String THUMBNAIL_EXTENSION = ".jpg";
  public static final DcType THUMBNAIL_DC_TYPE = DcType.IMAGE;
  public static final String SYSTEM_GENERATED = "System Generated";
  public static final String PDF_MEDIA_TYPE = MediaType.APPLICATION_PDF_VALUE;
  public static final String TIFF_MEDIA_TYPE = "image/tiff";
  public static final String THUMB_DC_FORMAT = MediaType.IMAGE_JPEG_VALUE;

  private final MinioFileService minioService;

  @Transactional
  @Async(MainConfiguration.DINA_THREAD_POOL_BEAN_NAME)
  public void generateThumbnail(
    @NonNull UUID derivativeFileIdentifier,
    @NonNull String sourceFilename,
    @NonNull String sourceFileType,
    @NonNull String sourceBucket,
    boolean isSourceDerivative
  ) {

    String fileName = derivativeFileIdentifier + ThumbnailGenerator.THUMBNAIL_EXTENSION;

    try (
      InputStream originalFile = minioService
        .retrieveFile(sourceBucket, sourceFilename, isSourceDerivative)
        .orElseThrow(() -> new IllegalArgumentException("file not found: " + sourceFilename));
      ByteArrayOutputStream os = new ByteArrayOutputStream()
    ) {

      Builder<?> thumbnailBuilder;

      // PDFs are handled as a special case:
      if (PDF_MEDIA_TYPE.equals(sourceFileType)) {
        try (PDDocument pDoc = PDDocument.load(originalFile)) {
          PDFRenderer pdfRenderer = new PDFRenderer(pDoc);
          BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 72, ImageType.RGB);
          thumbnailBuilder = Thumbnails.of(bufferedImage);
        }
      } else {
        // Standard image use case:
        thumbnailBuilder = Thumbnails.of(originalFile);
      }

      // Create the thumbnail:
      thumbnailBuilder
        .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
        .outputFormat("jpg")
        .toOutputStream(os);

      try (ByteArrayInputStream thumbnail = new ByteArrayInputStream(os.toByteArray())) {
        minioService.storeFile(sourceBucket, fileName, true, MediaType.IMAGE_JPEG_VALUE, thumbnail);
      }

    } catch (IOException e) {
      log.warn(() -> "A thumbnail could not be generated for file " + sourceFilename, e);
    }
  }

  public void deleteThumbnail(UUID derivativeFileIdentifier, String bucket) throws IOException {
    String fileName = derivativeFileIdentifier + ThumbnailGenerator.THUMBNAIL_EXTENSION;
    minioService.deleteFile(bucket, fileName, true);
  }

  public static boolean isSupported(String fileType) {

    if (StringUtils.isBlank(fileType)) {
      return false;
    }

    // PDFs are handled as a special case:
    if (PDF_MEDIA_TYPE.equals(fileType)) {
      return true;
    }

    //skip tiff for now since cr2 is detected as tiff and the thumbnail will not work
    if (TIFF_MEDIA_TYPE.equals(fileType)) {
      return false;
    }

    return ImageIO.getImageReadersByMIMEType(fileType).hasNext();
  }
}
