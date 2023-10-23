package ca.gc.aafc.objectstore.api.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import ca.gc.aafc.objectstore.api.file.FileObjectInfo;

/**
 * Main interface for interacting with the file storage.
 */
public interface FileStorage {

  /**
   * Store a new file in the file storage. The bucket is expected to exist but not the file.
   * @param bucket
   * @param fileName
   * @param isDerivative
   * @param contentType
   * @param iStream the input stream of the new file to store. Caller is responsible to close it.
   */
  void storeFile(String bucket, String fileName, boolean isDerivative, String contentType, InputStream iStream)
    throws IOException;

  Optional<InputStream> retrieveFile(String bucket, String fileName, boolean isDerivative)
    throws IOException;

  void deleteFile(String bucket, String fileName, boolean isDerivative);

  Optional<FileObjectInfo> getFileInfo(String fileName, String bucketName, boolean isDerivative)
    throws IOException;

}
