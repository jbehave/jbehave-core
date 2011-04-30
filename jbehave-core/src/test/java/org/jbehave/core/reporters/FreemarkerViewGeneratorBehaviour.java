package org.jbehave.core.reporters;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;

import org.jbehave.core.reporters.FreemarkerViewGenerator.Report;
import org.jbehave.core.reporters.FreemarkerViewGenerator.ReportCreationFailed;
import org.junit.Test;

public class FreemarkerViewGeneratorBehaviour {

    @Test
    public void shouldCountEvents(){
        // Given
        FreemarkerViewGenerator generator = new FreemarkerViewGenerator();

        // When
        Report report = mock(Report.class);
        Properties stats = new Properties();
        stats.setProperty("found", "1");
        when(report.asProperties("stats")).thenReturn(stats);

        // Then
        assertThat(generator.count("found", asList(report)), equalTo(1));
        assertThat(generator.count("notFound", asList(report)), equalTo(0));
        
    }

    @Test
    public void shouldFindIndexedReportFiles(){
        // Given
        FreemarkerViewGenerator generator = new FreemarkerViewGenerator();

        File outputDirectory = new File("src/test/java/org/jbehave/core/reporters/reports");
        // When
        SortedMap<String, List<File>> files = generator.readReportFiles(outputDirectory, "index.html", asList("html", "txt"));

        // Then
        assertThat(files.size(), equalTo(2));
        assertThat(files.get("report1").size(), equalTo(2));
        assertThat(files.get("report2").size(), equalTo(2));
    }

    @Test
    public void shouldHandleMissingOutputDirectory(){
        // Given
        FreemarkerViewGenerator generator = new FreemarkerViewGenerator();

        // Then
        assertThat(generator.readReportFiles(null, "index.html", asList("html", "txt")).size(), equalTo(0));
        assertThat(generator.readReportFiles(new File("inexistent"), "index.html", asList("html", "txt")).size(), equalTo(0));
    }
    
    
    @Test
    public void shouldHandleInvalidReportFile(){
        // Given
        Map<String, File> filesByFormat = new HashMap<String, File>();
        filesByFormat.put("format", null);
        Report report = new Report("name", filesByFormat);

        // When
        Properties properties = report.asProperties("format");

        // Then 
        assertThat(properties.size(), equalTo(0));
        
    }

    
    @Test(expected = ReportCreationFailed.class)
    public void shouldFailToCreateReportsFromInvalidFiles(){
        // Given
        FreemarkerViewGenerator generator = new FreemarkerViewGenerator();

        // When
        Map<String, List<File>> files = new HashMap<String, List<File>>();
        files.put("name", asList((File)null));
        generator.createReports(files);

        // Then .. fail as expected
        
    }
    
}
