package ca.gc.aafc.objectstore.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
public class MainConfigurationTest extends BaseIntegrationTest{

  private static final String CACHE_CONTROLLED_PATH = "/api/v1/license" ;

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }  

  @Test
  public void whenAccessLicence_cacheControlAddedToResponseHeader() throws Exception {
    this.mockMvc.perform(MockMvcRequestBuilders.get(CACHE_CONTROLLED_PATH))
      .andDo(MockMvcResultHandlers.print())
      .andExpect(MockMvcResultMatchers.header()
      .string("Cache-Control","max-age=86400, must-revalidate, no-transform"));
  }
}

