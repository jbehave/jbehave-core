package org.jbehave.core.reporters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.jbehave.core.io.IOUtils;
import org.xml.sax.SAXException;

public abstract class AbstractOutputBehaviour {

    protected void assertThatOutputIs(String out, String pathToExpected) throws IOException {
        String expected = dos2unix(IOUtils.toString(getClass().getResourceAsStream(pathToExpected), true));
        String actual = dos2unix(out);
        assertThat(actual, equalToCompressingWhiteSpace(expected));
    }

    protected String dos2unix(String string) {
        return string.replace("\r\n", "\n");
    }

    protected void assertFileOutputIsSameAs(File file, String name) throws IOException {
        String out = fileContent(file);
        assertThatOutputIs(out, "/" + name);
    }

    protected String fileContent(File file) throws IOException {
        return IOUtils.toString(new FileReader(file), true);
    }

    protected void validateFileOutput(File file) throws IOException, SAXException, ParserConfigurationException {
        if (file.getName().endsWith(".xml")) {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // will throw SAXException if the xml file is not well-formed
            documentBuilder.parse(file);
        } else if (file.getName().endsWith(".json")) {
            // will not throw JsonSyntaxException if the json file is not valid
            Gson gson = new Gson();
            String out = fileContent(file);
            JsonReader jsonReader = new JsonReader(new StringReader(out));
            jsonReader.setLenient(false);
            gson.fromJson(jsonReader, Object.class);
        }
    }

    protected File newFile(String path) {
        File file = new File(path);
        file.delete();
        return file;
    }
}
