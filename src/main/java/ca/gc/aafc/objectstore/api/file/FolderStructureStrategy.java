package ca.gc.aafc.objectstore.api.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

/**
 * A FolderStructureStrategy allows to return a folder structure based on a filename. The main purpose
 * of this implementation is to avoid storing too many files in a single folder and storing derivatives in
 * a separate root folder to simplify backup rules.
 * 
 * This class could be turned into an interface later if more than 1 strategy is implemented.
 * 
 * The current strategy will use the first 4 characters to determine 1 folder and 1 sub-folder.
 * "abdcefg.txt" will return the following path: "ab/dc/abdcefg.txt
 *
 */
@Service
public class FolderStructureStrategy {

  private static final String DERIVATIVES_ROOT_FOLDER = "derivatives";

  /**
   * Get a {@link Path} for the provided filename
   * @param filename name of the file that contains more than 4 alphanumerical characters
   * @param isDerivative is the file considered a derivative that could be recreated from a primary object
   * @return the path
   */
  public Path getPathFor(String filename, boolean isDerivative) {
    Objects.requireNonNull(filename, "filename shall be provided");
    Preconditions.checkArgument(filename.length() >= 4,
        "FolderStructureStrategy requires at least 4 characters:" + filename);
    Preconditions.checkArgument(StringUtils.isAlphanumeric(filename.substring(0, 4)),
        "FolderStructureStrategy requires the first 4 characters to be alphanumeric:" + filename);

    Path path = Paths.get(filename.substring(0, 2), filename.substring(2, 4),
        filename);

    if (isDerivative) {
      return Paths.get(DERIVATIVES_ROOT_FOLDER).resolve(path);
    }

    return path;
  }

}
