package ca.gc.aafc.objectstore.api.file;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class ThumbnailService {

  public static final int THUMBNAIL_WIDTH = 200;
  public static final int THUMBNAIL_HEIGHT = 200;
  public static final String THUMBNAIL_EXTENSION = ".jpg";
  public static final String THUMBNAIL_AC_SUB_TYPE = "THUMBNAIL";
  public static final DcType THUMBNAIL_DC_TYPE = DcType.IMAGE;
  public static final String SYSTEM_GENERATED = "System Generated";
  public static final String PDF_FILETYPE = "application/pdf";

  public InputStream generateThumbnail(InputStream sourceStream, String fileType) throws IOException {
    Builder<?> thumbnailBuilder;

    // PDFs are handled as a special case:
    if (PDF_FILETYPE.equals(fileType)) {
      try (PDDocument pDoc = PDDocument.load(sourceStream)) {
        PDFRenderer pdfRenderer = new PDFRenderer(pDoc);
        BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 72, ImageType.RGB);
        thumbnailBuilder = Thumbnails.of(bufferedImage);
      }
    } else {
      // Standard image use case:
      thumbnailBuilder = Thumbnails.of(sourceStream);
    }

    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      // Create the thumbnail:
      thumbnailBuilder
        .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
        .outputFormat("jpg")
        .toOutputStream(os);

      return new ByteArrayInputStream(os.toByteArray());
    }
  }

  public boolean isSupported(String fileType) {
    // PDFs are handled as a special case:
    if (PDF_FILETYPE.equals(fileType)) {
      return true;
    }

    return ImageIO.getImageReadersByMIMEType(fileType).hasNext();
  }

  /**
   * Returns a {@link ObjectStoreMetadataDto} for a thumbnail based of the given
   * parent resource and thumbnail identifier.
   * 
   * @param parent    - parent resource metadata of the thumbnail
   * @param thumbUuid - thumbnail identifier
   * @return {@link ObjectStoreMetadataDto} for the thumbnail
   */
  public static ObjectStoreMetadataDto generateThumbMetaData(ObjectStoreMetadataDto parent, UUID thumbUuid) {
    ObjectStoreMetadataDto thumbnailMetadataDto = new ObjectStoreMetadataDto();
    thumbnailMetadataDto.setFileIdentifier(thumbUuid);
    thumbnailMetadataDto.setAcDerivedFrom(parent);
    thumbnailMetadataDto.setDcType(THUMBNAIL_DC_TYPE);
    thumbnailMetadataDto.setAcSubType(THUMBNAIL_AC_SUB_TYPE);
    thumbnailMetadataDto.setBucket(parent.getBucket());
    thumbnailMetadataDto.setFileExtension(THUMBNAIL_EXTENSION);
    thumbnailMetadataDto.setOriginalFilename(parent.getOriginalFilename());
    thumbnailMetadataDto.setDcRights(parent.getDcRights());
    thumbnailMetadataDto.setXmpRightsOwner(parent.getXmpRightsOwner());
    thumbnailMetadataDto.setXmpRightsWebStatement(parent.getXmpRightsWebStatement());
    thumbnailMetadataDto.setXmpRightsUsageTerms(parent.getXmpRightsUsageTerms());
    thumbnailMetadataDto.setCreatedBy(SYSTEM_GENERATED);
    return thumbnailMetadataDto;
  }

}
