package org.jbehave.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <p>
 * Represents a text content that needs to rendered verbatim, ie as it is laid out in the original textual
 * representation.
 * <p/>
 *
 * <pre>
 *     Some textual value
 *   where we preserve spaces
 *      and new lines
 *
 * </pre>
 */
public class Verbatim {

    final String content;

    public Verbatim(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
