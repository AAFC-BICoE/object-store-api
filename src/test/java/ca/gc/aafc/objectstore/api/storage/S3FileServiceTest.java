package ca.gc.aafc.objectstore.api.storage;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import ca.gc.aafc.objectstore.api.BaseIntegrationTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.inject.Inject;
import lombok.SneakyThrows;

@ContextConfiguration(initializers = VersityWGTestContainerInitializer.class)
@SpringBootTest(properties = {"dev-user.enabled=true", "dina.fileStorage.implementation=S3"})
class S3FileServiceTest extends BaseIntegrationTest {

  public static final String BUCKET = "bucket";

  @Inject
  private FileStorage fileStorage;

  @Inject
  private S3FileManagement fileManagement;

  @SneakyThrows
  @BeforeEach
  void setUp() {
    fileManagement.ensureBucketExists(BUCKET);
  }

  @SneakyThrows
  @Test
  void storeFile_whenFileExists_FileOverWritten() {
    String fileName = "name";
    byte[] firstFile = "firstFile".getBytes();
    fileStorage.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(firstFile)
    );
    Assertions.assertArrayEquals(firstFile, returnBytesForFile(fileName));

    byte[] expected = "dina".getBytes();
    fileStorage.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(expected)
    );

    Assertions.assertArrayEquals(expected, returnBytesForFile(fileName));
  }

  @SneakyThrows
  @Test
  void storeFile_getFile_FilesStoredAndGotten() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileStorage.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(bytes)
    );

    Assertions.assertArrayEquals(bytes, returnBytesForFile(fileName));
  }

  @SneakyThrows
  @Test
  void getFile_WhenFileDoesNotExist_OptionalEmptyReturned() {
    Assertions.assertFalse(fileStorage.retrieveFile(BUCKET,"fileName", false).isPresent());
  }

  @SneakyThrows
  @Test
  void getFile_WhenBucketDoesNotExist_OptionalEmptyReturned() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileStorage.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(bytes)
    );

    Assertions.assertFalse(fileStorage.retrieveFile("fake", fileName,false).isPresent());
  }

  @SneakyThrows
  @Test
  void getFileInfo_ReturnsFileInfo() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileStorage.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(bytes)
    );

    Assertions.assertTrue(fileStorage.getFileInfo(BUCKET, fileName,false).isPresent());
  }

  @SneakyThrows
  @Test
  void getFileInfo_WhenNoFile_OptionalEmptyReturned() {
    Assertions.assertFalse(fileStorage.getFileInfo(BUCKET, "nosuchfile",false).isPresent());
  }

  @SneakyThrows
  @Test
  void removeFile_FileRemoved() {
    byte[] bytes = "dina".getBytes();
    String fileName = "name";

    fileStorage.storeFile(
      BUCKET,
      fileName,
      false,
      MediaType.TEXT_PLAIN_VALUE,
      new ByteArrayInputStream(bytes)
    );

    Assertions.assertTrue(fileStorage.retrieveFile(BUCKET, fileName,false).isPresent());
    fileStorage.deleteFile(BUCKET, fileName, false);
    Assertions.assertFalse(fileStorage.retrieveFile(BUCKET, fileName, false).isPresent());
  }

//  @Test
//  void bucketExists() {
//    Assertions.assertTrue(fileService.bucketExists(BUCKET));
//    Assertions.assertFalse(fileService.bucketExists("fake"));
//  }

  private byte[] returnBytesForFile(String fileName) throws IOException {
    return IOUtils.toByteArray(fileStorage.retrieveFile(BUCKET, fileName, false)
      .orElseThrow(() -> {
        Assertions.fail("The file was not persisted");
        return null;
      }));
  }
}