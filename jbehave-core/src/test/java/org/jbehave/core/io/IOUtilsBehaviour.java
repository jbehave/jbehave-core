package org.jbehave.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    // test if the close parameter works as expected closing the Reader
    public void shouldCloseReader() throws IOException {
        ReaderCloseSpy reader=new ReaderCloseSpy("aaa");
        assertEquals("aaa", IOUtils.toString(reader, true));
        assertTrue("method didn't close reader", reader.isClosed());

        reader=new ReaderCloseSpy("aaa");
        assertEquals("aaa", IOUtils.toString(reader, false));
        assertFalse("method closed reader, which wasn't requested", reader.isClosed());

        reader=new ReaderCloseSpyException("aaa");
        try {
            IOUtils.toString(reader, true);
        }
        catch(IOException ioex) {
            // expected
        }
        assertTrue("method didn't close reader on exception", reader.isClosed());
    }

    // same for InputStream
    @Test
    public void shouldProcessInputStream() throws IOException {
        assertEquals("", IOUtils.toString(new ByteArrayInputStream("".getBytes("utf-8")), true));
        assertEquals("a", IOUtils.toString(new ByteArrayInputStream("a".getBytes("utf-8")), true));
        assertEquals("asdf", IOUtils.toString(new ByteArrayInputStream("asdf".getBytes("utf-8")), true));
        assertEquals("äöü", IOUtils.toString(new ByteArrayInputStream("äöü".getBytes("utf-8")), true));

        ByteArrayInputStream input = new ByteArrayInputStream("asdf".getBytes("utf-8"));
        assertEquals("asdf", IOUtils.toString(input, false));
        input.close();

        String longString=createLongString();
        assertEquals(longString, IOUtils.toString(new ByteArrayInputStream(longString.getBytes("utf-8")), true));

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
        InputStreamCloseSpy inputStream=new InputStreamCloseSpy("aaa".getBytes("utf-8"));
        assertEquals("aaa", IOUtils.toString(inputStream, true));
        assertTrue("method didn't close reader", inputStream.isClosed());

        inputStream=new InputStreamCloseSpy("aaa".getBytes("utf-8"));
        assertEquals("aaa", IOUtils.toString(inputStream, false));
        assertFalse("method closed reader, which wasn't requested", inputStream.isClosed());

        inputStream=new InputStreamCloseSpyException("aaa".getBytes("utf-8"));
        try {
            IOUtils.toString(inputStream, true);
        }
        catch(RuntimeException e) {
            // expected
        }
        assertTrue("method didn't close input stream on exception", inputStream.isClosed());
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
