package ca.gc.aafc.objectstore.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@ConfigurationProperties(prefix = "dina.export")
@RequiredArgsConstructor
@Getter
@Setter
public class ObjectExportConfiguration {

  private DataSize maxObjectExportSize;

}
