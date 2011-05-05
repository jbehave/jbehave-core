package org.jbehave.core.io;

import org.jbehave.core.Embeddable;

/**
 * <p>
 * Resolves story paths while preserving the Java class case, e.g.:
 * "org.jbehave.core.ICanLogin.java" -> "org/jbehave/core/ICanLogin.story".
 * </p>
 * <p>
 * By default, the {@link AbstractStoryPathResolver#DEFAULT_EXTENSION} is used
 * but this can be configured via the constructor so that we can resolve class
 * to use another or no extension at all, e.g.: "org/jbehave/core/ICanLogin".
 * </p>
 */
public class CasePreservingResolver extends AbstractStoryPathResolver {

    public CasePreservingResolver() {
        this(DEFAULT_EXTENSION);
    }

    public CasePreservingResolver(String extension) {
        super(extension);
    }

    @Override
    protected String resolveName(Class<? extends Embeddable> embeddableClass) {
        return embeddableClass.getSimpleName();
    }

}
