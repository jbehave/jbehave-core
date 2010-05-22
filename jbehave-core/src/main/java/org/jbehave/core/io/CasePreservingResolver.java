package org.jbehave.core.io;

import org.jbehave.core.RunnableStory;


/**
 * <p>
 * Resolves story paths while preserving the Java story class case, e.g.:
 * "org.jbehave.core.ICanLogin.java" -> "org/jbehave/core/ICanLogin".
 * </p>
 * <p>
 * By default, no extension is used but this can be configured via the
 * constructor so that we can resolve story class to:
 * "org/jbehave/core/ICanLogin.story".
 * </p>
 */
public class CasePreservingResolver extends AbstractStoryPathResolver {

    public CasePreservingResolver() {
        super();
    }

    public CasePreservingResolver(String extension) {
    	super(extension);
    }

	@Override
	protected String resolveName(Class<? extends RunnableStory> storyClass) {
		return storyClass.getSimpleName();
	}

}
