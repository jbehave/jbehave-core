package org.jbehave.core.io.rest.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.ResourceUploader;
import org.jbehave.core.io.rest.confluence.IndexFromConfluence;
import org.jbehave.core.io.rest.confluence.LoadFromConfluence;
import org.jbehave.core.io.rest.redmine.IndexFromRedmine;
import org.jbehave.core.io.rest.redmine.LoadFromRedmine;
import org.jbehave.core.io.rest.redmine.UploadToRedmine;
import org.jbehave.core.io.rest.xwiki.IndexFromXWiki;
import org.jbehave.core.io.rest.xwiki.LoadFromXWiki;
import org.jbehave.core.io.rest.xwiki.UploadToXWiki;

/**
 * Abstract mojo for filesystem import/export operations.
 */
public abstract class AbstractFilesystemMojo extends AbstractMojo {

    private static final String REDMINE = "redmine";
    private static final String XWIKI = "xwiki";
    private static final String CONFLUENCE = "confluence";

    /**
     * The REST provider.  Currently supported are "redmine" and "xwiki".  Also supported is "confluence" for import
     * only.
     */
    @Parameter(property = "jbehave.rest.provider", defaultValue = "xwiki")
    String restProvider;

    /**
     * The root URI of the REST API
     */
    @Parameter(property = "jbehave.rest.rootURI", required = true)
    String restRootURI;

    /**
     * The username to access the REST API. May be null if no security enabled.
     */
    @Parameter(property = "jbehave.rest.username")
    String restUsername;

    /**
     * The password to access the REST API. May be null if no security enabled.
     */
    @Parameter(property = "jbehave.rest.password")
    String restPassword;

    /**
     * The path of the filesystem in which the resources are found
     */
    @Parameter(property = "jbehave.rest.resourcesPath", defaultValue = "src/main/resources/stories")
    String resourcesPath;

    /**
     * The extension of the resources
     */
    @Parameter(property = "jbehave.rest.resourcesExt", defaultValue = ".story")
    String resourcesExt;

    ResourceIndexer newResourceIndexer() {
        if (restProvider.equals(REDMINE)) {
            return new IndexFromRedmine(restUsername, restPassword);
        }
        if (restProvider.equals(XWIKI)) {
            return new IndexFromXWiki(restUsername, restPassword);
        }
        if (restProvider.equals(CONFLUENCE)) {
            return new IndexFromConfluence(restUsername, restPassword);
        }
        throw new RuntimeException("Unsupported ResourceIndexer provider " + restProvider);
    }

    ResourceLoader newResourceLoader() {
        if (restProvider.equals(REDMINE)) {
            return new LoadFromRedmine(Type.JSON, restUsername, restPassword);
        }
        if (restProvider.equals(XWIKI)) {
            return new LoadFromXWiki(Type.JSON, restUsername, restPassword);
        }
        if (restProvider.equals(CONFLUENCE)) {
            return new LoadFromConfluence(restUsername, restPassword);
        }

        throw new RuntimeException("Unsupported ResourceLoader provider " + restProvider);
    }

    ResourceUploader newResourceUploader() {
        if (restProvider.equals(REDMINE)) {
            return new UploadToRedmine(Type.JSON, restUsername, restPassword);
        }
        if (restProvider.equals(XWIKI)) {
            return new UploadToXWiki(Type.XML, restUsername, restPassword);
        }

        throw new RuntimeException("Unsupported ResourceUploader provider " + restProvider);
    }

}
