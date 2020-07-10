package ca.gc.aafc.objectstore.api.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.TestConfiguration;

@SpringBootTest
@ActiveProfiles("test")
public class FileUploadIT {

  @Autowired
  protected WebApplicationContext wac;

  private final static String bucketUnderTest = DinaAuthenticatedUserConfig.ROLES_PER_GROUP.keySet()
    .stream()
    .findFirst()
    .get();

  @Test
  public void fileUpload_onMultipartRequest_acceptFile() throws Exception {

    MockMultipartFile file = new MockMultipartFile("file", "testfile", MediaType.TEXT_PLAIN_VALUE,
        "Test Content".getBytes());

    webAppContextSetup(this.wac).build()
        .perform(MockMvcRequestBuilders.multipart("/api/v1/file/" + bucketUnderTest).file(file))
        .andExpect(status().is(200));
  }

  @Test
  public void fileUpload_onInvalidBucket_returnError() throws Exception {

    MockMultipartFile file = new MockMultipartFile("file", "testfile", MediaType.TEXT_PLAIN_VALUE,
        "Test Content".getBytes());
    
    try {
      webAppContextSetup(this.wac).build()
      .perform(MockMvcRequestBuilders
          .multipart("/api/v1/file/a" + TestConfiguration.ILLEGAL_BUCKET_CHAR + "b").file(file));
      fail("Expected NestedServletException");
    }
    // NestedServletException is a generic exception so we want to do the assertion on the cause
    catch (NestedServletException nsEx) {
      assertEquals(IllegalStateException.class, nsEx.getCause().getClass());
    }
     
  }
}
