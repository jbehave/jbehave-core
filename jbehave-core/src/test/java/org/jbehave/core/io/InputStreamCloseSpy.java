package org.jbehave.core.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 * subclass of ByteArrayInputStream that allows querying is the InputStream is already closed
 *
 */

class InputStreamCloseSpy extends ByteArrayInputStream {

    private boolean closed;

    public InputStreamCloseSpy(byte[] s) {
        super(s);
        closed=false;
    }

    public void close() throws IOException {
        super.close();
        closed=true;
    }

    public boolean isClosed() {
        return closed;
    }

}
