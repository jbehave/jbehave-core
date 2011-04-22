package org.jbehave.core.configuration.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class JBehaveNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("embedder", new JBehaveBeanDefinitionParser());
	}

}
