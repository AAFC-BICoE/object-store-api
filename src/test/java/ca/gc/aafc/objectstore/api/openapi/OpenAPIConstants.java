package ca.gc.aafc.objectstore.api.openapi;

import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;

import java.net.URL;

public class OpenAPIConstants {

  private static final String SCHEME = "https";
  static final String SPEC_HOST = "raw.githubusercontent.com";
  static final String SPEC_PATH = "DINA-Web/object-store-specs/master/schema/object-store-api.yml";
  
  public static final URL OBJECT_STORE_API_SPECS_URL = buildOpenAPISpecsURL();

  private OpenAPIConstants() {
  }

  @SneakyThrows
  private static URL buildOpenAPISpecsURL() {
    URIBuilder URI_BUILDER = new URIBuilder();
    URI_BUILDER.setScheme(SCHEME);
    URI_BUILDER.setHost(SPEC_HOST);
    URI_BUILDER.setPath(SPEC_PATH);
    return URI_BUILDER.build().toURL();
  }
}
