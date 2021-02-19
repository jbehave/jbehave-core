package org.jbehave.io;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * File manager that uploads and unarchives data files
 */
public class ArchivingFileManager implements FileManager {

    final FileArchiver archiver;
    final FileMonitor monitor;
    private final File uploadDirectory;

    public ArchivingFileManager(FileArchiver archiver, FileMonitor monitor, File uploadDirectory) {
        this.archiver = archiver;
        this.monitor = monitor;
        this.uploadDirectory = uploadDirectory;
    }

    public List<File> list() {
        File directory = uploadDirectory();
        List<File> files = asList(directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return !file.isDirectory();
            }
        }));
        monitor.filesListed(directory, files);
        return files;
    }

    File uploadDirectory() {
        uploadDirectory.mkdirs();
        return uploadDirectory;
    }
    
    public List<File> listContent(File file, boolean relativePaths) {
        if (!archiver.isArchive(file)) {
            return asList(file);
        }
        File directory = new File(uploadDirectory, archiver.directoryOf(new File(file.getName())).getPath());
        List<File> content = new ArrayList<File>();
        for (File archiveFile : archiver.listContent(directory)) {
            File contentFile = (relativePaths ? archiver.relativeTo(archiveFile, directory) : archiveFile);
            content.add(contentFile);
        }
        monitor.contentListed(file.getPath(), directory, relativePaths, content);
        return content;
    }

    public void delete(List<File> files) {
        for (File file: files) {
            deleteFile(file);
        }
    }

    private void deleteFile(File file) {
        if (file.isDirectory()) {
            // recursively delete children
            for (String child : file.list()) {
                deleteFile(new File(file, child));
            }
        }
        if (archiver.isArchive(file)) {
            // delete the unarchived directory too
            File directory = archiver.directoryOf(file);
            deleteFile(directory);
            monitor.fileDeleted(directory);
        }
        file.delete();
        monitor.fileDeleted(file);
    }

    public List<File> upload(List<FileItem> fileItems, List<String> errors) {
        List<File> files = new ArrayList<File>();
        File directory = uploadDirectory();
        for (FileItem item : fileItems) {
            try {
                File file = writeItemToFile(directory, item);
                monitor.fileUploaded(file);
                files.add(file);                
            } catch (FileItemNameMissingException e) {
                // ignore and carry on
            } catch (FileWriteFailedException e) {
                errors.add(e.getMessage());
                if (e.getCause() != null) {
                    errors.add(e.getCause().getMessage());
                }
                monitor.fileUploadFailed(item, e);
            }
        }
        return files;
    }

    public void unarchiveFiles(List<File> files, List<String> errors) {
        File directory = uploadDirectory();
        for (File file : files) {
            if (archiver.isArchive(file)) {
                try {
                    archiver.unarchive(file, directory);
                    monitor.fileUnarchived(file, directory);
                } catch (FileUnarchiveFailedException e) {
                    errors.add(e.getMessage());
                    if (e.getCause() != null) {
                        errors.add(e.getCause().getMessage());
                    }
                }
            }
        }

    }

    private File writeItemToFile(File directory, FileItem item) {
        if (isBlank(item.getName())) {
            throw new FileItemNameMissingException(item);
        }
        File file = new File(directory, fileName(item));
        try {
            if (file.exists()) {
                file.createNewFile();
            }
            item.write(file);
        } catch (Exception e) {
            throw new FileWriteFailedException(file, e);
        }
        return file;
    }

    private String fileName(FileItem item) {
        // FileItem.getName() may return the full path, depending on the client
        // (e.g. IE or Opera)
        // FilenameUtils.getName(path) extracts file name whatever the path
        // separator (Unix or Windows)
        return FilenameUtils.getName(item.getName());
    }

    public File getUploadDirectory() {
        return uploadDirectory;
    }

    @SuppressWarnings("serial")
    public static final class FileItemNameMissingException extends RuntimeException {

        public FileItemNameMissingException(FileItem file) {
            super(file.toString() + " missing name");
        }

    }

    @SuppressWarnings("serial")
    public static final class FileWriteFailedException extends RuntimeException {

        public FileWriteFailedException(File file, Throwable cause) {
            super(file.toString() + " write failed", cause);
        }

    }

}