package org.jbehave;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.hamcrest.Matcher;

/**
 * Collection of static utility ensure methods that use Hamcrest matchers
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 */
public class Ensure {
	
    public static void ensureThat(boolean matches) {
    	ensureThat(matches, is(true));
    }
    
    public static <T> void ensureThat(T actual, Matcher<T> matcher) {
        assertThat(actual, matcher);
    }

    public static boolean not(boolean matches) {
        return !matches;
    }

}
