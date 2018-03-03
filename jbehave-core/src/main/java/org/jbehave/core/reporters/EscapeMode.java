package org.jbehave.core.reporters;

import org.apache.commons.lang3.StringEscapeUtils;

public enum EscapeMode {
    HTML{
        @Override
        public String escapeString(String string) {
            return StringEscapeUtils.escapeHtml4(string);
        }
    },
    JSON{
        @Override
        public String escapeString(String string) {
            return StringEscapeUtils.escapeJson(string);
        }
    },
    XML{
        @Override
        public String escapeString(String string) {
            return StringEscapeUtils.escapeXml(string);
        }
    },
    NONE{
        @Override
        public String escapeString(String string) {
            return string;
        }
    };

    public abstract String escapeString(String string);

}
