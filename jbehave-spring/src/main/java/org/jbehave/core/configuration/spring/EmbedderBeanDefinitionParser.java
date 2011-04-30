package org.jbehave.core.configuration.spring;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderClassLoader;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.RethrowingFailure;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.LoadFromRelativeFile;
import org.jbehave.core.io.LoadFromURL;
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
	private static final String URL_LOADER_TAG = "urlLoader";
	private static final String RELATIVE_PATH_LOADER = "relativePathLoader";
	private static final String[] STORY_LOADER_CHOICES = new String[] {
			CLASSPATH_LOADER_TAG, URL_LOADER_TAG, RELATIVE_PATH_LOADER };

	private static final String OUTPUT_TAG = "output";
	private static final String FORMATS_SPRING_STORY_REPORTS_SETTER = "formats";
	private static final String WITH_DEFAULT_FORMATS_SPRING_STORY_REPORTER_METHOD = "withDefaultFormats";
	private static final String SKIP_SCENARIOS_AFTER_FAILURE_SPRING_STORY_CONTROLS_SETTER = "skipScenariosAfterFailure";
	private static final String DRY_RUN_SPRING_STORY_CONTROLS_SETTER = "dryRun";
	private static final String PREFIX_ATTRIBUTE = "prefix";
	private static final String ON_FAILURE_ATTRIBUTE = "onfailure";

	private enum FailureStrategyEnum {

		FAILINGUPON(FailingUponPendingStep.class), PASSINGUPON(
				PassingUponPendingStep.class), RETHROWING(
				RethrowingFailure.class), SILENTLYABSORBING(
				SilentlyAbsorbingFailure.class);

		private Class<? extends FailureStrategy> failureStrategyClass;

		private FailureStrategyEnum(
				Class<? extends FailureStrategy> failureStrategyClass) {
			this.failureStrategyClass = failureStrategyClass;
		}
	}

	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext parserContext) {
		return parseEmbedderElement(element, parserContext);
	}

	/**
	 * Parse Embedder namespace.
	 * 
	 * @param element
	 *            root element.
	 * @param parserContext
	 *            Parser Context Object.
	 * @return Embedder bean definition.
	 */
	private AbstractBeanDefinition parseEmbedderElement(Element element,
			ParserContext parserContext) {

		registerFaliureStrategy(element, parserContext);
		registerRegexPrefixCapturingPatternParserBean(element, parserContext);
		registerSpringStoryControlsBean(parserContext);
		registerSpringStoryReporterBuilderBean(
				getElementsContentFromNode(element, OUTPUT_TAG), parserContext);
		registerStoryLoader(element, parserContext);

		return registerEmbedderBean(element, parserContext);

	}

	/**
	 * Registers story loader.
	 * 
	 * @param root
	 *            element of jbehave namespace.
	 * @param parserContext
	 *            Parser Context Object.
	 * @return Created Story Loader bean or null if no story loader is
	 *         configured.
	 */
	private AbstractBeanDefinition registerStoryLoader(Element root,
			ParserContext parserContext) {

		@SuppressWarnings("unchecked")
		List<Element> storyLoader = DomUtils.getChildElementsByTagName(root,
				STORY_LOADER_CHOICES);

		AbstractBeanDefinition beanDefinition = null;
		if (storyLoader.size() > 0) {
			String tagName = getFirstTagName(storyLoader);
			beanDefinition = registerStoryLoaderByTagName(tagName, root,
					parserContext);
		}

		return beanDefinition;
	}

	/**
	 * Method that registers the story loader tag name. If tag name is
	 * CLASSPATH_LOADER_TAG LoadFromClasspath is registered. If tag name is
	 * URL_LOADER_TAG LoadFromURL is registered. If tag name is
	 * RELATIVE_PATH_LOADER LoadFromRelativePath is registered.
	 * 
	 * Otherwise no bean is registered.
	 * 
	 * @param tagName
	 *            tag name of required Story Loader.
	 * @param root
	 *            element of jbehave namespace.
	 * @param parserContext
	 *            Parser Context Object.
	 * @return Created Story Loader bean definition or null if not supported
	 *         story loader is configured.
	 */
	private AbstractBeanDefinition registerStoryLoaderByTagName(String tagName,
			Element root, ParserContext parserContext) {

		AbstractBeanDefinition beanDefinition = null;
		if (CLASSPATH_LOADER_TAG.equalsIgnoreCase(tagName)) {
			beanDefinition = registerLoadFromClasspathBean(
					getElementsContentFromNode(root, CLASSPATH_LOADER_TAG),
					parserContext);
		} else {
			if (URL_LOADER_TAG.equalsIgnoreCase(tagName)) {
				beanDefinition = registerLoadFromURLBean(parserContext);
			} else {
				if (RELATIVE_PATH_LOADER.equalsIgnoreCase(tagName)) {
					beanDefinition = registerLoadFromRelativePath(
							getElementContentFromNode(root,
									RELATIVE_PATH_LOADER), parserContext);
				}
			}
		}

		return beanDefinition;
	}

	/**
	 * Gets first tag name of given element list.
	 * 
	 * @param elements
	 *            that first tag name is returned.
	 * @return Tag name of first element.
	 */
	private String getFirstTagName(List<Element> elements) {
		Element element = elements.get(0);
		String tagName = element.getLocalName();
		return tagName;
	}

	/**
	 * Gets content of child element with given tag from element.
	 * 
	 * @param node
	 *            "parent" root.
	 * @param tagName
	 *            of required children.
	 * @return element content with given tag name.
	 */
	private String getElementContentFromNode(Element node, String tagName) {
		return DomUtils.getChildElementValueByTagName(node, tagName);
	}

	/**
	 * Gets content of child elements with given tag from element.
	 * 
	 * @param node
	 *            "parent" root.
	 * @param tagName
	 *            tag name of required children.
	 * @return List of content of child elements with given tag name.
	 */
	private List<String> getElementsContentFromNode(Element node, String tagName) {
		@SuppressWarnings("unchecked")
		List<Element> elements = DomUtils.getChildElementsByTagName(node,
				tagName);
		return getStringValuesFromElementsList(elements);
	}

	/**
	 * Transforms Elements list to a list of node values.
	 * 
	 * @param elements
	 *            elements where getNodeValue method will be executed.
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
	 * Registers Embedder Bean.
	 * 
	 * @param root
	 *            spring namespace element.
	 * @param parserContext
	 *            Parser Context Object.
	 * @return created Bean Definition Object.
	 */
	private AbstractBeanDefinition registerEmbedderBean(Element root,
			ParserContext parserContext) {
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(Embedder.class);
		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder
				.getBeanDefinition();
		registerBeanDefinition(parserContext, beanDefinition,
				getAttributeValue(root, ID_ATTRIBUTE));
		return beanDefinition;
	}

	/**
	 * Registers Load From URL Bean.
	 * 
	 * @param parserContext
	 *            Parser Context Object.
	 * @return created Bean Definition Object.
	 */
	private AbstractBeanDefinition registerLoadFromURLBean(
			ParserContext parserContext) {
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(LoadFromURL.class);
		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder
				.getBeanDefinition();
		registerBeanDefinition(parserContext, beanDefinition,
				LoadFromURL.class.getName());

		return beanDefinition;
	}

	/**
	 * Registers Load From Relative Bean.
	 * 
	 * @param location
	 *            relative path.
	 * @param parserContext
	 *            Parser Context Object.
	 * @return LoadFromRelativePath bean definition.
	 */
	private AbstractBeanDefinition registerLoadFromRelativePath(
			String location, ParserContext parserContext) {

		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(LoadFromRelativeFile.class);
		beanDefinitionBuilder.addConstructorArgValue(location);
		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder
				.getBeanDefinition();
		registerBeanDefinition(parserContext, beanDefinition,
				LoadFromRelativeFile.class.getName());

		return beanDefinition;
	}

	/**
	 * Registers Load From Classpath Bean.
	 * 
	 * @param classpathElements
	 *            classpath elements required by embedder.
	 * @param parserContext
	 *            Parser Context Object.
	 * @return created Bean Definition Object.
	 */
	private AbstractBeanDefinition registerLoadFromClasspathBean(
			List<String> classpathElements, ParserContext parserContext) {

		EmbedderClassLoader embedderClassLoader = new EmbedderClassLoader(
				classpathElements);
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(LoadFromClasspath.class);
		beanDefinitionBuilder.addConstructorArgValue(embedderClassLoader);
		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder
				.getBeanDefinition();
		registerBeanDefinition(parserContext, beanDefinition,
				LoadFromClasspath.class.getName());

		return beanDefinition;
	}

	/**
	 * Registers Spring Story Reporter Builder class into context.
	 * 
	 * @param outputFormatList
	 *            List of output formats.
	 * @param parserContext
	 *            Parser Context object.
	 * @return created Bean Definition Object.
	 */
	private AbstractBeanDefinition registerSpringStoryReporterBuilderBean(
			List<String> outputFormatList, ParserContext parserContext) {
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(SpringStoryReporterBuilder.class);
		beanDefinitionBuilder
				.setInitMethodName(WITH_DEFAULT_FORMATS_SPRING_STORY_REPORTER_METHOD);
		beanDefinitionBuilder.addPropertyValue(
				FORMATS_SPRING_STORY_REPORTS_SETTER, outputFormatList);
		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder
				.getBeanDefinition();
		registerBeanDefinition(parserContext, beanDefinition,
				SpringStoryReporterBuilder.class.getName());

		return beanDefinition;
	}

	/**
	 * Registers SpringStoryControls class into context.
	 * 
	 * @param parserContext
	 *            Parser Context Object.
	 * @return created Bean Definition Object.
	 */
	private AbstractBeanDefinition registerSpringStoryControlsBean(
			ParserContext parserContext) {
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(SpringStoryControls.class);
		beanDefinitionBuilder.addPropertyValue(
				DRY_RUN_SPRING_STORY_CONTROLS_SETTER, Boolean.FALSE);
		beanDefinitionBuilder.addPropertyValue(
				SKIP_SCENARIOS_AFTER_FAILURE_SPRING_STORY_CONTROLS_SETTER,
				Boolean.FALSE);
		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder
				.getBeanDefinition();
		registerBeanDefinition(parserContext, beanDefinition,
				SpringStoryControls.class.getName());

		return beanDefinition;
	}

	/**
	 * Register RegexPrefixCapturingPatternParser class into context.
	 * 
	 * @param element
	 *            root jbehave namespace element.
	 * @param parserContext
	 *            Parser Context Object.
	 * @return created Bean Definition Object.
	 */
	private AbstractBeanDefinition registerRegexPrefixCapturingPatternParserBean(
			Element element, ParserContext parserContext) {
		String prefix = getAttributeValue(element, PREFIX_ATTRIBUTE);
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(RegexPrefixCapturingPatternParser.class);
		beanDefinitionBuilder.addConstructorArgValue(prefix);
		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder
				.getBeanDefinition();
		registerBeanDefinition(parserContext, beanDefinition,
				RegexPrefixCapturingPatternParser.class.getName());

		return beanDefinition;

	}

	/**
	 * Registers failure strategy.
	 * @param root jbehave namespace element.
	 * @param parserContext Parser Context Object.
	 * @return created failure strategy pattern or null.
	 */
	private AbstractBeanDefinition registerFaliureStrategy(Element root,
			ParserContext parserContext) {

		String configuredFailureStrategy = getAttributeValue(root,
				ON_FAILURE_ATTRIBUTE);
		Class<? extends FailureStrategy> failureStrategyClass = getFailureStrategyClassFromOnFailureAttribute(configuredFailureStrategy);
		
		AbstractBeanDefinition beanDefinition = null;
		if (failureStrategyClass != null) {
			BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
					.rootBeanDefinition(failureStrategyClass);
			beanDefinition = beanDefinitionBuilder.getBeanDefinition();
			registerBeanDefinition(parserContext, beanDefinition,
					failureStrategyClass.getName());
		}
		return beanDefinition;
	}

	/**
	 * Gets failure strategy class from onfailure attribute value.
	 * @param onFailureValue attribute value.
	 * @return Class that represents onfailure value, or null if attribute is not valid.
	 */
	private Class<? extends FailureStrategy> getFailureStrategyClassFromOnFailureAttribute(
			String onFailureValue) {
		Class<? extends FailureStrategy> failureStrategyClass = null;

		if (onFailureValue != null && !"".equals(onFailureValue)) {
			FailureStrategyEnum failureStrategyEnum = FailureStrategyEnum
					.valueOf(FailureStrategyEnum.class, onFailureValue);
			failureStrategyClass = failureStrategyEnum.failureStrategyClass;
		}

		return failureStrategyClass;
	}

	/**
	 * Gets attribute value for given element.
	 * 
	 * @param element
	 *            element containing attribute.
	 * @param attributeName
	 *            attribute name.
	 * @return attribute value.
	 */
	private String getAttributeValue(Element element, String attributeName) {
		return element.getAttribute(attributeName);
	}

	/**
	 * Register a new bean into Spring context.
	 * 
	 * @param parserContext
	 *            parser Context Object.
	 * @param beanDefinition
	 *            bean Definition to register.
	 * @param springBeanId
	 *            name that will be used as Spring Bean Id field.
	 */
	private void registerBeanDefinition(ParserContext parserContext,
			BeanDefinition beanDefinition, String springBeanId) {
		BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(
				beanDefinition, springBeanId);
		BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder,
				parserContext.getRegistry());
		parserContext.getReaderContext().fireComponentRegistered(
				new BeanComponentDefinition(beanDefinitionHolder));
	}

}
