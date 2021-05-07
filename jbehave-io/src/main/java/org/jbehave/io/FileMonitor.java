package org.jbehave.io;

import java.io.File;
import java.util.List;

import org.apache.commons.fileupload.FileItem;

public interface FileMonitor {
    
    void contentListed(String path, File directory,
            boolean relativePaths, List<File> content);

    void filesListed(File uploadDirectory, List<File> files);

    void fileUploaded(File file);

    void fileUploadFailed(FileItem item, Exception cause);

    void fileUnarchived(File file, File directory);

    void fileDeleted(File file);

}
