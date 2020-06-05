package ca.gc.aafc.objectstore.api.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

/**
 * A FolderStructureStrategy allows to return a folder structure based on a filename. The main
 * purpose is to avoid storing too many files in a single folder.
 * 
 * This class could be turned into an interface later if more than 1 strategy is implemented.
 * 
 * The current strategy will use the first 4 characters to determine 1 folder and 1 sub-folder.
 * "abdcefg.txt" will return the following path: "ab/dc/abdcefg.txt
 *
 */
@Service
public class FolderStructureStrategy {

  public Path getPathFor(String filename) {
    Objects.requireNonNull(filename, "filename shall be provided");
    Preconditions.checkArgument(filename.length() >= 4,
        "FolderStructureStrategy requires at least 4 characters:" + filename);
    Preconditions.checkArgument(StringUtils.isAlphanumeric(filename.substring(0, 4)),
        "FolderStructureStrategy requires the first 4 characters to be alphanumeric:" + filename);

    return Paths.get(filename.substring(0, 2), filename.substring(2, 4),
        filename);
  }

}
