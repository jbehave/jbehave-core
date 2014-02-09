package org.jbehave.examples.core.rest.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.Resource;
import org.jbehave.core.io.rest.ResourceExporter;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.ResourceUploader;
import org.jbehave.core.io.rest.filesystem.ExportFromFilesystem;
import org.jbehave.core.io.rest.redmine.IndexFromRedmine;
import org.jbehave.core.io.rest.redmine.LoadFromRedmine;
import org.jbehave.core.io.rest.redmine.UploadToRedmine;
import org.jbehave.core.io.rest.xwiki.IndexFromXWiki;
import org.jbehave.core.io.rest.xwiki.LoadFromXWiki;
import org.jbehave.core.io.rest.xwiki.UploadToXWiki;

public class RESTSteps {

	private String providerName;
	private Map<String, Resource> index;
	private String storyText;

	@Given("REST provider is $name")
	public void givenRESTProvider(String name) {
		this.providerName = name;
	}

	@When("index is retrieved from $uri")
	public void indexIsRetrieved(String uri) {
		ResourceIndexer indexer = resourceIndexer();
		index = indexer.indexResources(uri);
	}

	@Then("the index is not empty")
	public void indexIsNotEmpty() {
		assertThat(index.isEmpty(), is(false));
	}

	@When("story $name is loaded")
	public void storyIsLoaded(String name) {
		ResourceLoader loader = resourceLoader();
		String uri = index.get(name).getURI();
		storyText = loader.loadResourceAsText(uri);
	}

	@When("stories in $sourcePath are exported to $rootURI")
	public void whenStoriesAreExported(String sourcePath, String rootURI) {
		ResourceExporter exporter = new ExportFromFilesystem(resourceIndexer(), resourceupLoader(), sourcePath, ".story", "**/.*story");
		exporter.exportResources(rootURI);
	}

	@Then("story text contains '$text'")
	public void storyContainsText(String text) {
		assertThat(storyText, containsString(text));
	}

	@When("story $name is uploaded appending '$text'")
	public void storyIsUploaded(String name, String text) {
		ResourceUploader uploader = resourceupLoader();
		String uri = index.get(name).getURI();
		uploader.uploadResourceAsText(uri, storyText + " " + text);
	}

	private ResourceIndexer resourceIndexer() {
		if (providerName.equals("Redmine")) {
			return new IndexFromRedmine();
		} else if (providerName.equals("XWiki")) {
			return new IndexFromXWiki();
		}
		throw new RuntimeException("Provider not supported: " + providerName);
	}

	private ResourceLoader resourceLoader() {
		if (providerName.equals("Redmine")) {
			return new LoadFromRedmine(Type.JSON);
		} else if (providerName.equals("XWiki")) {
			return new LoadFromXWiki(Type.JSON);
		}
		throw new RuntimeException("Provider not supported: " + providerName);
	}

	private ResourceUploader resourceupLoader() {
		if (providerName.equals("Redmine")) {
			return new UploadToRedmine(Type.JSON, "jbehave", "jbehave");
		} else if (providerName.equals("XWiki")) {
			return new UploadToXWiki(Type.XML, "jbehave", "jbehave");
		}
		throw new RuntimeException("Provider not supported: " + providerName);
	}

}
