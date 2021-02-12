package org.jbehave.io;

import org.apache.commons.fileupload.FileItem;

import java.io.File;
import java.util.List;

/**
 * Manages data files, allowing the upload, list and delete. If a file is any
 * archive, the contents can be unarchived to the directory with the corresponding
 * name af the archive (e.g. for archive "/path/to/archive.zip", the output
 * directory will be "/path/to/archive". The manager also allows to list
 * the content of a uploaded and unarchived files, e.g. the content of
 * "/path/to/archive".
 */
public interface FileManager {

    List<File> list();

    List<File> listContent(File file, boolean relativePaths);

    void delete(List<File> files);

    List<File> upload(List<FileItem> fileItems, List<String> errors);

    void unarchiveFiles(List<File> files, List<String> errors);

    File getUploadDirectory();
    
}