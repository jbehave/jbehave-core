package org.jbehave.examples.core.steps;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Parameter;

public class ExamplesTableParametersSteps {

    @Given("the parameters mapped via annotations to custom types: %table")
    public void givenTheAnnotatedParametersList(List<AnnotatedParameters> list) {
        System.out.println("List annotated: "+list);
    }

    @Given("the parameters mapped via annotations to custom type: %table")
    public void givenTheAnnotatedParametersType(AnnotatedParameters type) {
        System.out.println("Single annotated: "+type);
    }

    @Given("the parameters mapped via names to custom types: %table")
    public void givenTheNamedParametersList(List<AnnotatedParameters> list) {
        System.out.println("List named: "+list);
    }

    @Given("the parameters mapped via names to custom type: %table")
    public void givenTheNamedParametersType(AnnotatedParameters type) {
        System.out.println("Single named: "+type);
    }

    @AsParameters
    public static class AnnotatedParameters {
        @Parameter(name = "aString")
        private String string;
        @Parameter(name = "anInteger")
        private Integer integer;
        @Parameter(name = "aBigDecimal")
        private BigDecimal bigDecimal;
        
        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

}
