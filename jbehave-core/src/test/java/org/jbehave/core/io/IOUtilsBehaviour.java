package org.jbehave.core.io;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.isA;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

public class IOUtilsBehaviour {

    @Test
    public void shouldProcessReader() throws IOException {
        assertEquals("", IOUtils.toString(new StringReader(""), true));
        assertEquals("a", IOUtils.toString(new StringReader("a"), true));
        assertEquals("asdf", IOUtils.toString(new StringReader("asdf"), true));
        assertEquals("äöü", IOUtils.toString(new StringReader("äöü"), true));

        // close() can be called more than once, a more elaborate test is below 
        Reader reader=new StringReader("hello");
        assertEquals("hello", IOUtils.toString(reader, false));
        reader.close();

        String longString=createLongString();
        assertEquals(longString, IOUtils.toString(new StringReader(longString), true));

        // read an actual file
        assertEquals("##########", IOUtils.toString(new FileReader("src/test/resources/testfile"), true));

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

    @Test
    public void shouldCloseReaderException() throws IOException {
        Reader reader = mock(Reader.class);
        when(reader.read(isA(char[].class))).thenThrow(new IOException());
        try {
            IOUtils.toString(reader, true);
        }
        catch(IOException ioex) {
            // expected
        }
        verify(reader).close();
    }

    // same for InputStream
    @Test
    public void shouldProcessInputStream() throws IOException {
        assertEquals("", IOUtils.toString(new ByteArrayInputStream("".getBytes()), true));
        assertEquals("a", IOUtils.toString(new ByteArrayInputStream("a".getBytes()), true));
        assertEquals("asdf", IOUtils.toString(new ByteArrayInputStream("asdf".getBytes()), true));
        assertEquals("äöü", IOUtils.toString(new ByteArrayInputStream("äöü".getBytes()), true));

        ByteArrayInputStream input = new ByteArrayInputStream("asdf".getBytes());
        assertEquals("asdf", IOUtils.toString(input, false));
        input.close();

        String longString=createLongString();
        assertEquals(longString, IOUtils.toString(new ByteArrayInputStream(longString.getBytes()), true));

        assertEquals("##########", IOUtils.toString(new FileInputStream("src/test/resources/testfile"), true));

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

    @Test
    public void shouldCloseInputStreamException() throws IOException {
        InputStream stream = mock(InputStream.class);
        when(stream.read(isA(byte[].class), anyInt(), anyInt())).thenThrow(new IOException());
        try {
            IOUtils.toString(stream, true);
        }
        catch(IOException ioex) {
            // expected
        }
        verify(stream).close();
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

