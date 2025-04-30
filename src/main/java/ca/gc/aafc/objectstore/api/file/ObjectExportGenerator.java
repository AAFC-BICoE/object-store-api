package ca.gc.aafc.objectstore.api.file;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ca.gc.aafc.objectstore.api.MainConfiguration;
import ca.gc.aafc.objectstore.api.config.ExportFunction;
import ca.gc.aafc.objectstore.api.config.ObjectExportOption;
import ca.gc.aafc.objectstore.api.entities.AbstractObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.entities.Derivative;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.service.ImageOperationService;
import ca.gc.aafc.objectstore.api.storage.FileStorage;
import ca.gc.aafc.objectstore.api.util.ImageUtils;
import ca.gc.aafc.objectstore.api.util.ObjectFilenameUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import lombok.extern.log4j.Log4j2;

/**
 * Responsible to generate the export archive asynchronously.
 */
@Log4j2
@Service
public class ObjectExportGenerator {

  private final FileStorage fileStorage;
  private final ImageOperationService imageOperationService;

  public ObjectExportGenerator(FileStorage fileStorage, ImageOperationService ImageOperationService) {
    this.fileStorage = fileStorage;
    this.imageOperationService = ImageOperationService;
  }

  @Async(MainConfiguration.DINA_THREAD_POOL_BEAN_NAME)
  public CompletableFuture<UUID> export(UUID exportUUID, List<AbstractObjectStoreMetadata> objectsToExport,
                                        Path zipFile, ObjectExportOption exportOptions) {

    Map<UUID, String> layoutByFileIdentifier = invertExportLayout(exportOptions.exportLayout());
    Map<UUID, String> filenameAliases = exportOptions.aliases() == null ? Map.of() : exportOptions.aliases();
    Map<String, AtomicInteger> filenamesIncluded = new HashMap<>();

    try (ArchiveOutputStream<ZipArchiveEntry> o = new ZipArchiveOutputStream(zipFile)) {
      for (AbstractObjectStoreMetadata currObj: objectsToExport) {

        // Set zipEntry
        ZipArchiveEntry entry =
          new ZipArchiveEntry(generateExportItemFilename(currObj, filenameAliases.get(currObj.getFileIdentifier()),
            layoutByFileIdentifier.get(currObj.getFileIdentifier()), filenamesIncluded));
        o.putArchiveEntry(entry);

        // Get and copy the stream into the zip
        Optional<InputStream> optIs =
          fileStorage.retrieveFile(currObj.getBucket(), currObj.getFilename(), currObj instanceof Derivative);
        try (InputStream is = optIs.orElseThrow(
          () -> new IllegalStateException("No InputStream available"))) {

          //If there is no function(s) handling the stream copy it
          if (!handleImageFunction(is, currObj.getDcFormat(), o, exportOptions.functions())) {
            IOUtils.copy(is, o);
          }
        }
        o.closeArchiveEntry();
      }
      o.finish();
    } catch (IOException | IllegalStateException e) {
      return CompletableFuture.failedFuture(e);
    }
    
    return CompletableFuture.completedFuture(exportUUID);
  }

  /**
   * Handles the first specified export function to an image read from a source stream
   * and writes the result to an output stream.
   * <p>
   * This method currently only supports the {@link ExportFunction.FunctionDef#IMG_RESIZE} function.
   * If the first function in the list is not {@code IMG_RESIZE}, a warning is logged and no operation
   * is performed.
   * <p>
   * If a supported function is applied, it creates an {@link ImageOutputStream} from the provided output stream and writes the result as JPEG.
   * The original {@code source} and {@code out} streams are NOT closed by this method;
   * it is the caller's responsibility to manage their lifecycle.
   *
   * @param source    The input stream containing the image data. Must be a format supported by {@link ImageIO#read}.
   * @param sourceMediaType the media type of the source
   * @param out       The output stream where the processed image will be written.
   * @param functions A list of functions to apply to the image. Only the first function in the list
   *                  is currently processed.
   * @return Was the function applied ?
   */
  private boolean handleImageFunction(InputStream source, String sourceMediaType, OutputStream out, List<ExportFunction> functions)
    throws IOException {

    if (CollectionUtils.isEmpty(functions)) {
      log.debug("No export functions provided. Skipping operation.");
      return false;
    }

    ExportFunction exportFct = functions.getFirst();

    // Make sure the media type is supported and parameters are valid
    if (exportFct.isMediaTypeSupported(sourceMediaType) && exportFct.areParamsValid()) {
      BufferedImage buffImgIn = ImageIO.read(source);
      BufferedImage buffImgOut =
        imageOperationService.resize(buffImgIn, Float.parseFloat(exportFct.params().getFirst()));
      ImageOutputStream output = ImageIO.createImageOutputStream(out);
      ImageUtils.writeJpeg(buffImgOut, output);
      output.close();
      return true;
    } else {
      log.warn("Ignoring export function {} for media type {}",
        exportFct.functionDef().name(), sourceMediaType);
    }

    return false;
  }

  /**
   * Get a unique (withing the export) filename.
   *
   * @param obj           the data about the file to add
   * @param filenameAlias use an alternative name for the filename
   * @param folder        folder to which the file should be stored under
   * @param usedFilenames filenames that are already used with a counter. Will be modified by this function.
   * @return
   */
  private static String generateExportItemFilename(AbstractObjectStoreMetadata obj,
                                                   String filenameAlias, String folder,
                                                   Map<String, AtomicInteger> usedFilenames) {
    String filename;
    if (obj instanceof ObjectStoreMetadata metadata) {
      filename = ObjectFilenameUtils.generateMainObjectFilename(metadata, filenameAlias);
    } else if (obj instanceof Derivative derivative) {
      filename = ObjectFilenameUtils.generateDerivativeFilename(derivative, filenameAlias);
    } else {
      filename = obj.getFilename();
    }

    // Do we have an export layout to consider ?
    if (StringUtils.isNotBlank(folder)) {
      String folderName = ObjectFilenameUtils.standardizeFolderName(folder);
      filename = folderName + filename;
    }

    // if the filename is already used make sure to add a (1) at the end of the name
    if (usedFilenames.containsKey(filename)) {
      int duplicatedNumber = usedFilenames.get(filename).incrementAndGet();
      String newFilename = ObjectFilenameUtils.insertBeforeFileExtension(filename, "(" + duplicatedNumber + ")");

      // it is extremely unlikely but, we record the new generated name just in case later we have a file that
      // used that name as original filename
      usedFilenames.put(newFilename, new AtomicInteger(0));

      return newFilename;
    } else {
      usedFilenames.put(filename, new AtomicInteger(0));
    }
    return filename;
  }

  private static Map<UUID, String> invertExportLayout(Map<String, List<UUID>> exportLayout) {

    if (MapUtils.isEmpty(exportLayout)) {
      return Map.of();
    }

    Map<UUID, String> layoutByFileIdentifier = new HashMap<>();
    for (var entry : exportLayout.entrySet()) {
      for (var uuid : entry.getValue()) {
        if (layoutByFileIdentifier.containsKey(uuid)) {
          throw new IllegalArgumentException("fileIdentifiers should be unique in exportLayout");
        }
        layoutByFileIdentifier.put(uuid, entry.getKey());
      }
    }
    return layoutByFileIdentifier;
  }
}
