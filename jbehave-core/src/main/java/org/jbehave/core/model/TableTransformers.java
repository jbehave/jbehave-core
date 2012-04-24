package org.jbehave.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * Facade responsible for transforming table string representations. It allows
 * the registration of several {@link TableTransformer} instances by name.
 * </p>
 * </p>
 */
public class TableTransformers {

    private final Map<String,TableTransformer> transformers = new HashMap<String, TableTransformer>();
    
    public String transform(String transformerName, String tableAsString, Properties properties) {
        TableTransformer transformer = transformers.get(transformerName);
        if ( transformer != null ){
            return transformer.transform(tableAsString, properties);
        }
        return tableAsString;
    }

    public void useTransformer(String name, TableTransformer transformer) {
        transformers.put(name, transformer);
    }

    public interface TableTransformer {
        String transform(String tableAsString, Properties properties);
    }

    
}
