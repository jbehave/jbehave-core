/**
 * 
 */
package org.jbehave.scenario.i18n;

import java.io.UnsupportedEncodingException;

/**
 * Supports encoding of strings using specified charsets for encoding (i.e. from
 * String to byte[]) and decoding (i.e. from byte[] to String).
 */
public class StringEncoder {

	public static final String UTF_8 = "UTF-8";
	private String encoding;
	private String decoding;

	/**
	 * Creates an encoder using "UTF-8" for encoding and decoding
	 */
	public StringEncoder() {
		this(UTF_8, UTF_8);
	}

	/**
	 * Creates an encoder using the specifed charsets for encoding and decoding
	 * 
	 * @param encoding
	 *            the name of the encoding charset
	 * @param decoding
	 *            the name of the decoding charset
	 */
	public StringEncoder(String encoding, String decoding) {
		this.encoding = encoding;
		this.decoding = decoding;
	}

	public String encode(String value) {
		try {
			return new String(value.getBytes(encoding), decoding);
		} catch (UnsupportedEncodingException e) {
			throw new InvalidEncodingExcepion(value, e);
		}
	}

	@SuppressWarnings("serial")
	public static final class InvalidEncodingExcepion extends RuntimeException {

		public InvalidEncodingExcepion(String value,
				UnsupportedEncodingException cause) {
			super(value, cause);
		}

	}

}