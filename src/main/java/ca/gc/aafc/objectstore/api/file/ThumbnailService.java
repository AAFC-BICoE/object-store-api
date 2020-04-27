package ca.gc.aafc.objectstore.api.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import net.coobird.thumbnailator.Thumbnails;

@Service
public class ThumbnailService {

  public static final int THUMBNAIL_WIDTH = 200;
  public static final int THUMBNAIL_HEIGHT = 200;
  public static final String THUMBNAIL_EXTENSION = ".jpg";
  public static final String THUMBNAIL_AC_SUB_TYPE = "THUMBNAIL";
  public static final DcType THUMBNAIL_DC_TYPE = DcType.IMAGE;

  public InputStream generateThumbnail(InputStream sourceImageStream) throws IOException {
    // Copy to a temp file; The source image must be a file for thumbnailator to
    // generate the thumbnail.
    File tempSourceFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
    FileUtils.copyToFile(sourceImageStream, tempSourceFile);

    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      // Create the thumbnail:
      Thumbnails.of(tempSourceFile)
        .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
        .outputFormat("jpg")
        .toOutputStream(os);

      FileUtils.deleteQuietly(tempSourceFile);

      ByteArrayInputStream thumbnail = new ByteArrayInputStream(os.toByteArray());
      return thumbnail;
    }
  }

  public boolean isSupported(String extension) {
    return ImageIO.getImageReadersByMIMEType(extension).hasNext();
  }

  /**
   * Returns a {@link ObjectStoreMetadataDto} for a thumbnail based off the given
   * parent resource and thumbnail identifier.
   * 
   * @param parent  - parent resource metadata of the thumbnail
   * @param thumbUuid - thumbnail identifier
   * @return {@link ObjectStoreMetadataDto} for the thumbnail
   */
  public static ObjectStoreMetadata generateThumbMetaData(ObjectStoreMetadata parent, UUID thumbUuid) {
    ObjectStoreMetadata thumbnailMetadataDto = new ObjectStoreMetadata();
    thumbnailMetadataDto.setFileIdentifier(thumbUuid);
    thumbnailMetadataDto.setAcDerivedFrom(parent);
    thumbnailMetadataDto.setDcType(THUMBNAIL_DC_TYPE);
    thumbnailMetadataDto.setBucket(parent.getBucket());
    thumbnailMetadataDto.setFileExtension(THUMBNAIL_EXTENSION);
    thumbnailMetadataDto.setOriginalFilename(parent.getOriginalFilename());
    thumbnailMetadataDto.setDcRights(parent.getDcRights());
    thumbnailMetadataDto.setXmpRightsOwner(parent.getXmpRightsOwner());
    thumbnailMetadataDto.setXmpRightsWebStatement(parent.getXmpRightsWebStatement());
    return thumbnailMetadataDto;
  }

}
