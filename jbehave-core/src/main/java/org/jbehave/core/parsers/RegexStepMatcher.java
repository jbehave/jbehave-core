package org.jbehave.core.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexStepMatcher implements StepMatcher {

	private final Pattern pattern;
	private final String[] parameterNames;
    private String pseudoPattern;
    private Matcher matcher;

	public RegexStepMatcher(Pattern pattern, String[] parameterNames, String stepPattern) {
		this.pattern = pattern;
		this.parameterNames = parameterNames;
        this.pseudoPattern = stepPattern;
    }
	
	public boolean matches(String stepWithoutStartingWord){
		matcher(stepWithoutStartingWord);
		return matcher.matches();
	}

	public boolean find(String stepWithoutStartingWord){
		matcher(stepWithoutStartingWord);
		return matcher.find();
	}
	
	public String parameter(int matchedPosition) {
		return matcher.group(matchedPosition);
	}

	private void matcher(String patternToMatch){
		matcher = pattern.matcher(patternToMatch);
	}

	public String[] parameterNames(){
		return parameterNames;
	}

	public String pattern() {
		return pattern.pattern();
	}

	public String pseudoPattern() {
		return pseudoPattern;
	}

}
