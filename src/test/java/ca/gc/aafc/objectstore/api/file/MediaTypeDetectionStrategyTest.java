package ca.gc.aafc.objectstore.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.google.common.io.Resources;

public class MediaTypeDetectionStrategyTest {

  private static final MediaTypeDetectionStrategy MTDS = new MediaTypeDetectionStrategy();
 
  @Test
  public void detectMediaType_onNoMediaType_mediaTypeIsDetectedExtPreserved() throws FileNotFoundException, URISyntaxException {
    try (FileInputStream fis = new FileInputStream(
        Resources.getResource("drawing.png").toURI().getPath())) {

      assertDetectedAndEvaluatedMediaType(fis, null, "my_file.ab1",
          MediaType.IMAGE_PNG_VALUE, ".png", // png should be detected
          MediaType.IMAGE_PNG_VALUE, ".ab1");//but the original extension should be preserved
    } catch (IOException e) {
      fail(e);
    }
  }
  
  @Test
  public void detectMediaType_onTextFileWithSpecificExt_extIsPreserved() throws FileNotFoundException, URISyntaxException {
    try (FileInputStream fis = new FileInputStream(
        Resources.getResource("testfile.txt").toURI().getPath())) {

      assertDetectedAndEvaluatedMediaType(fis, MediaType.TEXT_PLAIN_VALUE, "testfile.ab2",
          MediaType.TEXT_PLAIN_VALUE, ".txt", // txt should be detected
          MediaType.TEXT_PLAIN_VALUE, ".ab2");//but the original extension should be preserved
    } catch (IOException e) {
      fail(e);
    }
  }
  
  @Test
  public void detectMediaType_onExcelSpreadsheet_mediaTypeDetected() throws FileNotFoundException, URISyntaxException {
    try (FileInputStream fis = new FileInputStream(
        Resources.getResource("test_spreadsheet.xlsx").toURI().getPath())) {

      assertEvaluatedMediaType(fis, null, "test_spreadsheet.xlsx",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
    } catch (IOException e) {
      fail(e);
    }
  }
  
  @Test
  public void detectMediaType_onNoMediaTypeProvided_typeDetected() {
    ByteArrayInputStream bais = new ByteArrayInputStream(
        "Test content".getBytes(StandardCharsets.UTF_8));

    assertEvaluatedMediaType(bais, null, null,
        MediaType.TEXT_PLAIN_VALUE, ".txt");
  }

  /**
   * Testing a jpg submitted as octet_stream
   * @throws URISyntaxException
   */
  @Test
  public void detectMediaType_onMediaTypeOctetStream_typeDetectedUsed() throws URISyntaxException {
    try (FileInputStream fis = new FileInputStream(
        Resources.getResource("cc0_test_image.jpg").toURI().getPath())) {

      assertEvaluatedMediaType(fis, MediaType.APPLICATION_OCTET_STREAM_VALUE, "cc0_test_image.jpg",
          MediaType.IMAGE_JPEG_VALUE, ".jpg");
    } catch (IOException e) {
      fail(e);
    }
  }
  
  @Test
  public void detectMediaType_onGenericMediaType_extensionIsPreserved() {
    ByteArrayInputStream bais = new ByteArrayInputStream(
        "Test content".getBytes(StandardCharsets.UTF_8));

    assertEvaluatedMediaType(bais, MediaType.TEXT_PLAIN_VALUE, "my_file.ab1",
        MediaType.TEXT_PLAIN_VALUE, ".ab1");
  }
  
  @Test
  public void detectMediaType_onUnknownMediaType_extensionAndMediaTypeIsPreserved() {
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { 1, 0, 10, 23 });
    assertEvaluatedMediaType(bais, "special/binary", "my_file.bin", "special/binary", ".bin");
  }
  
  @Test
  public void detectMediaType_onUnknownTextMediaType_extensionAndMediaTypeIsPreserved() {
    ByteArrayInputStream bais = new ByteArrayInputStream(
        "Test content".getBytes(StandardCharsets.UTF_8));
    assertEvaluatedMediaType(bais, "special/text", "my_file.zzz", "special/text", ".zzz");
  }
  
  @Test
  public void detectMediaType_onUnknownMediaType_extensionAndMediaTypeSet() {
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { 1, 0, 10, 23 });
    assertEvaluatedMediaType(bais, null, null, MediaType.APPLICATION_OCTET_STREAM_VALUE, ".bin");
  }
  
  private void assertEvaluatedMediaType(InputStream is, @Nullable String receivedMediaType,
      @Nullable String originalFilename, String evaluatedMediaType, String evaluatedExt) {
    assertDetectedAndEvaluatedMediaType(is, receivedMediaType, originalFilename, null, null, evaluatedMediaType,
        evaluatedExt);
  }
  
  /**
   * Method responsible to detect the mediatype/extension and assert the result against the provided
   * values.
   * 
   * @param is
   * @param receivedMediaType
   * @param originalFilename
   * @param detectedMediaType
   * @param detectedExt
   * @param evaluatedMediaType
   * @param evaluatedExt
   */
  private void assertDetectedAndEvaluatedMediaType(InputStream is, @Nullable String receivedMediaType,
      @Nullable String originalFilename, String detectedMediaType, String detectedExt,
      String evaluatedMediaType, String evaluatedExt) {
    try {
      MediaTypeDetectionStrategy.MediaTypeDetectionResult mtdr = MTDS.detectMediaType(is,
          receivedMediaType, originalFilename);
      
      if (detectedMediaType != null) {
        assertEquals(detectedMediaType, mtdr.getDetectedMediaType().toString());
      }
      if (detectedExt != null) {
        assertEquals(detectedExt, mtdr.getDetectedMimeType().getExtension());
      }
      
      assertEquals(evaluatedMediaType, mtdr.getEvaluatedMediaType());
      assertEquals(evaluatedExt, mtdr.getEvaluatedExtension());
    } catch (MimeTypeException | IOException e) {
      fail(e);
    }
  }
}
