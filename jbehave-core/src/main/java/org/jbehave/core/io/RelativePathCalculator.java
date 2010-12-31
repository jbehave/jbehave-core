package org.jbehave.core.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link PathCalculator} that finds given stories relative to the current story.
 */
public class RelativePathCalculator implements PathCalculator {

    @Override
    public String calculate(String root, String path) {
        return join(calculatePath(toList(root), toList(path)));
    }

    private List<String> toList(String path) {
        if (path.length() == 0) {
            return new LinkedList<String>();
        }

        return new LinkedList<String>(Arrays.asList(path.replace('\\', '/').split("/")));
    }

    private Iterable<String> calculatePath(List<String> root, List<String> path) {
        if (path.get(0).length() == 0) {
            return path.subList(1, path.size());
        }

        ArrayList<String> list = new ArrayList<String>();
        if (root.size() > 0) {
            list.addAll(root.subList(0, root.size() - 1));
        }
        list.addAll(path);

        return list;
    }

    private String join(Iterable<String> list) {
        StringBuilder b = new StringBuilder();

        for (String each : list) {
            b.append("/").append(each);
        }

        return b.substring(1);
    }
}
