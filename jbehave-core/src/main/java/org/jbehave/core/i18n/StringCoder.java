package org.jbehave.core.i18n;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.lang.CharEncoding;

/**
 * Supports encoding and decoding of strings using specified charsets.
 */
public class StringCoder {

    public enum Mode {
        STRICT, WARN
    }

    private final String charsetName;

    /**
     * Creates a StringCoder using {@link Charset.defaultCharset().name()}
     */
    public StringCoder() {
        this(Charset.defaultCharset().name());
    }

    /**
     * Creates a StringCoder using the specified charset
     * 
     * @param charsetName the name of the charset
     */
    public StringCoder(String charsetName) {
        this.charsetName = charsetName;
    }

    public String getCharsetName() {
        return charsetName;
    }
    
    public boolean isCharsetSupported() {
        return CharEncoding.isSupported(charsetName);
    }

    /**
     * Encodes and decodes input using the coder charset
     * 
     * @param input the String input 
     * @return String after encoding and decoding
     * @throws InvalidEncodingException if encoding is not supported for charset
     */
    public String canonicalize(String input) {
        try {
            return new String(input.getBytes(charsetName), charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new InvalidEncodingException(input, e);
        }
    }

    public void validateEncoding(String input, Mode mode) {
    }

    @SuppressWarnings("serial")
    public static final class InvalidEncodingException extends RuntimeException {

        public InvalidEncodingException(String message) {
            super(message);
        }

        public InvalidEncodingException(String input, UnsupportedEncodingException cause) {
            super(input, cause);
        }

    }

}
