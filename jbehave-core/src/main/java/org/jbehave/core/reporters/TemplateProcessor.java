package org.jbehave.core.reporters;

import java.io.Writer;
import java.util.Map;

public interface TemplateProcessor {
    void process(String resource, Map<String, Object> dataModel, Writer writer);
}