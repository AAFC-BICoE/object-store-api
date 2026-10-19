package ca.gc.aafc.objectstore.api.file;

import java.nio.file.Path;

import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;

/**
 * A fully resolved entry to include in an export archive.
 * <p>
 * For stored files and derivatives {@code externalPath} is {@code null} and the content is read
 * from the object store. For externally hosted resources {@code externalPath} points to the
 * resolved local file and the content is read from the file system.
 *
 * @param metadata      the metadata backing this export entry
 * @param filenameAlias optional alias to use for the filename, may be null
 * @param folder        optional folder to place the entry under, may be null
 * @param externalPath  resolved path for externally hosted resources, {@code null} for stored files
 */
public record ObjectExportItem(AbstractObjectStoreMetadata metadata, String filenameAlias,
                               String folder, Path externalPath) {
}
