package ca.gc.aafc.objectstore.api.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;

import ca.gc.aafc.dina.repository.ReadOnlyDinaRepositoryV2;
import ca.gc.aafc.objectstore.api.dto.ConfigPropertiesDto;
import ca.gc.aafc.objectstore.api.service.ConfigService;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "${dina.apiPrefix:}", produces = JSON_API_VALUE)
public class ConfigRepository extends ReadOnlyDinaRepositoryV2<String, ConfigPropertiesDto> {

  protected ConfigRepository(ConfigService configService) {
    super(configService);
  }

  @GetMapping(ConfigPropertiesDto.TYPENAME + "/{id}")
  public ResponseEntity<RepresentationModel<?>> handleFindOne(@PathVariable String id) {

    ConfigPropertiesDto dto = findOne(id);
    if (dto == null) {
      return ResponseEntity.notFound().build();
    }

    JsonApiModelBuilder builder = jsonApiModel().model(RepresentationModel.of(dto));
    return ResponseEntity.ok(builder.build());
  }

  @GetMapping(ConfigPropertiesDto.TYPENAME)
  public ResponseEntity<RepresentationModel<?>> handleFindAll(HttpServletRequest req) {

    String queryString = StringUtils.isBlank(req.getQueryString()) ? "" :
      URLDecoder.decode(req.getQueryString(), StandardCharsets.UTF_8);

    List<ConfigPropertiesDto> dtos ;
    try {
      dtos = findAll(queryString);
    } catch (IllegalArgumentException iaEx) {
      return ResponseEntity.badRequest().build();
    }

    JsonApiModelBuilder builder = jsonApiModel().model(CollectionModel.of(dtos));
    return ResponseEntity.ok(builder.build());
  }
}
