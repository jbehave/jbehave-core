package org.jbehave.core.i18n;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.jbehave.core.i18n.StringCoder.DEFAULT_CHARSET_NAME;

import org.jbehave.core.i18n.StringCoder.EncodingInvalid;
import org.junit.Test;

public class StringCoderBehaviour {
    
    @Test
    public void shouldSupportUTF8AsDefault() {        
        StringCoder coder = new StringCoder();
        assertThat(coder.getCharsetName(), equalTo(DEFAULT_CHARSET_NAME));
        assertThat(coder.isCharsetSupported(), is(true));
        String input = "Et voilà";
        assertThat(coder.canonicalize(input), equalTo(input));
    }

    @Test(expected=EncodingInvalid.class)
    public void shouldNotSupportInvalidCharset() {        
        String charsetName = "invalid";
        StringCoder coder = new StringCoder(charsetName);
        assertThat(coder.getCharsetName(), equalTo(charsetName));
        assertThat(coder.isCharsetSupported(), is(false));
        coder.canonicalize("anything");
    }
}
