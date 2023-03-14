package org.jbehave.io;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.fileupload.FileItem;

public class PrintStreamFileMonitor implements FileMonitor {

    private final PrintStream output;

    public PrintStreamFileMonitor() {
        this(System.out);
    }

    public PrintStreamFileMonitor(PrintStream output) {
        this.output = output;
    }

    protected void print(String format, Object... args) {
        output.printf(format + "%n", args);
    }

    protected void printStackTrace(Throwable e) {
        e.printStackTrace(output);
    }

    @Override
    public void contentListed(String path, File directory, boolean relativePaths, List<File> content) {
        print("Listed content of path %s from directory %s using %s paths: %s", path, directory, relativePaths
                ? "relative" : "full", content);
    }

    @Override
    public void fileDeleted(File file) {
        print("Deleted file %s", file);
    }

    @Override
    public void fileUnarchived(File file, File directory) {
        print("Unarchived file %s to directory %s", file, directory);
    }

    @Override
    public void fileUploaded(File file) {
        print("Uploaded file %s", file);
    }

    @Override
    public void fileUploadFailed(FileItem item, Exception cause) {
        print("File upload of %s failed: ", item);
        printStackTrace(cause);
    }

    @Override
    public void filesListed(File uploadDirectory, List<File> files) {
        print("Listed files from upload directory %s: %s", uploadDirectory, files);
    }

}
