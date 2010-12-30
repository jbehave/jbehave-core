package org.jbehave.core.io;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class RelativePathCalculatorBehaviour {

    private RelativePathCalculator calculator;

    @Before
    public void setUp() throws Exception {
        calculator = new RelativePathCalculator();
    }

    @Test
    public void shouldReturnAbsolutePaths() throws Exception {
        MatcherAssert.assertThat(calculator.calculate("", "/file.story"), is("file.story"));
        MatcherAssert.assertThat(calculator.calculate("a/path/", "/file.story"), is("file.story"));

        MatcherAssert.assertThat(calculator.calculate("/", "/file.story"), is("file.story"));
        MatcherAssert.assertThat(calculator.calculate("a/path/a.txt", "/file.story"), is("file.story"));
    }

    @Test
    public void shouldReturnPathsRelativeToFiles() throws Exception {
        MatcherAssert.assertThat(calculator.calculate("a.txt", "file.story"), is("file.story"));
        MatcherAssert.assertThat(calculator.calculate("a/path/a.txt", "file.story"), is("a/path/file.story"));
    }
}
