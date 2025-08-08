package ca.gc.aafc.objectstore.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.objectstore.api.dto.ObjectExportDto;
import ca.gc.aafc.objectstore.api.service.ObjectExportService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface ObjectExportMapper {

  ObjectExportMapper INSTANCE = Mappers.getMapper(ObjectExportMapper.class);

  @Mapping(source = "filenameAliases", target = "objectExportOption.aliases")
  @Mapping(source = "exportLayout", target = "objectExportOption.exportLayout")
  @Mapping(source = "exportFunction", target = "objectExportOption.exportFunction")
  ObjectExportService.ExportArgs toEntity(ObjectExportDto dto);

  default <T> List<T> nullSafeList(List<T> list) {
    return list == null ? null : new ArrayList<>(list);
  }

  default <K, V> Map<K, V> nullSafeMap(Map<K, V> map) {
    return map == null ? null : new LinkedHashMap<>(map);
  }
}
