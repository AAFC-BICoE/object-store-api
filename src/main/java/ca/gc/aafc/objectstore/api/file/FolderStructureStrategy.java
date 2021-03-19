package ca.gc.aafc.objectstore.api.file;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * A FolderStructureStrategy allows to return a folder structure based on a filename. The main purpose is to
 * avoid storing too many files in a single folder.
 * <p>
 * This class could be turned into an interface later if more than 1 strategy is implemented.
 * <p>
 * The current strategy will use the first 4 characters to determine 1 folder and 1 sub-folder. "abdcefg.txt"
 * will return the following path: "ab/dc/abdcefg.txt
 * <p>
 * The current strategy will allow any leading path structures untouched. Ex. "myLeadingPath/aabb.txt" will
 * translate to "myLeadingPath/aa/bb/aabb.txt"
 */
@Service
public class FolderStructureStrategy {

  private static final String FOLDER_SEPARATOR = "/";

  public Path getPathFor(String filename) {
    Objects.requireNonNull(filename, "filename shall be provided");

    String folderPrefix;
    String fileNameToProcess;
    int folderSeparatorIndex = filename.lastIndexOf(FOLDER_SEPARATOR);
    if (folderSeparatorIndex != -1) {
      folderPrefix = filename.substring(0, folderSeparatorIndex);
      fileNameToProcess = filename.substring(folderSeparatorIndex + 1);
    } else {
      folderPrefix = "";
      fileNameToProcess = filename;
    }

    Preconditions.checkArgument(fileNameToProcess.length() >= 4,
        "FolderStructureStrategy requires at least 4 characters:" + fileNameToProcess);
    Preconditions.checkArgument(StringUtils.isAlphanumeric(fileNameToProcess.substring(0, 4)),
        "FolderStructureStrategy requires the first 4 characters to be alphanumeric:" + fileNameToProcess);

    return Paths.get(
      folderPrefix,
      fileNameToProcess.substring(0, 2),
      fileNameToProcess.substring(2, 4),
      fileNameToProcess);
  }

}
