package org.jbehave.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A collection of utility methods performing I/O operations,
 * complementing IOUtils methods provided by other libraries.
 */
public class IOUtils {

    public static String toString(InputStream input, Charset charset, boolean close) throws IOException {
        try {
            return org.apache.commons.io.IOUtils.toString(input, charset.name());
        } finally {
            if (close) {
                input.close();
            }
        }
    }

    /**
     * Returns the content of the InputStream as a String, closing the stream afterwards if configured.
     * 
     * @param input the InputStream
     * @param close the boolean to close the input afterwards
     * @return A String with the content 
     * @throws IOException
     * @see {@link org.apache.commons.io.IOUtils#toString(InputStream)}
     */
    public static String toString(InputStream input, boolean close) throws IOException {
        return toString(input, StandardCharsets.UTF_8, close);
    }

    /**
     * Returns the content of the Reader as a String, closing the stream afterwards if configured.
     * 
     * @param input the Reader
     * @param close the boolean to close the input afterwards
     * @return A String with the content 
     * @throws IOException
     * @see {@link org.apache.commons.io.IOUtils#toString(Reader)}
     */
    public static String toString(Reader input, boolean close) throws IOException {
        try {
            return org.apache.commons.io.IOUtils.toString(input);
        } finally {
            if (close) {
                input.close();
            }
        }
    }

}
