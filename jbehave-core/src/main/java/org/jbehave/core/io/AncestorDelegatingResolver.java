package org.jbehave.core.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class AncestorDelegatingResolver implements StoryNameResolver {

	private static final String SEPARATOR = "/";
	private static final String SPACE = " ";
	
    private final int ancestors;
    private final StoryNameResolver delegate; 

    public AncestorDelegatingResolver() {
    	this(1);
	}

    public AncestorDelegatingResolver(int ancestors) {
    	this(ancestors, new UnderscoredToCapitalized());
	}

    public AncestorDelegatingResolver(int ancestors, StoryNameResolver delegate) {
		this.ancestors = ancestors;
		this.delegate = delegate;
	}

	@Override
    public String resolveName(String path) {
		List<String> reversed = Arrays.asList(path.split(SEPARATOR));
		Collections.reverse(reversed);
		List<String> names = new ArrayList<>();
		for (int i = 0; i < ancestors + 1; i++) {
			names.add(0, delegate.resolveName(reversed.get(i)));
		}
		return StringUtils.join(names,SPACE);
	}
}
