package org.jbehave.core.i18n;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.CharEncoding;

/**
 * Supports encoding and decoding of Strings using specified charset, defaulting
 * to "UTF-8" as {@link #DEFAULT_CHARSET_NAME}.
 */
public class StringCoder {

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private final String charsetName;

    /**
     * Creates a StringCoder using the {@link #DEFAULT_CHARSET_NAME} charset
     */
    public StringCoder() {
        this(DEFAULT_CHARSET_NAME);
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
     * Canonicalizes input, i.e. encodes (String to byte[]) and decodes (byte[]
     * to String), using the configured charset.
     * 
     * @param input the String input
     * @return Canonicalized String
     * @throws EncodingInvalid if encoding is not supported for charset
     */
    public String canonicalize(String input) {
        try {
            return new String(input.getBytes(charsetName), charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new EncodingInvalid(charsetName, e);
        }
    }

    @SuppressWarnings("serial")
    public static final class EncodingInvalid extends RuntimeException {

        public EncodingInvalid(String charsetName, UnsupportedEncodingException cause) {
            super(charsetName, cause);
        }

    }

}
