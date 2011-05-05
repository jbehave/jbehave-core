package org.jbehave.core.configuration.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandler extends NamespaceHandlerSupport {

	private static final String EMBEDDER_TAG = "embedder";

	public void init() {
		registerBeanDefinitionParser(EMBEDDER_TAG, new EmbedderBeanDefinitionParser());
	}

}
