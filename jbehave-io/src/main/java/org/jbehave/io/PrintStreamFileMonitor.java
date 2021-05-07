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

    protected void print(PrintStream output, String message) {
        print(output, message, null);
    }

    protected void print(PrintStream output, String message, Exception cause) {
        output.println(message);
        if (cause != null) {
            cause.printStackTrace(output);
        }
    }

    public void contentListed(String path, File directory,
            boolean relativePaths, List<File> content) {
        print(output, "Listed content of path " + path + " from directory "
                + directory + " using " + (relativePaths ? "relative" : "full")
                + " paths: " + content);
    }

    public void fileDeleted(File file) {
        print(output, "Deleted file " + file);
    }

    public void fileUnarchived(File file, File directory) {
        print(output, "Unarchived file " + file + " to directory " + directory);
    }

    public void fileUploaded(File file) {
        print(output, "Uploaded file " + file);
    }

    public void fileUploadFailed(FileItem item, Exception cause) {
        print(output, "File upload of " + item +" failed: ", cause);
    }

    public void filesListed(File uploadDirectory, List<File> files) {
        print(output, "Listed files from upload directory " + uploadDirectory
                + ": " + files);
    }

}
