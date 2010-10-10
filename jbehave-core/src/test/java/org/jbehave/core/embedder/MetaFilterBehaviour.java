package org.jbehave.core.embedder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class MetaFilterBehaviour {

    @Test
    public void shouldParseIncludesAndExcludes(){
        String filterAsString = "+author Mauro -theme smoke testing +map UI -author Paul";
        MetaFilter filter = new MetaFilter(filterAsString);
        assertThat(filter.asString(), equalTo(filterAsString));
        assertThat(filter.include().toString(), equalTo("{author=Mauro, map=UI}"));
        assertThat(filter.exclude().toString(), equalTo("{author=Paul, theme=smoke testing}"));
    }
    
}
