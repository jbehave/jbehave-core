package org.jbehave.core.io;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;

/**
 * Transforms a list of java source paths to corresponding class names
 */
public class PathToClassNames {

    private static final String JAVA = ".java";

    public List<String> transform(List<String> paths) {
        List<String> trasformed = new ArrayList<String>(paths);
        CollectionUtils.transform(trasformed, new Transformer() {
            public Object transform(Object input) {
                String path = (String) input;
                if (!StringUtils.endsWithIgnoreCase(path, JAVA)) {
                    return input;
                }
                return StringUtils.removeEndIgnoreCase(path, JAVA).replace('/', '.');
            }
        });
        return trasformed;
    }

}
