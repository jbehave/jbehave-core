package org.jbehave.io;

import java.io.File;

@SuppressWarnings("serial")
public final class FileUnarchiveFailedException extends
        RuntimeException {

    public FileUnarchiveFailedException(File archive, File directory,
                                        Exception cause) {
        super("Failed to unarchive " + archive + " to dir " + directory,
                cause);
    }

}
