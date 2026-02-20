package ca.gc.aafc.objectstore.api.storage;
import static java.nio.file.FileVisitResult.*;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;
import lombok.extern.log4j.Log4j2;

/**
 * A file visitor that detects the presence of xl.meta files in a file system directory tree.
 * 
 * The object-store-api using the FS storage implementation will not be able to read files uploaded 
 * to a MinIO bucket that has erasure coding enabled.  
 * MinIO creates xl.meta files in directories that are erasure coded, which is not compatible 
 * with the Object Store API's FS file storage implementation. 
 * 
 * The visitor traverses the FS storage file tree  and terminates immediately upon finding the first
 * xl.meta file, avoiding unnecessary traversal of the entire directory tree.
 * 
 * @see java.nio.file.SimpleFileVisitor
 */
@Log4j2
public class XlMetaFileDetector extends SimpleFileVisitor<Path> {

    private boolean foundXlMeta = false;

    public boolean isFoundXlMeta() {
        return foundXlMeta;
    }
    @Override
    public java.nio.file.FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if(attrs.isRegularFile()) {
            if (file.getFileName().toString().equals("xl.meta")) {
                log.info("Found xl.meta file in FS storage directory. Please ensure that the previous data in this directory was not erasure coded.");
                return TERMINATE; // Stop traversal once a .xlmeta file is found
            }
        } 
        return CONTINUE; 
    }
}
