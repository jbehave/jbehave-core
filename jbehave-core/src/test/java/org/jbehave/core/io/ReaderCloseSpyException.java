package org.jbehave.core.io;

import java.io.IOException;

class ReaderCloseSpyException extends ReaderCloseSpy {

    public ReaderCloseSpyException(String s) {
        super(s);
    }

    public int read() throws IOException {
        throw new IOException();
    }

    public int read(char cbuf[], int off, int len) throws IOException {
        throw new IOException();
    }
}
