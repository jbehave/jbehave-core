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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * <p>Freemarker-based {@link ViewGenerator}, using the file outputs of the
 * reporters for the given formats. The FTL templates for the index and single
 * views are injectable the {@link #generateView(File, List, Properties)}
 * but defaults are provided. To override, specify the path the
 * new template under keys "index", "decorated" and "nonDecorated".</p>
 * <p>The view generator provides the following resources:
 * <pre>
 * resources.setProperty("index", "ftl/jbehave-reports-index.ftl");
 * resources.setProperty("decorated", "ftl/jbehave-report-decorated.ftl");
 * resources.setProperty("nonDecorated", "ftl/jbehave-report-non-decorated.ftl");
 * resources.setProperty("decorateNonHtml", "true");
 * resources.setProperty("defaultFormats", "stats");
 * resources.setProperty("viewDirectory", "view");
 * </pre>  
 * </p>
 * @author Mauro Talevi
 */
public class FreemarkerViewGenerator implements ViewGenerator {

    private final Configuration configuration;
    private Properties resources;
	private List<Report> reports = new ArrayList<Report>();

    public FreemarkerViewGenerator() {
        this.configuration = configure();
    }

    public static Properties defaultResources() {
        Properties resources = new Properties();
        resources.setProperty("index", "ftl/jbehave-reports-index.ftl");
        resources.setProperty("decorated", "ftl/jbehave-report-decorated.ftl");
        resources.setProperty("nonDecorated", "ftl/jbehave-report-non-decorated.ftl");
        resources.setProperty("decorateNonHtml", "true");
        resources.setProperty("defaultFormats", "stats");
        resources.setProperty("viewDirectory", "view");
        return resources;
    }

    private Properties mergeWithDefault(Properties resources) {
        Properties merged = defaultResources();
        merged.putAll(resources);
        return merged;
    }

    public void generateView(File outputDirectory, List<String> formats, Properties resources) {
        this.resources = mergeWithDefault(resources);
        createIndex(outputDirectory, formats);
    }

    public int countStories(){
    	return reports.size();
    }
    
    public int countScenarios(){
		return count("scenarios", reports);
    }

    public int countFailedScenarios(){
		return count("scenariosFailed", reports);
    }

	private int count(String event, List<Report> reports) {
		int count = 0;
    	for (Report report : reports) {
			Properties stats = report.asProperties("stats");
			if ( stats != null ){
				if ( stats.containsKey(event)){
					int failed = Integer.parseInt((String)stats.get(event));
					count = count + failed;
				}
			}
		}
    	return count;
	}
	
    private void createIndex(File outputDirectory, List<String> formats) {
        String outputName = templateResource("viewDirectory")+"/index.html";
        String index = templateResource("index");
        List<String> mergedFormats = mergeWithDefaults(formats);
        reports = toReports(indexedReportFiles(outputDirectory, outputName, mergedFormats));
        Map<String, Object> dataModel = newDataModel();
        dataModel.put("reports", reports);
        dataModel.put("date", new Date());
        write(outputDirectory, outputName, index, dataModel);
    }

    private List<String> mergeWithDefaults(List<String> formats) {
        List<String> merged = new ArrayList<String>();
        merged.addAll(asList(templateResource("defaultFormats").split(",")));        
        merged.addAll(formats);
        return merged;
    }

    private SortedMap<String, List<File>> indexedReportFiles(File outputDirectory, final String outputName,
            final List<String> formats) {
        SortedMap<String, List<File>> reports = new TreeMap<String, List<File>>();
        if (outputDirectory == null || !outputDirectory.exists()) {
            return reports;
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
            List<File> filesByName = reports.get(name);
            if (filesByName == null) {
                filesByName = new ArrayList<File>();
                reports.put(name, filesByName);
            }
            filesByName.add(new File(outputDirectory, fileName));
        }
        return reports;
    }

    private List<Report> toReports(Map<String, List<File>> reportFiles) {
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
                    String outputName = viewDirectory+ "/" + fileName;
                    String template = decoratedTemplate;
                    if (!format.equals("html")) {
                    	if ( decorateNonHtml ){
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
            throw new RuntimeException(e);
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
        String resource = resources.getProperty(format);
        if (resource == null) {
            throw new ViewTemplateNotFoundForFormat(format);
        }
        return resource;
    }

    private Map<String, Object> newDataModel() {
        return new HashMap<String, Object>();
    }

    @SuppressWarnings("serial")
    public static class ViewGenerationFailedForTemplate extends RuntimeException {

        public ViewGenerationFailedForTemplate(String resource, Exception cause) {
            super(resource, cause);
        }

    }

    @SuppressWarnings("serial")
    public static class ViewTemplateNotFoundForFormat extends RuntimeException {

        public ViewTemplateNotFoundForFormat(String format) {
            super(format);
        }

    }

    public static class Report {

        private final String name;
        private final Map<String, File> filesByFormat;

        public Report(String name, Map<String, File> filesByFormat) {
            this.name = name;
            this.filesByFormat = filesByFormat;
        }

        public String getName() {
            return name;
        }

        public Map<String, File> getFilesByFormat() {
            return filesByFormat;
        }
        
        public Properties asProperties(String format){
            Properties p = new Properties();
            File stats = filesByFormat.get(format);
            if ( stats == null ){
                return p;
            }
            try {
                p.load(new FileInputStream(stats));
            } catch (IOException e) {
                // return empty map
            }
            return p;
        }

    }
}
