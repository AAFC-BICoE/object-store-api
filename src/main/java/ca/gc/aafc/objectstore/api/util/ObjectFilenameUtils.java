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

  // Accepted : alphanumerical (with Unicode) and _ - [] () . +
  public static final Pattern FILENAME_PATTERN = Pattern.compile("[^\\p{L}\\p{N}_\\-\\[\\]().+ ]");

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
   * Generate a filename (with file extension) for a derivative.
   * Try to use the alias or the derivative filename is available. If not, try to use the name
   * of the derivedFrom and add the derivative type as suffix. We will use a fallback on derivative's uuid.
   * @param derivative non null
   * @param filenameAlias optional, an alias to use instead of the filename of the derivedFrom
   * @return a filename like myUploadedImage_thumbnail.jpg
   */
  public static String generateDerivativeFilename(Derivative derivative, String filenameAlias) {
    Objects.requireNonNull(derivative);

    // if the derivative has an alias or filename
    String filename = StringUtils.firstNonBlank(filenameAlias, derivative.getFilename());
    if (StringUtils.isNotEmpty(filename) && StringUtils.isNotEmpty(FilenameUtils.getBaseName(filename))) {
      // use the internal extension since we are also returning the internal media type
      return FilenameUtils.getBaseName(standardizeFilename(filename)) + derivative.getFileExtension();
    }

    // If not, try with the derivedFrom
    ObjectStoreMetadata derivedFrom = derivative.getAcDerivedFrom();
    // make sure there is a derivedFrom and that it has a filename
    if (derivedFrom != null) {
      String derivativeSuffix =
        derivative.getDerivativeType() != null ? derivative.getDerivativeType().getSuffix() :
          "derivative";

      String derivedFromFilename = StringUtils.firstNonBlank(derivedFrom.getFilename(), derivedFrom.getOriginalFilename());
      if (StringUtils.isNotEmpty(derivedFromFilename) && StringUtils.isNotEmpty(FilenameUtils.getBaseName(derivedFromFilename))) {
        // generate a name from the originalFilename + the generated suffix + the derivative file extension (since it might be different from the original)
        return FilenameUtils.getBaseName(standardizeFilename(derivedFromFilename)) + "_" + derivativeSuffix + derivative.getFileExtension();
      }
    }
    //fallback, use the internal name
    return derivative.getInternalFilename();
  }

  /**
   * Make sure a valid filename is generated for the download.
   * Return the first candidate of : alias, filename, original filename, internal filename
   *
   * @param mainObject
   * @param filenameAlias optional, an alias to use instead of the filename
   * @return generated filename including file extension
   */
  public static String generateMainObjectFilename(ObjectStoreMetadata mainObject, String filenameAlias) {
    Objects.requireNonNull(mainObject);

    // get the first available value in order of priority
    String filename = StringUtils.firstNonBlank(filenameAlias, mainObject.getFilename(), mainObject.getOriginalFilename());

    // if there is no filename or the filename is just an extension
    if (StringUtils.isEmpty(filename) || StringUtils.isEmpty(FilenameUtils.getBaseName(filename))) {
      return mainObject.getInternalFilename();
    }
    // use the internal extension since we are also returning the internal media type
    return FilenameUtils.getBaseName(standardizeFilename(filename)) + mainObject.getFileExtension();
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
