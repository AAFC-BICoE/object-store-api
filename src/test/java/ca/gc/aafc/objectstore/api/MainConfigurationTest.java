package ca.gc.aafc.objectstore.api;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@WebAppConfiguration
@SpringBootTest(classes={MainConfiguration.class})
public class MainConfigurationTest extends BaseIntegrationTest{
    
  @Autowired
  protected WebApplicationContext wac ;    

  protected MockMvc mockMvc;  

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();  
 }

  @Test
  public void whenAccessLicence_cacheControlAddedToResponseHeader() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/license"))
      .andDo(MockMvcResultHandlers.print())
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.header()
      .string("Cache-Control","max-age=24, must-revalidate, no-transform"));
  }

}
