package org.jbehave.core.steps;

import java.beans.IntrospectionException;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.junit.Test;

import static org.apache.commons.lang.StringEscapeUtils.escapeJava;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;


public class PendingStepMethodGeneratorBehaviour {
        
    private PendingStepMethodGenerator generator = new PendingStepMethodGenerator(new LocalizedKeywords());

    @Test
    public void shouldGenerateMethodForNonAndPendingStep() throws IntrospectionException {
        // When
        PendingStep pendingStep = (PendingStep) StepCreator.createPendingStep("When I am pending", null);

        // Then
        String method = 
            "@When(\"I am pending\")\n" +
            "@Pending\n"+
            "public void whenIAmPending(){\n"+
            "  // PENDING\n"+
            "}\n";
       assertThat(generator.generateMethod(pendingStep), equalTo(method));
    }

    @Test
    public void shouldGenerateMethodForAndPendingStepWithPreviousNonAndStep() throws IntrospectionException {

        // When
        PendingStep pendingStep = (PendingStep) StepCreator.createPendingStep("And I am pending", "Given I was pending");

        // Then
        String method = 
            "@Given(\"I am pending\")\n" +
            "@Pending\n"+
            "public void givenIAmPending(){\n"+
            "  // PENDING\n"+
            "}\n";
       assertThat(generator.generateMethod(pendingStep), equalTo(method));
    }
        
    @Test
    public void shouldNormaliseStepPatternToJavaCompatibleMethodNameAndString() throws IntrospectionException {
        // When
        String pattern = "I'm searching for \".*\", and for others chars such as :;!|, and I look for <this>: $ \\ / () {} [] ";
        PendingStep pendingStep = (PendingStep) StepCreator.createPendingStep("When "+pattern, null);

        // Then
        String method = 
            "@When(\""+escapeJava(pattern)+"\")\n" +
            "@Pending\n"+
            "public void whenImSearchingForAndForOthersCharsSuchAsAndILookForthis(){\n"+
            "  // PENDING\n"+
            "}\n";
       assertThat(generator.generateMethod(pendingStep), equalTo(method));
    }

}
