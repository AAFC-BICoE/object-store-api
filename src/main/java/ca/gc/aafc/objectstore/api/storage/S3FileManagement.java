package ca.gc.aafc.objectstore.api.storage;

import java.io.IOException;
import java.net.URI;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

import software.amazon.awssdk.services.s3.model.S3Exception;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import ca.gc.aafc.objectstore.api.config.S3Config;

/**
 * {@link FileManagement} implementation using s3 client to perform management operations.
 */
@ConditionalOnExpression("'${dina.fileStorage.implementation}' == 'S3' or '${dina.fileStorage.implementation}' == 'MINIO'")
@Service
@Log4j2
public class S3FileManagement implements FileManagement {

  private final S3Config s3Config;

  public S3FileManagement (S3Config s3Config) {
    this.s3Config = s3Config;
  }

  @Override
  public void ensureBucketExists(String bucketName) throws IOException {
    try (S3Client s3Client = S3Client.builder()
      .endpointOverride(URI.create(s3Config.getEndpoint()))
      .region(Region.of(s3Config.getRegion()))
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(s3Config.getAccessKey(), s3Config.getSecretKey())))
      .forcePathStyle(true) // Crucial for MinIO/Local S3
      .build()) {

      try {
        // 1. Check if bucket exists
        s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        log.debug("Bucket: {} already exists.", bucketName);
      } catch (S3Exception e) {
        if (e.statusCode() == 404) {
          // 2. Create bucket
          s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
          log.debug("Bucket {} created.", bucketName);
        } else {
          throw new IOException(e);
        }
      }
    }
  }
}
