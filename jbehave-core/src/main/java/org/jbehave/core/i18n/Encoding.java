package org.jbehave.core.i18n;

import org.apache.commons.lang.CharEncoding;

/**
 * Specifies the encoding via a charset, defaulting
 * to "UTF-8" as {@link #DEFAULT_CHARSET_NAME}.
 */
public class Encoding {

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private final String charsetName;

    /**
     * Creates a Encoding using the {@link #DEFAULT_CHARSET_NAME} charset
     */
    public Encoding() {
        this(DEFAULT_CHARSET_NAME);
    }

    /**
     * Creates a Encoding using the specified charset
     * 
     * @param charsetName the name of the charset
     */
    public Encoding(String charsetName) {
        this.charsetName = charsetName;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public boolean isCharsetSupported() {
        return CharEncoding.isSupported(charsetName);
    }

}
