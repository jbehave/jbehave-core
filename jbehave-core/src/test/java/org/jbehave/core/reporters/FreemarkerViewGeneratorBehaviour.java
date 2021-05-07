package org.jbehave.core.reporters;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;

import org.jbehave.core.reporters.TemplateableViewGenerator.Report;
import org.jbehave.core.reporters.TemplateableViewGenerator.ReportCreationFailed;
import org.jbehave.core.reporters.TemplateableViewGenerator.Reports;
import org.jbehave.core.reporters.TemplateableViewGenerator.TimeFormatter;
import org.junit.jupiter.api.Test;

class FreemarkerViewGeneratorBehaviour {

    @Test
    void shouldCountEvents() {
        // Given
        FreemarkerViewGenerator generator = new FreemarkerViewGenerator();

        // When
        Report report = mock(Report.class);
        Properties stats = new Properties();
        stats.setProperty("found", "1");
        when(report.asProperties("stats")).thenReturn(stats);
        Reports reports = mock(Reports.class);
        when(reports.getReports()).thenReturn(asList(report));

        // Then
        assertThat(generator.count("found", reports), equalTo(1));
        assertThat(generator.count("notFound", reports), equalTo(0));
        
    }

    @Test
    void shouldFindIndexedReportFiles() {
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
    void shouldHandleMissingOutputDirectory() {
        // Given
        FreemarkerViewGenerator generator = new FreemarkerViewGenerator();

        // Then
        assertThat(generator.readReportFiles(null, "index.html", asList("html", "txt")).size(), equalTo(0));
        assertThat(generator.readReportFiles(new File("inexistent"), "index.html", asList("html", "txt")).size(), equalTo(0));
    }
    
    
    @Test
    void shouldHandleInvalidReportFile() {
        // Given
        Map<String, File> filesByFormat = new HashMap<>();
        filesByFormat.put("format", null);
        Report report = new Report("name", filesByFormat);

        // When
        Properties properties = report.asProperties("format");

        // Then 
        assertThat(properties.size(), equalTo(0));
        
    }

    
    @Test
    void shouldFailToCreateReportsFromInvalidFiles() {
        // Given
        FreemarkerViewGenerator generator = new FreemarkerViewGenerator();

        // When
        Map<String, List<File>> files = new HashMap<>();
        files.put("name", asList((File)null));
        assertThrows(ReportCreationFailed.class, () -> generator.createReports(files));
        // Then .. fail as expected
        
    }
    
    @Test
    void shouldFormatTimeDurantionInMillis() {
        long s = 1000;
        long m = 60 * s;
        long h = 60 * m;
        TimeFormatter timeFormatter = new TimeFormatter();
        assertThat(timeFormatter.formatMillis(0), equalTo("00:00:00.000"));
        assertThat(timeFormatter.formatMillis(1), equalTo("00:00:00.001"));
        assertThat(timeFormatter.formatMillis(10), equalTo("00:00:00.010"));
        assertThat(timeFormatter.formatMillis(100), equalTo("00:00:00.100"));
        assertThat(timeFormatter.formatMillis(2*s+1), equalTo("00:00:02.001"));
        assertThat(timeFormatter.formatMillis(3*m+20*s+1), equalTo("00:03:20.001"));
        assertThat(timeFormatter.formatMillis(44*h+33*m+22*s+1), equalTo("44:33:22.001"));
    }
    
}
