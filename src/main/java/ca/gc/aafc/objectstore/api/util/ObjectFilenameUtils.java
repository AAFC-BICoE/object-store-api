package ca.gc.aafc.objectstore.api.util;

import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

/**
 * Utility class to help with filename generation for downloads or exports.
 */
public final class ObjectFilenameUtils {

  // Accepted : alphanumerical (with Unicode) and _ - [] () .
  public static final Pattern FILENAME_PATTERN = Pattern.compile("[^\\p{L}\\p{N}_\\-\\[\\](). ]");


  public static final String FOLDER_FIRST_CHARS_TO_REMOVE = "^[^a-zA-Z0-9]+";

  // Accepted : alphanumerical and _ - [] () /
  public static final Pattern FOLDERNAME_PATTERN = Pattern.compile("[^\\w\\-\\[\\]()/]");

  private ObjectFilenameUtils() {
    // utility class
  }

  /**
   * Standardize filename to make sure we can download the file in all known operating systems.
   * The regex accepts less than what is technically accepted to keep things simple.
   * @param userSubmittedFilename
   * @return
   */
  public static String standardizeFilename(String userSubmittedFilename) {
    if (StringUtils.isBlank(userSubmittedFilename)) {
      return userSubmittedFilename;
    }
    return FILENAME_PATTERN.matcher(userSubmittedFilename).replaceAll("_");
  }

  /**
   * Standardize folder name to make sure we can extract the file in most operating systems.
   * The regex accepts less than what is technically accepted to keep things simple.
   * @param userSubmittedFolderName
   * @return
   */
  public static String standardizeFolderName(String userSubmittedFolderName) {
    if (StringUtils.isBlank(userSubmittedFolderName)) {
      return userSubmittedFolderName;
    }

    // make sure the folder name doesn't start with an invalid char (e.g. / or ~) so we have a
    // relative path
    String noInvalidLeadingChar =
      userSubmittedFolderName.replaceFirst(FOLDER_FIRST_CHARS_TO_REMOVE, "");

    if (StringUtils.isBlank(noInvalidLeadingChar)) {
      return "";
    }

    return
      StringUtils.appendIfMissing(
        FOLDERNAME_PATTERN.matcher(noInvalidLeadingChar).replaceAll("_"), "/");
  }

  /**
   * Generate a filename (with file extension) for a derivative. Since Derivatives don't have a name we are trying to use the name
   * of the derivedFrom and add the derivative type as suffix. We will use a fallback on derivative's uuid.
   * @param derivative non null
   * @return a filename like myUploadedImage_thumbnail.jpg
   */
  public static String generateDerivativeFilename(Derivative derivative) {
    Objects.requireNonNull(derivative);

    ObjectStoreMetadata derivedFrom = derivative.getAcDerivedFrom();
    // make sure there is a derivedFrom and that it has a filename
    if (derivedFrom != null && StringUtils.isNotEmpty(FilenameUtils.getBaseName(derivedFrom.getOriginalFilename()))) {
      String derivativeSuffix =
        derivative.getDerivativeType() != null ? derivative.getDerivativeType().getSuffix() :
          "derivative";
      // generate a name from the originalFilename + the generated suffix + the derivative file extension (since it might be different from the original)
      return FilenameUtils.getBaseName(derivedFrom.getOriginalFilename()) + "_" + derivativeSuffix + derivative.getFileExtension();
    }
    //fallback, use the internal name
    return derivative.getFilename();
  }

  /**
   * Make sure a valid filename is generated for the download.
   *
   * @param mainObject
   * @return
   */
  public static String generateMainObjectFilename(ObjectStoreMetadata mainObject) {
    Objects.requireNonNull(mainObject);

    String originalFilename = mainObject.getOriginalFilename();

    // if there is no original file name of the filename is just an extension
    if (StringUtils.isEmpty(originalFilename) || StringUtils.isEmpty(FilenameUtils.getBaseName(originalFilename))) {
      return mainObject.getFilename();
    }
    // use the internal extension since we are also returning the internal media type
    return FilenameUtils.getBaseName(originalFilename) + mainObject.getFileExtension();
  }

  /**
   * Insert a specific string just before the extensions in the filename.
   * @param filename current filename including extension
   * @param toInsert string to insert in the filename just before the extension
   * @return
   */
  public static String insertBeforeFileExtension(String filename, String toInsert) {
    Objects.requireNonNull(filename);
    Objects.requireNonNull(toInsert);

    StringBuilder newFilename = new StringBuilder(filename);
    newFilename.insert(FilenameUtils.indexOfExtension(filename), toInsert);
    return newFilename.toString();
  }
}
