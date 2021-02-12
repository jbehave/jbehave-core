package org.jbehave.io;

import java.io.PrintStream;

public class SilentFileMonitor extends PrintStreamFileMonitor {

    protected void print(PrintStream output, String message, Exception cause) {
        // print nothing
    }
    
}
