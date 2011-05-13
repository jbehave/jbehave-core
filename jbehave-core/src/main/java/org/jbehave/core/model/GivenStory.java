package org.jbehave.core.model;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class GivenStory {

    private final String givenStoryAsString;
    private Map<String, String> parameters = new HashMap<String, String>();
    private String path;
    private String anchor;

    public GivenStory(String givenStoryAsString) {
        this.givenStoryAsString = givenStoryAsString.trim();
        parse();
    }

    private void parse() {
        Pattern pattern = compile("(.*)\\#\\{(.*?)\\}", DOTALL);
        Matcher matcher = pattern.matcher(givenStoryAsString.trim());
        if (matcher.matches()) {
            path = matcher.group(1).trim();
            anchor = matcher.group(2).trim();
        } else {
            path = givenStoryAsString;
            anchor = "";
        }
    }

    public String getPath() {
        return path;
    }

    public String getAnchor() {
        return anchor;
    }

    public boolean hasAnchor() {
        return !StringUtils.isBlank(anchor);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void useParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String asString() {
        return givenStoryAsString;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
