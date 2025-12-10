package ca.gc.aafc.objectstore.api.storage;

import java.io.IOException;

public interface FileManagement {

  /**
   * Checks if the bucket exists and if it doesn't create it.
   * @param bucketName
   */
  void ensureBucketExists(String bucketName) throws IOException;

}
