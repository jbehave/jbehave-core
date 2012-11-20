package org.jbehave.core.io;

/**
 *
 * subclass of InputStreamCloseSpy that will not read any data
 *
 */

class InputStreamCloseSpyException extends InputStreamCloseSpy {

    public InputStreamCloseSpyException(byte[] s) {
        super(s);
    }

    public synchronized int read(byte b[], int off, int len) {
        // ByteArrayInputStream.read doesn't throw an Exception
        // so we have to use a runtime exception here
        throw new RuntimeException();
    }
}
