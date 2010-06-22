package org.jbehave.core.configuration.spring;

import java.util.List;

import org.jbehave.core.annotations.AddSteps;
import org.jbehave.core.annotations.spring.AddStepsWithSpring;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationFinder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.PrintStreamAnnotationMonitor;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;

public class SpringAnnotationBuilder extends AnnotationBuilder {

    private final AnnotationMonitor annotationMonitor;

    public SpringAnnotationBuilder() {
        this(new PrintStreamAnnotationMonitor());
    }

    public SpringAnnotationBuilder(AnnotationMonitor annotationMonitor) {
        this.annotationMonitor = annotationMonitor;
    }

    /**
     * Builds CandidateSteps using annotation {@link AddSteps} found in the
     * annotated object instance
     * 
     * @param annotatedInstance
     *            the Object instance that contains the annotations
     * @return A List of CandidateSteps instances
     */
    public List<CandidateSteps> buildCandidateSteps(Object annotatedInstance) {
        AnnotationFinder finder = new AnnotationFinder(annotatedInstance.getClass());
        Configuration configuration = buildConfiguration(annotatedInstance);
        InjectableStepsFactory factory = new InstanceStepsFactory(configuration);
        if (finder.isAnnotationPresent(AddStepsWithSpring.class)) {
            if ( finder.isAnnotationValuePresent(AddStepsWithSpring.class, "locations") ){
                List<String> locations = finder.getAnnotatedValues(AddStepsWithSpring.class, String.class, "locations");
                factory = new SpringStepsFactory(configuration, locations.toArray(new String[locations.size()]));
            }
        } else {
            annotationMonitor.annotationNotFound(AddStepsWithSpring.class, annotatedInstance);
        }
        return factory.createCandidateSteps();
    }

}
