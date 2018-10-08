package org.jbehave.core.io;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * {@link PathCalculator} that finds given stories relative to the current story.
 */
public class RelativePathCalculator implements PathCalculator {

    @Override
    public String calculate(String root, String path) {
        return join(calculatePath(split(root), split(path)));
    }

    private List<String> split(String path) {
        if (path.trim().length() == 0) {
            return new LinkedList<>();
        }

        return new LinkedList<>(asList(path.replace('\\', '/').split("/")));
    }

    private Iterable<String> calculatePath(List<String> root, List<String> path) {
        if (path.get(0).length() == 0) {
            return path.subList(1, path.size());
        }

        List<String> list = new ArrayList<>();
        if (root.size() > 0) {
            list.addAll(root.subList(0, root.size() - 1));
        }
        list.addAll(path);

        return list;
    }

    private String join(Iterable<String> list) {
        StringBuilder sb = new StringBuilder();

        for (String each : list) {
            sb.append("/").append(each);
        }

        return sb.substring(1);
    }
}
