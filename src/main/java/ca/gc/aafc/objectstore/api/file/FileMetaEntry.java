package ca.gc.aafc.objectstore.api.file;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents metadata about an uploaded file.
 * This class could be versioned in the future. 
 *
 */
@Getter
public class FileMetaEntry {
  
  public static final String VERSION_1_0 = "1.0";
  public static final String SUFFIX = "_meta.json";
  
  private String metaFileEntryVersion = VERSION_1_0;
  
  private final UUID fileIdentifier;
  
  @JsonCreator
  public FileMetaEntry(UUID fileIdentifier) {
    this.fileIdentifier = fileIdentifier;
  }
  
  @Setter
  private String originalFilename;
  
  @Setter
  private String sha1Hex;
  
  @Setter
  private String receivedMediaType;
  
  @Setter
  private String detectedMediaType;
  
  @Setter
  private String detectedFileExtension;
  
  @Setter
  private String evaluatedMediaType;
  
  @Setter
  private String evaluatedFileExtension;

  @Setter
  private long sizeInBytes;

  @Setter
  private UUID thumbnailIdentifier;

  @JsonIgnore
  public String getFileMetaEntryFilename() {
    return fileIdentifier + SUFFIX;
  }

}
