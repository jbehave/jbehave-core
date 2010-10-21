package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class GivenStories {
    
    public static final GivenStories EMPTY = new GivenStories("");

    private final List<GivenStory> givenStories = new ArrayList<GivenStory>();
    private final String givenStoriesAsString;
    private ExamplesTable examplesTable = ExamplesTable.EMPTY;

    public GivenStories(String givenStoriesAsString) {
        this.givenStoriesAsString = givenStoriesAsString;
        if ( !StringUtils.isBlank(givenStoriesAsString) ){
            parse();            
        }
    }

    private void parse() {
        givenStories.clear();
        for (String storyPath : givenStoriesAsString.split(",", -1)) {
            givenStories.add(new GivenStory(storyPath));
        }
    }

    public List<GivenStory> getStories() {
        for (GivenStory givenStory : givenStories) {            
            int examplesRow = givenStory.getExamplesRow();
            if ( examplesRow > -1 && examplesRow < examplesTable.getRowCount() ){
                givenStory.useParameters(examplesTable.getRow(examplesRow));
            }
        }
        return givenStories;
    }

    public List<String> getPaths() {
        List<String> paths = new ArrayList<String>();
        for (GivenStory story : givenStories) {
            paths.add(story.asString().trim());
        }
        return Collections.unmodifiableList(paths);
    }

    public boolean requireExamplesTable() {
        for (GivenStory givenStory : givenStories) {
            if ( givenStory.getExamplesRow() > -1 ){
                return true;
            }
        }
        return false;
    }

    public void useExamplesTable(ExamplesTable examplesTable) {
        this.examplesTable = examplesTable;
    }
    
    public String asString() {
        return givenStoriesAsString;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


}
