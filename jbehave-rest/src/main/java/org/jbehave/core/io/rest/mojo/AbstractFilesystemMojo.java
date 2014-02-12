package org.jbehave.core.io.rest.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.ResourceUploader;
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

    /**
     * The REST provider.  Currently supported are "redmine" and "xwiki"
     * 
     * @parameter default-value="xwiki" expression="${jbehave.rest.provider}
     */
    String restProvider;

    /**
     * The root URI of the REST API
     * 
     * @parameter expression="${jbehave.rest.rootURI}
     * @required
     */
    String restRootURI;

    /**
     * The username to access the REST API. May be null if no security enabled.
     * 
     * @parameter expression="${jbehave.rest.username}
     */
    String restUsername;

    /**
     * The password to access the REST API. May be null if no security enabled.
     * 
     * @parameter expression="${jbehave.rest.password}
     */
    String restPassword;

    /**
	 * The path of the filesystem in which the resources are found
	 * 
	 * @parameter default-value="src/main/resources/stories"
	 *            expression="${jbehave.rest.resourcesPath}
	 */
	String resourcesPath;

	/**
	 * The extension of the resources
	 * 
	 * @parameter default-value=".story" expression="${jbehave.rest.resourcesExt}
	 */
	String resourcesExt;

    ResourceIndexer newResourceIndexer() {
        if (restProvider.equals(REDMINE)) {
            return new IndexFromRedmine(restUsername, restPassword);
        }
        if (restProvider.equals(XWIKI)) {
            return new IndexFromXWiki(restUsername, restPassword);
        }
        throw new RuntimeException("Unsupported REST provider " + restProvider);
    }

    ResourceLoader newResourceLoader() {
        if (restProvider.equals(REDMINE)) {
            return new LoadFromRedmine(Type.JSON, restUsername, restPassword);
        }
        if (restProvider.equals(XWIKI)) {
            return new LoadFromXWiki(Type.JSON, restUsername, restPassword);
        }

        throw new RuntimeException("Unsupported REST provider " + restProvider);
    }

    ResourceUploader newResourceUploader() {
        if (restProvider.equals(REDMINE)) {
            return new UploadToRedmine(Type.JSON, restUsername, restPassword);
        }
        if (restProvider.equals(XWIKI)) {
            return new UploadToXWiki(Type.XML, restUsername, restPassword);
        }

        throw new RuntimeException("Unsupported REST provider " + restProvider);
    }

}
