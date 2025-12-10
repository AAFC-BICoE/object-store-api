package ca.gc.aafc.objectstore.api.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import ca.gc.aafc.objectstore.api.file.FileObjectInfo;

/**
 * Main interface for interacting with the file storage.
 * Based on "bucket" concept like s3.
 */
public interface FileStorage {

  /**
   * Store a new file in the file storage. The bucket is expected to exist.
   * If the file already exists the default behavior is to overwrite it.
   * @param bucket
   * @param fileName
   * @param isDerivative
   * @param contentType
   * @param iStream the input stream of the new file to store. Caller is responsible to close it.
   */
  void storeFile(String bucket, String fileName, boolean isDerivative, String contentType,
                 InputStream iStream)
      throws IOException;

  Optional<InputStream> retrieveFile(String bucket, String fileName, boolean isDerivative)
      throws IOException;

  void deleteFile(String bucket, String fileName, boolean isDerivative) throws IOException;

  /**
   * Get information about a file without retrieving it.
   * @param bucketName
   * @param fileName
   * @param isDerivative
   * @return
   */
  Optional<FileObjectInfo> getFileInfo(String bucketName, String fileName, boolean isDerivative)
      throws IOException;

}
