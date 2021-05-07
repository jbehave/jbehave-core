package org.jbehave.core.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class RelativePathCalculatorBehaviour {

    private RelativePathCalculator calculator = new RelativePathCalculator();

    @Test
    void shouldReturnAbsolutePaths() {
        assertThat(calculator.calculate("", "/file.story"), is("file.story"));
        assertThat(calculator.calculate("a/path/", "/file.story"), is("file.story"));
        assertThat(calculator.calculate("/", "/file.story"), is("file.story"));
        assertThat(calculator.calculate("a/path/a.txt", "/file.story"), is("file.story"));
    }

    @Test
    void shouldReturnPathsRelativeToFiles() {
        assertThat(calculator.calculate("a.txt", "file.story"), is("file.story"));
        assertThat(calculator.calculate("a/path/a.txt", "file.story"), is("a/path/file.story"));
    }
    
}
