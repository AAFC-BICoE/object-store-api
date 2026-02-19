package ca.gc.aafc.objectstore.api.storage;
import static java.nio.file.FileVisitResult.*;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;
import lombok.extern.log4j.Log4j2;

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
