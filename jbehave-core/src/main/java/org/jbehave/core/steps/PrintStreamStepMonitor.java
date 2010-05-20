package org.jbehave.core.steps;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * StepMonitor that prints to a {@link PrintStream}, defaulting to
 * {@link System.out}.
 */
public class PrintStreamStepMonitor implements StepMonitor {

	private static final String CONVERTED_VALUE_OF_TYPE = "Converted value ''{0}'' of type ''{1}'' to ''{2}'' with converter ''{3}''";
	private static final String STEP_MATCHES_TYPE = "Step ''{0}'' (with previous step ''{1}'') ''{2}'' type ''{3}'' for method ''{4}'' with annotations ''{5}'' in steps instance ''{6}''";
	private static final String STEP_MATCHES_PATTERN = "Step ''{0}'' {1} pattern ''{2}'' for method ''{3}'' with annotations ''{4}'' in steps instance ''{5}''";
	private static final String PERFORMING = "Performing step ''{0}'' {1}";
	private static final String DRY_RUN = "(DRY RUN)";
	private static final String MATCHES = "matches";
	private static final String DOES_NOT_MATCH = "does not match";
	private static final String USING_NAME_FOR_ARG = "Using {0} name ''{1}'' for position {2}";
	private static final String ANNOTATED = "annotated";
	private static final String PARAMETER = "parameter";
	private static final String TABLE_ANNOTATED = "table annotated";
	private static final String TABLE_PARAMETER = "table parameter";
	private static final String USING_NATURAL_ORDER_FOR_ARG = "Using natural order for position {0}";
	private static final String FOUND_ARG = "Found argument ''{0}'' for position {1}";

	private final PrintStream output;

	public PrintStreamStepMonitor() {
		this(System.out);
	}

	public PrintStreamStepMonitor(PrintStream output) {
		this.output = output;
	}

	public void stepMatchesType(String step, String previous, boolean matches,
			StepType stepType, Method method, Object stepsInstance) {
		String message = format(STEP_MATCHES_TYPE, step, previous,
				(matches ? MATCHES : DOES_NOT_MATCH), stepType, method,
				asList(method.getAnnotations()), stepsInstance);
		print(output, message);
	}

	public void stepMatchesPattern(String step, boolean matches,
			String pattern, Method method, Object stepsInstance) {
		String message = format(STEP_MATCHES_PATTERN, step, (matches ? MATCHES
				: DOES_NOT_MATCH), pattern, method, asList(method
				.getAnnotations()), stepsInstance);
		print(output, message);
	}

	public void convertedValueOfType(String value, Type type, Object converted,
			Class<?> converterClass) {
		String message = format(CONVERTED_VALUE_OF_TYPE, value, type,
				converted, converterClass);
		print(output, message);
	}

	public void performing(String step, boolean dryRun) {
		String message = format(PERFORMING, step, (dryRun ? DRY_RUN : ""));
		print(output, message);
	}

	public void usingAnnotatedNameForArg(String name, int position) {
		String message = format(USING_NAME_FOR_ARG, ANNOTATED, name, position);
		print(output, message);
	}

	public void usingParameterNameForArg(String name, int position) {
		String message = format(USING_NAME_FOR_ARG, PARAMETER, name, position);
		print(output, message);
	}

	public void usingTableAnnotatedNameForArg(String name, int position) {
		String message = format(USING_NAME_FOR_ARG, TABLE_ANNOTATED, name,
				position);
		print(output, message);
	}

	public void usingTableParameterNameForArg(String name, int position) {
		String message = format(USING_NAME_FOR_ARG, TABLE_PARAMETER, name,
				position);
		print(output, message);
	}

	public void usingNaturalOrderForArg(int position) {
		String message = format(USING_NATURAL_ORDER_FOR_ARG, position);
		print(output, message);
	}

	public void foundArg(String arg, int position) {
		String message = format(FOUND_ARG, arg, position);
		print(output, message);
	}

	protected void print(PrintStream output, String message) {
		output.println(message);
	}

}
