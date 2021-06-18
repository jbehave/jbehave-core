package org.jbehave.core.steps;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.FromContext;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.ToContext;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;

public class SomeSteps extends Steps {

    public SomeSteps() {
    }

    public Object args;

    public void aMethod() {
    }

    public void aFailingMethod() {
        throw new RuntimeException();
    }

    @BeforeScenario
    public void aFailingBeforeScenarioMethod() {
        throw new RuntimeException();
    }

    public void aPendingMethod() {
        throw new PendingStepFound("a step");
    }

    public void aMethodWith(String args) {
        this.args = args;
    }

    public void aMethodWith(double args) {
        this.args = args;
    }

    public void aMethodWith(long args) {
        this.args = args;
    }

    public void aMethodWith(int args) {
        this.args = args;
    }

    public void aMethodWith(float args) {
        this.args = args;
    }

    public void aMethodWithListOfStrings(List<String> args) {
        this.args = args;
    }

    public void aMethodWithListOfLongs(List<Long> args) {
        this.args = args;
    }

    public void aMethodWithListOfIntegers(List<Integer> args) {
        this.args = args;
    }

    public void aMethodWithListOfDoubles(List<Double> args) {
        this.args = args;
    }

    public void aMethodWithListOfFloats(List<Float> args) {
        this.args = args;
    }

    public void aMethodWithDate(Date args) {
        this.args = args;
    }

    public void aMethodWithExamplesTable(ExamplesTable args) {
        this.args = args;
    }

    public ExamplesTable aMethodReturningExamplesTable(String value) {
        return new ExamplesTable(value);
    }

    @AsParameters
    public static class MyParameters {
        String col1;
        String col2;
    }

    public ExamplesTable aFailingMethodReturningExamplesTable(String value) {
        throw new RuntimeException(value);
    }
    
    public void aMethodWithANamedParameter(@Named("theme") String theme, @Named("variant") String variant) {
        Map<String, Object> namedArgs = new HashMap<>();
        namedArgs.put("theme", theme);
        namedArgs.put("variant", variant);
        this.args = namedArgs;
    }

    public void aMethodWithoutNamedAnnotation(String theme) {
        this.args = theme;
    }

    public void aMultipleParamMethodWithoutNamedAnnotation(String theme, String variant) {
        HashMap<String, Object> multipleArgs = new HashMap<>();
        multipleArgs.put("theme", theme);
        multipleArgs.put("variant", variant);
        this.args = multipleArgs;
    }

    public void aMethodThatExpectsUUIDExceptionWrapper(UUIDExceptionWrapper exception) {
        this.args = exception;
    }

    @ToContext("someKey")
    public String aMethodStoringAString() {
        return "someValue";
    }

    @ToContext(value = "someKey", retentionLevel = ToContext.RetentionLevel.SCENARIO)
    public String aMethodStoringAStringInScenario() {
        return "someValue";
    }

    @ToContext(value = "someKey", retentionLevel = ToContext.RetentionLevel.STORY)
    public String aMethodStoringAStringInStory() {
        return "someValue";
    }

    public void aMethodReadingFromContext(@FromContext("someKey") String value) {
        this.args = value;
    }

    public static Method methodFor(String methodName) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(SomeSteps.class);
        for (MethodDescriptor md : beanInfo.getMethodDescriptors()) {
            if (md.getMethod().getName().equals(methodName)) {
                return md.getMethod();
            }
        }
        return null;
    }

}

enum SomeEnum {
    ONE,
    TWO,
    THREE,
    MULTIPLE_WORDS_AND_1_NUMBER,
    ;
}
