package org.jbehave.core.configuration.guice;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.annotations.UsingControls;
import org.jbehave.core.annotations.guice.UsingGuice;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationFinder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MissingAnnotationException;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.guice.GuiceStepsFactory;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;

public class GuiceAnnotationBuilder extends AnnotationBuilder {

    private Injector injector;

    public GuiceAnnotationBuilder(Class<?> annotatedClass) {
        super(annotatedClass);
    }

    public GuiceAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        super(annotatedClass, annotationMonitor);
    }

    @SuppressWarnings("rawtypes")
    public Configuration buildConfiguration() throws MissingAnnotationException {

        AnnotationFinder finder = getFinder();
        if (finder.isAnnotationPresent(UsingGuice.class)) {
            List<Class> moduleClasses = finder.getAnnotatedValues(UsingGuice.class, Class.class, "modules");
            List<Module> modules = new ArrayList<Module>();
            for (Class<Module> moduleClass : moduleClasses) {
                try {
                    modules.add(moduleClass.newInstance());
                } catch (Exception e) {
                    getAnnotationMonitor().elementCreationFailed(moduleClass, e);
                }
            }
            // creating injector with any modules found
            if ( modules.size() > 0 ){
                injector = Guice.createInjector(Modules.combine(modules));
            }
        } else {
            getAnnotationMonitor().annotationNotFound(UsingGuice.class, getAnnotatedClass());
        }
        return super.buildConfiguration();
    }

    @Override
    public List<CandidateSteps> buildCandidateSteps() {
        Configuration configuration = buildConfiguration();
        InjectableStepsFactory factory = new InstanceStepsFactory(configuration);
        if ( injector != null ){
            factory = new GuiceStepsFactory(configuration, injector);            
        }
        return factory.createCandidateSteps();
    }
    
    @Override
    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder) {
        if ( injector != null ){
            List<Binding<ParameterConverter>> bindingsByType = injector.findBindingsByType(new TypeLiteral<ParameterConverter>(){});
            List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
            for ( Binding<ParameterConverter> binding : bindingsByType ) {
                converters.add(binding.getProvider().get());
            }
            return new ParameterConverters().addConverters(converters);
        }
        return super.parameterConverters(annotationFinder);
    }

    @Override
    protected <T> T instanceOf(Class<T> type, Class<T> ofClass) {
        if ( injector != null ){
            try {
                return injector.getInstance(type);
            } catch ( RuntimeException e ){
                // fall back on default
                //getAnnotationMonitor().elementCreationFailed(type, e);                
            }
        }
        return super.instanceOf(type, ofClass);
    }

    public Object instanceWithInjectedEmbedder() {

        // Build Configuration and CandidateSteps
        Configuration configuration = buildConfiguration();
        List<CandidateSteps> candidateSteps = buildCandidateSteps();

        // Create the Embedder class
        Embedder embedder = injector.getInstance(Embedder.class);
        // TODO **** REMOVE when javax.inject is supported by Guice 2.1
        embedder.useConfiguration(configuration);
        embedder.useCandidateSteps(candidateSteps);
        // TODO ****
        AnnotationFinder finder = getFinder();
        if (finder.isAnnotationPresent(UsingControls.class)) {

            boolean batch = control(finder, "batch");
            boolean skip = control(finder, "skip");
            boolean generateViewAfterStories = control(finder, "generateViewAfterStories");
            boolean ignoreFailureInStories = control(finder, "ignoreFailureInStories");
            boolean ignoreFailureInView = control(finder, "ignoreFailureInView");

            embedder.embedderControls().doBatch(batch).doSkip(skip)
                    .doGenerateViewAfterStories(generateViewAfterStories)
                    .doIgnoreFailureInStories(ignoreFailureInStories)
                    .doIgnoreFailureInView(ignoreFailureInView);
        }
        return injector.getInstance(annotatedClass());
    }

    private boolean control(AnnotationFinder finder, String name) {
        return finder.getAnnotatedValue(UsingControls.class, Boolean.class, name);
    }


}
