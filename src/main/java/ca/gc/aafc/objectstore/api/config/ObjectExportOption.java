package ca.gc.aafc.objectstore.api.config;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ObjectExportOption(Map<UUID, String> aliases,
                                 Map<String, List<UUID>> exportLayout,
                                 List<ExportFunction> functions) {
}
