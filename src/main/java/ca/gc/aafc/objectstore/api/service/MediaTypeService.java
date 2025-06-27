package ca.gc.aafc.objectstore.api.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ca.gc.aafc.dina.service.CollectionBackedReadOnlyDinaService;
import ca.gc.aafc.objectstore.api.config.MediaTypeConfiguration;
import ca.gc.aafc.objectstore.api.dto.MediaTypeDto;

@Service
public class MediaTypeService extends CollectionBackedReadOnlyDinaService<String, MediaTypeDto> {

  public MediaTypeService(MediaTypeConfiguration mediaTypeConfig) {
    super(mediaTypeConfig.getSupportedMediaType()
      .stream().map(MediaTypeDto::fromMediaType)
      .collect(Collectors.toSet()), MediaTypeDto::getId
    );
  }
}
