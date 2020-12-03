package org.jbehave.io;

import java.io.File;

@SuppressWarnings("serial")
public final class FileArchiveFailedException extends
        RuntimeException {

    public FileArchiveFailedException(File archive, File directory,
                                      Exception cause) {
        super("Failed to archive dir " + directory + " to " + archive,
                cause);
    }

}
