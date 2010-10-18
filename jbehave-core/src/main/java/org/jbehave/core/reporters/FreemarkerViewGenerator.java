package org.jbehave.core.reporters;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.io.StoryNameResolver;
import org.jbehave.core.io.UnderscoredToCapitalized;
import org.jbehave.core.model.StoryLanes;
import org.jbehave.core.model.StoryMap;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * <p>
 * Freemarker-based {@link ViewGenerator}, using the file outputs of the
 * reporters for the given formats. The FTL templates for the index and single
 * views are injectable the {@link #generateReportsView(File, List, Properties)}
 * but defaults are provided. To override, specify the path the new template
 * under keys "index", "decorated" and "nonDecorated".
 * </p>
 * <p>
 * The view generator provides the following resources:
 * 
 * <pre>
 * resources.setProperty(&quot;maps&quot;, &quot;ftl/jbehave-story-maps.ftl&quot;);
 * resources.setProperty(&quot;index&quot;, &quot;ftl/jbehave-reports-index-with-totals.ftl&quot;);
 * resources.setProperty(&quot;decorated&quot;, &quot;ftl/jbehave-report-decorated.ftl&quot;);
 * resources.setProperty(&quot;nonDecorated&quot;, &quot;ftl/jbehave-report-non-decorated.ftl&quot;);
 * resources.setProperty(&quot;decorateNonHtml&quot;, &quot;true&quot;);
 * resources.setProperty(&quot;defaultFormats&quot;, &quot;stats&quot;);
 * resources.setProperty(&quot;viewDirectory&quot;, &quot;view&quot;);
 * </pre>
 * 
 * </p>
 * 
 * @author Mauro Talevi
 */
public class FreemarkerViewGenerator implements ViewGenerator {

    private final Configuration configuration;
    private Properties viewProperties;
    private List<Report> reports = new ArrayList<Report>();
    private StoryNameResolver nameResolver = new UnderscoredToCapitalized();
    
    public FreemarkerViewGenerator() {
        this.configuration = configure();
    }

    public static Properties defaultViewProperties() {
        Properties properties = new Properties();
        properties.setProperty("maps", "ftl/jbehave-story-maps.ftl");
        properties.setProperty("index", "ftl/jbehave-reports-index-with-totals.ftl");
        properties.setProperty("decorated", "ftl/jbehave-report-decorated.ftl");
        properties.setProperty("nonDecorated", "ftl/jbehave-report-non-decorated.ftl");
        properties.setProperty("decorateNonHtml", "true");
        properties.setProperty("defaultFormats", "stats");
        properties.setProperty("viewDirectory", "view");
        return properties;
    }

    private Properties mergeWithDefault(Properties properties) {
        Properties merged = defaultViewProperties();
        merged.putAll(properties);
        return merged;
    }

    public void generateStoryMapsView(File outputDirectory, List<StoryMap> storyMaps, Properties viewProperties) {
        this.viewProperties = mergeWithDefault(viewProperties);
        createMaps(outputDirectory, storyMaps);
    }

    private void createMaps(File outputDirectory, List<StoryMap> storyMaps) {
        String outputName = templateResource("viewDirectory") + "/maps.html";
        String maps = templateResource("maps");
        Map<String, Object> dataModel = newDataModel();
        dataModel.put("storyLanes", new StoryLanes(storyMaps, nameResolver));
        dataModel.put("date", new Date());
        write(outputDirectory, outputName, maps, dataModel);
    }

    public void generateReportsView(File outputDirectory, List<String> formats, Properties viewProperties) {
        this.viewProperties = mergeWithDefault(viewProperties);
        createReportsIndex(outputDirectory, formats);
    }

    public ReportsCount getReportsCount() {
        int stories = reports.size() - 1; // don't count the totals reports
        int scenarios = count("scenarios", reports);
        int failedScenarios = count("scenariosFailed", reports);
        return new ReportsCount(stories, scenarios, failedScenarios);
    }

    int count(String event, Collection<Report> reports) {
        int count = 0;
        for (Report report : reports) {
            Properties stats = report.asProperties("stats");
            if (stats.containsKey(event)) {
                count = count + Integer.parseInt((String) stats.get(event));
            }
        }
        return count;
    }

    private void createReportsIndex(File outputDirectory, List<String> formats) {
        String outputName = templateResource("viewDirectory") + "/index.html";
        String index = templateResource("index");
        List<String> mergedFormats = mergeWithDefaults(formats);
        reports = createReports(readReportFiles(outputDirectory, outputName, mergedFormats));
        Map<String, Object> dataModel = newDataModel();
        dataModel.put("reportsTable", new ReportsTable(reports, nameResolver));
        dataModel.put("date", new Date());
        write(outputDirectory, outputName, index, dataModel);
    }

    private List<String> mergeWithDefaults(List<String> formats) {
        List<String> merged = new ArrayList<String>();
        merged.addAll(asList(templateResource("defaultFormats").split(",")));
        merged.addAll(formats);
        return merged;
    }

    SortedMap<String, List<File>> readReportFiles(File outputDirectory, final String outputName,
            final List<String> formats) {
        SortedMap<String, List<File>> reportFiles = new TreeMap<String, List<File>>();
        if (outputDirectory == null || !outputDirectory.exists()) {
            return reportFiles;
        }
        String[] fileNames = outputDirectory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.equals(outputName) && hasFormats(name, formats);
            }

            private boolean hasFormats(String name, List<String> formats) {
                for (String format : formats) {
                    if (name.endsWith(format)) {
                        return true;
                    }
                }
                return false;
            }
        });
        for (String fileName : fileNames) {
            String name = FilenameUtils.getBaseName(fileName);
            List<File> filesByName = reportFiles.get(name);
            if (filesByName == null) {
                filesByName = new ArrayList<File>();
                reportFiles.put(name, filesByName);
            }
            filesByName.add(new File(outputDirectory, fileName));
        }
        return reportFiles;
    }

    List<Report> createReports(Map<String, List<File>> reportFiles) {
        try {
            String decoratedTemplate = templateResource("decorated");
            String nonDecoratedTemplate = templateResource("nonDecorated");
            String viewDirectory = templateResource("viewDirectory");
            boolean decorateNonHtml = Boolean.valueOf(templateResource("decorateNonHtml"));
            List<Report> reports = new ArrayList<Report>();
            for (String name : reportFiles.keySet()) {
                Map<String, File> filesByFormat = new HashMap<String, File>();
                for (File file : reportFiles.get(name)) {
                    String fileName = file.getName();
                    String format = FilenameUtils.getExtension(fileName);
                    Map<String, Object> dataModel = newDataModel();
                    dataModel.put("name", name);
                    dataModel.put("body", IOUtils.toString(new FileReader(file)));
                    dataModel.put("format", format);
                    File outputDirectory = file.getParentFile();
                    String outputName = viewDirectory + "/" + fileName;
                    String template = decoratedTemplate;
                    if (!format.equals("html")) {
                        if (decorateNonHtml) {
                            outputName = outputName + ".html";
                        } else {
                            template = nonDecoratedTemplate;
                        }
                    }
                    File written = write(outputDirectory, outputName, template, dataModel);
                    filesByFormat.put(format, written);
                }
                reports.add(new Report(name, filesByFormat));
            }
            return reports;
        } catch (Exception e) {
            throw new ReportCreationFailed(reportFiles, e);
        }
    }

    private File write(File outputDirectory, String outputName, String resource, Map<String, Object> dataModel) {
        try {
            File file = new File(outputDirectory, outputName);
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            process(resource, dataModel, writer);
            return file;
        } catch (Exception e) {
            throw new ViewGenerationFailedForTemplate(resource, e);
        }
    }

    private Configuration configure() {
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(FreemarkerViewGenerator.class, "/");
        configuration.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        return configuration;
    }

    private void process(String resource, Map<String, Object> dataModel, Writer writer) throws TemplateException,
            IOException {
        Template template = configuration.getTemplate(resource);
        template.process(dataModel, writer);
    }

    private String templateResource(String format) {
        return viewProperties.getProperty(format);
    }

    private Map<String, Object> newDataModel() {
        return new HashMap<String, Object>();
    }

    @SuppressWarnings("serial")
    public static class ReportCreationFailed extends RuntimeException {

        public ReportCreationFailed(Map<String, List<File>> reportFiles, Exception cause) {
            super("Report creation failed from file " + reportFiles, cause);
        }
    }

    @SuppressWarnings("serial")
    public static class ViewGenerationFailedForTemplate extends RuntimeException {

        public ViewGenerationFailedForTemplate(String resource, Exception cause) {
            super(resource, cause);
        }

    }

    public static class ReportsTable{

        private final Map<String, Report> reports = new HashMap<String, Report>();
        private final StoryNameResolver nameResolver;

        public ReportsTable(List<Report> reports, StoryNameResolver nameResolver) {           
            this.nameResolver = nameResolver;
            index(reports);
            addTotalsReport();
        }

        private void index(List<Report> reports) {
            for (Report report : reports) {                
                report.nameAs(nameResolver.resolveName(report.getPath()));
                this.reports.put(report.getName(), report);
            }
        }

        private void addTotalsReport() {
            Report report = totals(reports.values());
            report.nameAs(nameResolver.resolveName(report.getPath()));
            reports.put(report.getName(), report);
        }

        private Report totals(Collection<Report> values) {
            Map<String, Integer> totals = new HashMap<String, Integer>();
            for (Report report : values) {
                Map<String, Integer> stats = report.getStats();
                for (String key : stats.keySet()) {
                    Integer total = totals.get(key);
                    if (total == null) {
                        total = 0;
                    }
                    total = total + stats.get(key);
                    totals.put(key, total);
                }
            }
            return new Report("totals", new HashMap<String, File>(), totals);
        }
        
        
        public List<Report> getReports(){
            List<Report> list = new ArrayList<Report>(reports.values());
            Collections.sort(list);
            return list;
        }
        
        public List<String> getReportNames(){
            List<String> list = new ArrayList<String>(reports.keySet());
            Collections.sort(list);
            return list;
        }
        
        public Report getReport(String name){
            return reports.get(name);
        }
    }

    public static class Report implements Comparable<Report> {

        private final String path;
        private final Map<String, File> filesByFormat;
        private Map<String, Integer> stats;
        private String name;

        public Report(String path, Map<String, File> filesByFormat) {
            this(path, filesByFormat, null);
        }

        public Report(String path, Map<String, File> filesByFormat, Map<String, Integer> stats) {
            this.path = path;
            this.filesByFormat = filesByFormat;
            this.stats = stats;
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name != null ? name : path;
        }

        public void nameAs(String name) {
            this.name = name;
        }

        public Map<String, File> getFilesByFormat() {
            return filesByFormat;
        }

        public Properties asProperties(String format) {
            Properties p = new Properties();
            File stats = filesByFormat.get(format);
            try {
                p.load(new FileInputStream(stats));
            } catch (Exception e) {
                // return empty map
            }
            return p;
        }

        public Map<String, Integer> getStats() {
            if (stats == null) {
                Properties p = asProperties("stats");
                stats = new HashMap<String, Integer>();
                for (Enumeration<?> e = p.propertyNames(); e.hasMoreElements();) {
                    String key = (String) e.nextElement();
                    stats.put(key, valueOf(key, p));
                }
            }
            return stats;
        }

        private Integer valueOf(String key, Properties p) {
            try {
                return Integer.valueOf(p.getProperty(key));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        public int compareTo(Report that) {
            return CompareToBuilder.reflectionCompare(this.getName(), that.getName());
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(path).toString();
        }
    }

}
