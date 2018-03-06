package org.jbehave.core.steps;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.steps.StepCreator.PendingStep;

import static java.text.MessageFormat.format;

public class PendingStepMethodGenerator {

    private static final String METHOD_SOURCE = "@{0}(\"{1}\")\n@{2}\npublic void {3}() '{'\n  // {4}\n'}'\n";

    private final Keywords keywords;

    public PendingStepMethodGenerator(Keywords keywords) {
        this.keywords = keywords;
    }

    public String generateMethod(PendingStep step) {
        String stepAsString = step.stepAsString();
        String previousNonAndStepAsString = step.previousNonAndStepAsString();
        StepType stepType = null;
        if (keywords.isAndStep(stepAsString) && previousNonAndStepAsString != null) {
            stepType = keywords.stepTypeFor(previousNonAndStepAsString);
        } else {
            stepType = keywords.stepTypeFor(stepAsString);
        }
        String stepPattern = keywords.stepWithoutStartingWord(stepAsString, stepType);
        String stepAnnotation = StringUtils.capitalize(stepType.name().toLowerCase());
        String methodName = methodName(stepType, stepPattern);
        String pendingAnnotation = Pending.class.getSimpleName();
        return format(METHOD_SOURCE, stepAnnotation, StringEscapeUtils.escapeJava(stepPattern), pendingAnnotation,
                methodName, keywords.pending());
    }

    private String methodName(StepType stepType, String stepPattern) {
        String name = stepType.name().toLowerCase() + WordUtils.capitalize(stepPattern);
        char filteredName[]=new char[name.length()];
        int index=0;
        for(int i=0;i<name.length();i++) {
            char ch=name.charAt(i);
            if(Character.isJavaIdentifierPart(ch) && ch!='$' && ch!=127) {
                filteredName[index++]=ch;
            }
        }
        return new String(filteredName,0,index);
    }

}
