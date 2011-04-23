package org.jbehave.core.configuration.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("embedder", new EmbedderBeanDefinitionParser());
	}

}
