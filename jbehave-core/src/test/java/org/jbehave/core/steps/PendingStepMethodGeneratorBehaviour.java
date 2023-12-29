package org.jbehave.core.steps;

import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.junit.jupiter.api.Test;


class PendingStepMethodGeneratorBehaviour {

    private PendingStepMethodGenerator generator = new PendingStepMethodGenerator(new LocalizedKeywords());

    @Test
    void shouldGenerateMethodForNonAndPendingStep() {
        // When
        PendingStep pendingStep = (PendingStep) StepCreator.createPendingStep("When I am pending", null);

        // Then
        String method =
            "@When(\"I am pending\")\n"
            + "@Pending\n"
            + "public void whenIAmPending() {\n"
            + "  // PENDING\n"
            + "}\n";
        assertThat(generator.generateMethod(pendingStep), equalTo(method));
    }

    @Test
    void shouldGenerateMethodForAndPendingStepWithPreviousNonAndStep() {

        // When
        PendingStep pendingStep = (PendingStep) StepCreator.createPendingStep("And I am pending",
                "Given I was pending");

        // Then
        String method =
            "@Given(\"I am pending\")\n"
            + "@Pending\n"
            + "public void givenIAmPending() {\n"
            + "  // PENDING\n"
            + "}\n";
        assertThat(generator.generateMethod(pendingStep), equalTo(method));
    }

    @Test
    void shouldNormaliseStepPatternToJavaCompatibleMethodNameAndString() {
        // When
        String pattern = "I'm searching for \".*\", and for others chars such as :;!|, "
                + "and I look for <this>: $ \\ / () {} [] ";
        PendingStep pendingStep = (PendingStep) StepCreator.createPendingStep("When " + pattern, null);

        // Then
        String method =
            "@When(\"" + escapeJava(pattern) + "\")\n"
            + "@Pending\n"
            + "public void whenImSearchingForAndForOthersCharsSuchAsAndILookForthis() {\n"
            + "  // PENDING\n"
            + "}\n";
        assertThat(generator.generateMethod(pendingStep), equalTo(method));

        // test basically all characters (issue JBEHAVE-710)
        // When
        pattern = "I'm searching for ";
        for (int i = 32; i < 128; i++) {
            pattern += (char) i;
        }
        pendingStep = (PendingStep) StepCreator.createPendingStep("When " + pattern, null);

        // Then
        method =
            "@When(\"" + escapeJava(pattern) + "\")\n"
            + "@Pending\n"
            + "public void whenImSearchingFor0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz() {\n"
            + "  // PENDING\n"
            + "}\n";
        assertThat(generator.generateMethod(pendingStep), equalTo(method));

        // When
        pattern = "I'm searching for ";
        for (int i = 160; i < 256; i++) {
            pattern += (char) i;
        }
        pendingStep = (PendingStep) StepCreator.createPendingStep("When " + pattern, null);

        // Then
        method =
            "@When(\"" + escapeJava(pattern) + "\")\n"
            + "@Pending\n"
            + "public void whenImSearchingFor¢£¤¥ª­µºÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ() "
            + "{\n"
            + "  // PENDING\n"
            + "}\n";
        assertThat(generator.generateMethod(pendingStep), equalTo(method));
    }

}
