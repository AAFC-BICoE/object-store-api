package ca.gc.aafc.objectstore.api.file;

import java.nio.file.Path;
import java.util.Objects;

import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;

/**
 * A fully resolved entry to include in an export archive.
 * <p>
 * Either a {@link StoredFile} (stored files and derivatives, read from the object store) or an
 * {@link ExternalResource} (externally hosted resources, read from the local file system).
 */
public sealed interface ObjectExportItem
    permits ObjectExportItem.StoredFile, ObjectExportItem.ExternalResource {

  /**
   * @return the metadata backing this export entry
   */
  AbstractObjectStoreMetadata metadata();

  /**
   * @return optional alias to use for the filename, may be null
   */
  String filenameAlias();

  /**
   * @return optional folder to place the entry under, may be null
   */
  String folder();

  /**
   * Export of a file (or derivative) stored in the object store.
   */
  record StoredFile(AbstractObjectStoreMetadata metadata, String filenameAlias, String folder)
      implements ObjectExportItem {
    public StoredFile {
      Objects.requireNonNull(metadata, "metadata");
    }
  }

  /**
   * Export of an externally hosted resource.
   *
   * @param externalPath resolved local path of the external resource
   */
  record ExternalResource(AbstractObjectStoreMetadata metadata, String filenameAlias,
                          String folder, Path externalPath) implements ObjectExportItem {
    public ExternalResource {
      Objects.requireNonNull(metadata, "metadata");
      Objects.requireNonNull(externalPath, "externalPath");
    }
  }
}
