package ca.gc.aafc.objectstore.api.mapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.gc.aafc.objectstore.api.config.ExportFunction;
import ca.gc.aafc.objectstore.api.dto.ObjectExportDto;
import ca.gc.aafc.objectstore.api.service.ObjectExportService;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectExportMapperTest {

  @Test
  public void testMapping() {

    UUID file1UUID = UUID.randomUUID();

    ObjectExportDto exportDto = ObjectExportDto.builder()
      .username("Jim")
      .fileIdentifiers(List.of(file1UUID))
      .name("my-export")
      .filenameAliases(Map.of(file1UUID, "alias1"))
      .exportLayout(Map.of("myfolder", List.of(file1UUID)))
      .exportFunction(ExportFunction.builder().functionDef(ExportFunction.FunctionDef.IMG_RESIZE).build())
      .build();

    ObjectExportService.ExportArgs exportArgs = ObjectExportMapper.INSTANCE.toEntity(exportDto);

    assertEquals("alias1", exportArgs.objectExportOption().aliases().get(file1UUID));
    assertEquals(file1UUID, exportArgs.objectExportOption().exportLayout().get("myfolder").getFirst());
    assertEquals(ExportFunction.FunctionDef.IMG_RESIZE, exportArgs.objectExportOption().exportFunction().functionDef());
    assertEquals("Jim", exportArgs.username());
    assertEquals(file1UUID, exportArgs.fileIdentifiers().getFirst());
    assertEquals("my-export", exportArgs.name());

  }
}
