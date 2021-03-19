package ca.gc.aafc.objectstore.api.file;

import java.io.IOException;
import java.util.Optional;

/**
 * 
 * Service allowing read access to file information. 
 *
 */
public interface FileInformationService {

  /**
   * Check if a bucket exists.
   * This method doesn't throw exception and simply return false if there is an Exception.
   * Should be changed to at least throw IOException.
   * @param bucketName
   * @return
   */
  boolean bucketExists(String bucketName);
  
  /**
   * Get information about a file as {@link FileObjectInfo}.
   * 
   * @param fileName
   * @param bucketName
   * @return {@link FileObjectInfo} instance or {@link Optional#empty} if the filename or the bucket
   *         don't exist
   * @throws IOException
   */
  Optional<FileObjectInfo> getFileInfo(String fileName, String bucketName, boolean isDerivative) throws IOException;

}
