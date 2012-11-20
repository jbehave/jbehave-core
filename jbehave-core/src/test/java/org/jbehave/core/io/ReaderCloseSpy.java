package org.jbehave.core.io;

import java.io.StringReader;

/**
 *
 * subclass of StringReader that allows querying is the Reader is already closed
 *
 */

class ReaderCloseSpy extends StringReader {

    private boolean closed;

    public ReaderCloseSpy(String s) {
        super(s);
        closed=false;
    }

    public void close() {
        super.close();
        closed=true;
    }
    
    public boolean isClosed() {
        return closed;
    }

}
