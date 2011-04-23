package org.jbehave.core.configuration.spring;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.embedder.EmbedderClassLoader;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class EmbedderBeanDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String CLASSPATH_LOADER_TAG = "classpathLoader";
    private static final String OUTPUT_TAG = "output";
    private static final String FORMATS_SPRING_STORY_REPORTS_SETTER = "formats";
    private static final String WITH_DEFAULT_FORMATS_SPRING_STORY_REPORTER_METHOD = "withDefaultFormats";
    private static final String SKIP_SCENARIOS_AFTER_FAILURE_SPRING_STORY_CONTROLS_SETTER = "skipScenariosAfterFailure";
    private static final String DRY_RUN_SPRING_STORY_CONTROLS_SETTER = "dryRun";
    private static final String PREFIX_ATTRIBUTE = "prefix";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        return parseEmbedderElement(element, parserContext);
    }

    /**
     * Parse Embedder namespace.
     * 
     * @param element root element.
     * @param parserContext Parser Context Object.
     * @return LoadFromClasspath Bean definition.
     */
    private AbstractBeanDefinition parseEmbedderElement(Element element, ParserContext parserContext) {

        registerRegexPrefixCapturingPatternParserBean(element, parserContext);
        registerSpringStoryControlsBean(parserContext);
        registerSpringStoryReporterBuilderBean(getElementsFromNode(element, OUTPUT_TAG), parserContext);

        return registerLoadFromClasspathBean(element, getElementsFromNode(element, CLASSPATH_LOADER_TAG), parserContext);

    }

    /**
     * Gets child elements with given tag from element.
     * 
     * @param node "parent" root.
     * @param tagName tag name of required children.
     * @return List of child elements with given tag name.
     */
    private List<String> getElementsFromNode(Element node, String tagName) {
        @SuppressWarnings("unchecked")
        List<Element> elements = DomUtils.getChildElementsByTagName(node, tagName);
        return getStringValuesFromElementsList(elements);
    }

    /**
     * Transforms Elements list to a list of node values.
     * 
     * @param elements elements where getNodeValue method will be executed.
     * @return list of texts containing each node value.
     */
    private List<String> getStringValuesFromElementsList(List<Element> elements) {
        List<String> elementsText = new ArrayList<String>();
        for (Element element : elements) {
            elementsText.add(DomUtils.getTextValue(element));
        }
        return elementsText;
    }

    /**
     * Registers Load From Classpath Bean.
     * 
     * @param root spring embedder id.
     * @param classpathElements classpath elements required by embedder.
     * @param parserContext Parser Context Object.
     * @return created Bean Definition Object.
     */
    private AbstractBeanDefinition registerLoadFromClasspathBean(Element root, List<String> classpathElements,
            ParserContext parserContext) {

        EmbedderClassLoader embedderClassLoader = new EmbedderClassLoader(classpathElements);
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(LoadFromClasspath.class);
        beanDefinitionBuilder.addConstructorArgValue(embedderClassLoader);
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        registerBeanDefinition(parserContext, beanDefinition, getAttributeValue(root, ID_ATTRIBUTE));

        return beanDefinition;
    }

    /**
     * Registers Spring Story Reporter Builder class into context.
     * 
     * @param outputFormatList List of output formats.
     * @param parserContext Parser Context object.
     * @return created Bean Definition Object.
     */
    private AbstractBeanDefinition registerSpringStoryReporterBuilderBean(List<String> outputFormatList,
            ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .rootBeanDefinition(SpringStoryReporterBuilder.class);
        beanDefinitionBuilder.setInitMethodName(WITH_DEFAULT_FORMATS_SPRING_STORY_REPORTER_METHOD);
        beanDefinitionBuilder.addPropertyValue(FORMATS_SPRING_STORY_REPORTS_SETTER, outputFormatList);
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        registerBeanDefinition(parserContext, beanDefinition, SpringStoryReporterBuilder.class.getName());

        return beanDefinition;
    }

    /**
     * Registers SpringStoryControls class into context.
     * 
     * @param parserContext Parser Context Object.
     * @return created Bean Definition Object.
     */
    private AbstractBeanDefinition registerSpringStoryControlsBean(ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .rootBeanDefinition(SpringStoryControls.class);
        beanDefinitionBuilder.addPropertyValue(DRY_RUN_SPRING_STORY_CONTROLS_SETTER, Boolean.FALSE);
        beanDefinitionBuilder
                .addPropertyValue(SKIP_SCENARIOS_AFTER_FAILURE_SPRING_STORY_CONTROLS_SETTER, Boolean.FALSE);
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        registerBeanDefinition(parserContext, beanDefinition, SpringStoryControls.class.getName());

        return beanDefinition;
    }

    /**
     * Register RegexPrefixCapturingPatternParser class into context.
     * 
     * @param element Root jbehave namespace element.
     * @param parserContext Parser Context Object.
     * @return created Bean Definition Object.
     */
    private AbstractBeanDefinition registerRegexPrefixCapturingPatternParserBean(Element element,
            ParserContext parserContext) {
        String prefix = getAttributeValue(element, PREFIX_ATTRIBUTE);
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .rootBeanDefinition(RegexPrefixCapturingPatternParser.class);
        beanDefinitionBuilder.addConstructorArgValue(prefix);
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        registerBeanDefinition(parserContext, beanDefinition, RegexPrefixCapturingPatternParser.class.getName());

        return beanDefinition;

    }

    /**
     * Gets attribute value for given element.
     * 
     * @param element element containing attribute.
     * @param attributeName attribute name.
     * @return attribute value.
     */
    private String getAttributeValue(Element element, String attributeName) {
        return element.getAttribute(attributeName);
    }

    /**
     * Register a new bean into Spring context.
     * 
     * @param parserContext parser Context Object.
     * @param beanDefinition bean Definition to register.
     * @param springBeanId name that will be used as Spring Bean Id field.
     */
    private void registerBeanDefinition(ParserContext parserContext, BeanDefinition beanDefinition,
            String springBeanId) {
        BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, springBeanId);
        BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, parserContext.getRegistry());
        parserContext.getReaderContext().fireComponentRegistered(new BeanComponentDefinition(beanDefinitionHolder));
    }

}
