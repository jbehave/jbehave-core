package org.jbehave.core.io;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class IOUtilsBehaviour {

    @Test
    void shouldProcessReader() throws IOException {
        assertThat(IOUtils.toString(new StringReader(""), true), equalTo(""));
        assertThat(IOUtils.toString(new StringReader("a"), true), equalTo("a"));
        assertThat(IOUtils.toString(new StringReader("asdf"), true), equalTo("asdf"));
        assertThat(IOUtils.toString(new StringReader("äöü"), true), equalTo("äöü"));

        // close() can be called more than once, a more elaborate test is below 
        Reader reader = new StringReader("hello");
        assertThat(IOUtils.toString(reader, false), equalTo("hello"));
        reader.close();

        String longString = createLongString();
        assertThat(IOUtils.toString(new StringReader(longString), true), equalTo(longString));

        // read an actual file
        assertThat(IOUtils.toString(new FileReader("src/test/resources/testfile"), true), equalTo("##########"));

    }

    @Test
    void shouldHandleReaderNull() {
        // this causes a NPE in the apache-commons code, no point
        // in changing the logic in our implementation, I guess
        assertThrows(NullPointerException.class, () -> IOUtils.toString((Reader) null, true));
    }

    @Test
    void shouldCloseReader() throws IOException {
        Reader reader = mock(Reader.class);
        when(reader.read(isA(char[].class))).thenReturn(-1);
        IOUtils.toString(reader, true);
        verify(reader).close();
    }

    @Test
    void shouldNotCloseReader() throws IOException {
        Reader reader = mock(Reader.class);
        when(reader.read(isA(char[].class))).thenReturn(-1);
        IOUtils.toString(reader, false);
        verify(reader, never()).close();
    }

    @Test
    void shouldCloseReaderException() throws IOException {
        Reader reader = mock(Reader.class);
        when(reader.read(isA(char[].class))).thenThrow(new IOException());
        assertThrows(IOException.class, () -> IOUtils.toString(reader, true));
        verify(reader).close();
    }

    // same for InputStream
    @Test
    void shouldProcessInputStream() throws IOException {
        assertThat(IOUtils.toString(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), true), equalTo(""));
        assertThat(IOUtils.toString(new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8)), true), equalTo("a"));
        assertThat(IOUtils.toString(new ByteArrayInputStream("asdf".getBytes(StandardCharsets.UTF_8)), true), equalTo("asdf"));
        assertThat(IOUtils.toString(new ByteArrayInputStream("äöü".getBytes(StandardCharsets.UTF_8)), true), equalTo("äöü"));

        ByteArrayInputStream input = new ByteArrayInputStream("asdf".getBytes(StandardCharsets.UTF_8));
        assertThat(IOUtils.toString(input, false), equalTo("asdf"));
        input.close();

        String longString = createLongString();
        assertThat(IOUtils.toString(new ByteArrayInputStream(longString.getBytes(StandardCharsets.UTF_8)), true), equalTo(longString));

        assertThat(IOUtils.toString(new FileInputStream("src/test/resources/testfile"), true), equalTo("##########"));

    }

    @Test
    void shouldHandleInputStreamNull() {
        // this causes a NPE in the apache-commons code, no point
        // in changing the logic in our implementation, I guess
        assertThrows(NullPointerException.class, () -> IOUtils.toString((InputStream) null, true));
    }

    @Test
    void shouldCloseInputStream() throws IOException {
        InputStream stream = mock(InputStream.class);
        when(stream.read(isA(byte[].class), anyInt(), anyInt())).thenReturn(-1);
        IOUtils.toString(stream, true);
        verify(stream).close();
    }

    @Test
    void shouldNotCloseInputStream() throws IOException {
        InputStream stream = mock(InputStream.class);
        when(stream.read(isA(byte[].class), anyInt(), anyInt())).thenReturn(-1);
        IOUtils.toString(stream, false);
        verify(stream, never()).close();
    }

    @Test
    void shouldCloseInputStreamException() throws IOException {
        InputStream stream = mock(InputStream.class);
        when(stream.read(isA(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
        try {
            IOUtils.toString(stream, true);
        } catch (Exception e) {
            assertThat(e, is(instanceOf(IOException.class)));
        } finally {
            verify(stream).close();
        }
    }

    /*
     * create a 1mb String
     */
    private String createLongString() {
        StringBuilder sb = new StringBuilder();
        sb.append("*");
        for (int i = 0; i < 20; i++) {
            sb.append(sb);
        }
        return sb.toString();
    }

}

