package ca.gc.aafc.objectstore.api.config;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.executable.ExecutableParser;
import org.apache.tika.parser.pkg.PackageParser;
import org.apache.tika.parser.pkg.RarParser;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

/**
 * ObjectStore configuration for MediaType based on Tika.
 *
 */
@Configuration
public class MediaTypeConfiguration {

  private static final TikaConfig TIKA_CONFIG = TikaConfig.getDefaultConfig();

  // we allow CompressorParser gzip
  public static final Set<MediaType> UNSUPPORTED_MEDIA_TYPE = getSupportedMediaTypesFromParsers(
          new ExecutableParser(), new PackageParser(), new RarParser());

  public static final Map<String, Set<MediaType>> SUPPORTED_MEDIA_TYPE = initSupportedMediaType();

  /**
   * init the supported MediaType from Tika but removed unsupported MediaType.
   * Also group them by the base type.
   * @return
   */
  private static Map<String, Set<MediaType>> initSupportedMediaType() {
    MediaTypeRegistry typeRegistry = TIKA_CONFIG.getMediaTypeRegistry();
    return typeRegistry.getTypes().stream()
            .filter( mt -> !UNSUPPORTED_MEDIA_TYPE.contains(mt))
            .collect(Collectors.groupingBy(MediaType::getType, mapping(MediaType::getBaseType, toSet())));
  }

  /**
   * Return an immutable set of all the supported media type of the provided parsers.
   * @param parsers
   * @return
   */
  private static Set<MediaType> getSupportedMediaTypesFromParsers(Parser... parsers) {
    return Stream.of(parsers)
            .map(p -> p.getSupportedTypes(null))
            .flatMap(Collection::stream)
            .collect(toSet());
  }

  /**
   * Checks of the provided MediaType is supported.
   * Supported if defined by the absence of the MediaType from {@link #UNSUPPORTED_MEDIA_TYPE}.
   * @param mediaType
   * @return
   */
  public boolean isSupported(MediaType mediaType) {
    return !UNSUPPORTED_MEDIA_TYPE.contains(mediaType);
  }

  /**
   * Returns all the supported media type.
   * @return
   */
  public Set<MediaType> getSupportedMediaType() {
    return MediaTypeConfiguration.SUPPORTED_MEDIA_TYPE
            .values().stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
  }

  /**
   * {@link MimeType} is useful to get the file extension(s) used by a MediaType.
   * @param mimeTypeName
   * @return
   */
  public MimeType mimeTypeFromName(String mimeTypeName) throws MimeTypeException {
    return TIKA_CONFIG.getMimeRepository().forName(mimeTypeName);
  }

}
