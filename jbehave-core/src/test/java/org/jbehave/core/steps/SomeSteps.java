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

    public void method() {
    }

    public void failingMethod() {
        throw new RuntimeException();
    }

    @BeforeScenario
    public void failingBeforeScenarioMethod() {
        throw new RuntimeException();
    }

    public void pendingMethod() {
        throw new PendingStepFound("a step");
    }

    public void methodWith(String args) {
        this.args = args;
    }

    public void methodWith(double args) {
        this.args = args;
    }

    public void methodWith(long args) {
        this.args = args;
    }

    public void methodWith(int args) {
        this.args = args;
    }

    public void methodWith(float args) {
        this.args = args;
    }

    public void methodWithListOfStrings(List<String> args) {
        this.args = args;
    }

    public void methodWithListOfLongs(List<Long> args) {
        this.args = args;
    }

    public void methodWithListOfIntegers(List<Integer> args) {
        this.args = args;
    }

    public void methodWithListOfDoubles(List<Double> args) {
        this.args = args;
    }

    public void methodWithListOfFloats(List<Float> args) {
        this.args = args;
    }

    public void methodWithDate(Date args) {
        this.args = args;
    }

    public void methodWithExamplesTable(ExamplesTable args) {
        this.args = args;
    }

    public ExamplesTable methodReturningExamplesTable(String value) {
        return new ExamplesTable(value);
    }

    @AsParameters
    public static class MyParameters {
        String col1;
        String col2;
    }

    public ExamplesTable failingMethodReturningExamplesTable(String value) {
        throw new RuntimeException(value);
    }
    
    public void methodWithANamedParameter(@Named("theme") String theme, @Named("variant") String variant) {
        Map<String, Object> namedArgs = new HashMap<>();
        namedArgs.put("theme", theme);
        namedArgs.put("variant", variant);
        this.args = namedArgs;
    }

    public void methodWithoutNamedAnnotation(String theme) {
        this.args = theme;
    }

    public void multipleParamMethodWithoutNamedAnnotation(String theme, String variant) {
        HashMap<String, Object> multipleArgs = new HashMap<>();
        multipleArgs.put("theme", theme);
        multipleArgs.put("variant", variant);
        this.args = multipleArgs;
    }

    public void methodThatExpectsUuidExceptionWrapper(UUIDExceptionWrapper exception) {
        this.args = exception;
    }

    @ToContext("someKey")
    public String methodStoringAString() {
        return "someValue";
    }

    @ToContext(value = "someKey", retentionLevel = ToContext.RetentionLevel.SCENARIO)
    public String methodStoringAStringInScenario() {
        return "someValue";
    }

    @ToContext(value = "someKey", retentionLevel = ToContext.RetentionLevel.STORY)
    public String methodStoringAStringInStory() {
        return "someValue";
    }

    public void methodReadingFromContext(@FromContext("someKey") String value) {
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

    enum SomeEnum {
        ONE,
        TWO,
        THREE,
        MULTIPLE_WORDS_AND_1_NUMBER,
        ;
    }

}
