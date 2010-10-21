package org.jbehave.core.model;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class GivenStory {

    private final String givenStoryAsString;
    private String path;
    private int examplesRow;
    private Map<String, String> parameters = new HashMap<String, String>();

    public GivenStory(String storyPath) {
        this.givenStoryAsString = storyPath;
        parse(givenStoryAsString);
    }

    private void parse(String givenStoryAsString) {
        Pattern pattern = compile("(.*)\\#\\{(.*?)\\}", DOTALL);
        Matcher matcher = pattern.matcher(givenStoryAsString.trim());
        if (matcher.matches()) {
            path = matcher.group(1);
            examplesRow = Integer.parseInt(matcher.group(2));
        } else {
            path = givenStoryAsString;
            examplesRow = -1;
        }
        
    }

    public String getPath() {
        return path;
    }

    public int getExamplesRow(){
        return examplesRow;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }

    public void useParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public String asString(){
        return givenStoryAsString;
    }
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
