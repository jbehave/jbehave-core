package org.jbehave.examples.core.steps;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Parameter;

public class TableMappingSteps {

    @Given("the parameters mapped via annotations to custom types: $table")
    public void givenTheMyParametersList(List<MyParameters> list) {
        System.out.println("List annotated: "+list);
    }

    @Given("the parameters mapped via annotations to custom type: $table")
    public void givenTheMyParametersType(MyParameters single) {
        System.out.println("Single annotated: "+single);
    }

    @Given("the parameters mapped via names to custom types: $table")
    public void givenTheNamedParametersList(List<MyParameters> list) {
        System.out.println("List named: "+list);
    }

    @Given("the parameters mapped via names to custom type: $table")
    public void givenTheNamedParametersType(MyParameters single) {
        System.out.println("Single named: "+single);
    }

    @AsParameters
    public static class MyParameters {
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
