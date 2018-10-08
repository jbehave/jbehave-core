package org.jbehave.core.io;

import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class IOUtilsBehaviour {

    @Test
    public void shouldProcessReader() throws IOException {
        assertThat(IOUtils.toString(new StringReader(""), true), equalTo(""));
        assertThat(IOUtils.toString(new StringReader("a"), true), equalTo("a"));
        assertThat(IOUtils.toString(new StringReader("asdf"), true), equalTo("asdf"));
        assertThat(IOUtils.toString(new StringReader("äöü"), true), equalTo("äöü"));

        // close() can be called more than once, a more elaborate test is below 
        Reader reader=new StringReader("hello");
        assertThat(IOUtils.toString(reader, false), equalTo("hello"));
        reader.close();

        String longString=createLongString();
        assertThat(IOUtils.toString(new StringReader(longString), true), equalTo(longString));

        // read an actual file
        assertThat(IOUtils.toString(new FileReader("src/test/resources/testfile"), true), equalTo("##########"));

    }

    @Test(expected=NullPointerException.class)
    public void shouldHandleReaderNull() throws IOException {
        // this causes a NPE in the apache-commons code, no point
        // in changing the logic in our implementation, I guess
        IOUtils.toString((Reader)null, true);
    }

    @Test
    public void shouldCloseReader() throws IOException {
        Reader reader = mock(Reader.class);
        when(reader.read(isA(char[].class))).thenReturn(-1);
        IOUtils.toString(reader, true);
        verify(reader).close();
    }

    @Test
    public void shouldNotCloseReader() throws IOException {
        Reader reader = mock(Reader.class);
        when(reader.read(isA(char[].class))).thenReturn(-1);
        IOUtils.toString(reader, false);
        verify(reader, never()).close();
    }

    @Test(expected = IOException.class)
    public void shouldCloseReaderException() throws IOException {
        Reader reader = mock(Reader.class);
        when(reader.read(isA(char[].class))).thenThrow(new IOException());
        try {
            IOUtils.toString(reader, true);
        } finally {
            verify(reader).close();
        }
    }

    // same for InputStream
    @Test
    public void shouldProcessInputStream() throws IOException {
        assertThat(IOUtils.toString(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), true), equalTo(""));
        assertThat(IOUtils.toString(new ByteArrayInputStream("a".getBytes(StandardCharsets.UTF_8)), true), equalTo("a"));
        assertThat(IOUtils.toString(new ByteArrayInputStream("asdf".getBytes(StandardCharsets.UTF_8)), true), equalTo("asdf"));
        assertThat(IOUtils.toString(new ByteArrayInputStream("äöü".getBytes(StandardCharsets.UTF_8)), true), equalTo("äöü"));

        ByteArrayInputStream input = new ByteArrayInputStream("asdf".getBytes(StandardCharsets.UTF_8));
        assertThat(IOUtils.toString(input, false), equalTo("asdf"));
        input.close();

        String longString=createLongString();
        assertThat(IOUtils.toString(new ByteArrayInputStream(longString.getBytes(StandardCharsets.UTF_8)), true), equalTo(longString));

        assertThat(IOUtils.toString(new FileInputStream("src/test/resources/testfile"), true), equalTo("##########"));

    }

    @Test(expected=NullPointerException.class)
    public void shouldHandleInputStreamNull() throws IOException {
        // this causes a NPE in the apache-commons code, no point
        // in changing the logic in our implementation, I guess
        IOUtils.toString((InputStream)null, true);
    }

    @Test
    public void shouldCloseInputStream() throws IOException {
        InputStream stream = mock(InputStream.class);
        when(stream.read(isA(byte[].class), anyInt(), anyInt())).thenReturn(-1);
        IOUtils.toString(stream, true);
        verify(stream).close();
    }

    @Test
    public void shouldNotCloseInputStream() throws IOException {
        InputStream stream = mock(InputStream.class);
        when(stream.read(isA(byte[].class), anyInt(), anyInt())).thenReturn(-1);
        IOUtils.toString(stream, false);
        verify(stream, never()).close();
    }

    @Test(expected = IOException.class)
    public void shouldCloseInputStreamException() throws IOException {
        InputStream stream = mock(InputStream.class);
        when(stream.read(isA(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
        try {
            IOUtils.toString(stream, true);
        } finally {
            verify(stream).close();
        }
    }

    /*
     * create a 1mb String
     */
    private String createLongString() {
        StringBuilder sb=new StringBuilder();
        sb.append("*");
        for(int i=0;i<20;i++) {
            sb.append(sb);
        }
        return sb.toString();
    }

}

