package org.jbehave.examples.core.needle;

import static org.needle4j.common.Annotations.assertIsQualifier;

import java.lang.annotation.Annotation;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.needle.UsingNeedle;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.needle.NeedleAnnotatedEmbedderRunner;
import org.jbehave.examples.core.needle.ParentAnnotatedEmbedderUsingNeedle.TradingServiceInjectionProvider;
import org.jbehave.examples.core.service.TradingService;
import org.junit.runner.RunWith;
import org.needle4j.injection.InjectionProvider;
import org.needle4j.injection.InjectionTargetInformation;

@RunWith(NeedleAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true,
        ignoreFailureInView = true)
@UsingNeedle(provider = TradingServiceInjectionProvider.class)
public abstract class ParentAnnotatedEmbedderUsingNeedle extends InjectableEmbedder {

    /**
     * Inline provider
     *
     * @author Simon Zambrovski
     */
    public static class TradingServiceInjectionProvider implements InjectionProvider<TradingService> {

        protected final TradingService instance = new TradingService();

        public TradingServiceInjectionProvider() {
        }

        @Override
        public Object getKey(final InjectionTargetInformation injectionTargetInformation) {
            return injectionTargetInformation.getType();
        }

        @Override
        public boolean verify(final InjectionTargetInformation injectionTargetInformation) {
            return isTargetAssignable(injectionTargetInformation);
        }

        @Override
        public TradingService getInjectedObject(Class<?> injectionPointType) {
            return instance;
        }

        /**
         * <code>true</code> when injection target is or extends/implements instance
         * type
         *
         * @param injectionTargetInformation
         * @return true when type is assignable from instance
         */
        protected boolean isTargetAssignable(final InjectionTargetInformation injectionTargetInformation) {
            return injectionTargetInformation.getType().isAssignableFrom(instance.getClass());
        }

        protected boolean isTargetQualifierPresent(final InjectionTargetInformation injectionTargetInformation,
                final Class<? extends Annotation> qualifier) {
            assertIsQualifier(qualifier);
            return injectionTargetInformation.isAnnotationPresent(qualifier);
        }

    }

}
