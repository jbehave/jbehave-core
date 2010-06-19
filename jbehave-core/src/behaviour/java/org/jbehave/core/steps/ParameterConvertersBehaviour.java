package org.jbehave.core.steps;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.core.steps.ParameterConverters.MethodReturningConverter;
import org.jbehave.core.steps.ParameterConverters.NumberListConverter;
import org.junit.Test;

public class ParameterConvertersBehaviour {

	private static final NumberFormat NUMBER_FORMAT = NumberFormat
			.getInstance();

	@Test
	public void shouldConvertValuesToNumbers() {
		ParameterConverters converters = new ParameterConverters();
		assertThat((Integer) converters.convert("3", Integer.class), equalTo(3));
		assertThat((Integer) converters.convert("3", int.class), equalTo(3));
		assertThat((Float) converters.convert("3.0", Float.class),
				equalTo(3.0f));
		assertThat((Float) converters.convert("3.0", float.class),
				equalTo(3.0f));
		assertThat((Long) converters.convert("3", Long.class), equalTo(3L));
		assertThat((Long) converters.convert("3", long.class), equalTo(3L));
		assertThat((Double) converters.convert("3.0", Double.class),
				equalTo(3.0d));
		assertThat((Double) converters.convert("3.0", double.class),
				equalTo(3.0d));
		assertThat((BigInteger) converters.convert("3", BigInteger.class),
				equalTo(new BigInteger("3")));
		assertThat((BigDecimal) converters.convert("3.0", BigDecimal.class),
				equalTo(new BigDecimal("3.0")));
	}

	@Test
	public void shouldConvertNaNAndInfinityValuesToNumbers() {
		ParameterConverters converters = new ParameterConverters();
		assertThat((Float) converters.convert("Infinity", Float.class),
				equalTo(Float.POSITIVE_INFINITY));
		assertThat((Float) converters.convert("-Infinity", Float.class),
				equalTo(Float.NEGATIVE_INFINITY));
		assertThat((Float) converters.convert("NaN", Float.class),
				equalTo(Float.NaN));
		assertThat((Double) converters.convert("NaN", Double.class),
				equalTo(Double.NaN));
		assertThat((Double) converters.convert("Infinity", Double.class),
				equalTo(Double.POSITIVE_INFINITY));
		assertThat((Double) converters.convert("-Infinity", Double.class),
				equalTo(Double.NEGATIVE_INFINITY));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldConvertCommaSeparatedValuesToListsOfNumbers()
			throws ParseException, IntrospectionException {
		ParameterConverters converters = new ParameterConverters();
		Type type = SomeSteps.methodFor("aMethodWithListOfNumbers")
				.getGenericParameterTypes()[0];
		List<Number> list = (List<Number>) converters.convert(
				"3, 0.5, 6.1f, 8.00", type);
		assertThat(list.get(0), equalTo(NUMBER_FORMAT.parse("3")));
		assertThat(list.get(1), equalTo(NUMBER_FORMAT.parse("0.5")));
		assertThat(list.get(2), equalTo(NUMBER_FORMAT.parse("6.1f")));
		assertThat(list.get(3), equalTo(NUMBER_FORMAT.parse("8.00")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldConvertCommaSeparatedValuesToListsOfNumbersWithCustomFormat()
			throws ParseException, IntrospectionException {
		NumberFormat numberFormat = new DecimalFormat("#,####");
		ParameterConverters converters = new ParameterConverters()
				.addConverters(new NumberListConverter(numberFormat, " "));
	Type type = SomeSteps.methodFor("aMethodWithListOfNumbers")
				.getGenericParameterTypes()[0];
		List<Number> list = (List<Number>) converters.convert(
				"3,000 0.5 6.1f 8.00", type);
		assertThat(list.get(0), equalTo(numberFormat.parse("3,000")));
		assertThat(list.get(1), equalTo(numberFormat.parse("0.5")));
		assertThat(list.get(2), equalTo(numberFormat.parse("6.1f")));
		assertThat(list.get(3), equalTo(numberFormat.parse("8.00")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldConvertCommaSeparatedValuesOfSpecificNumberTypes()
			throws ParseException, IntrospectionException {
		ParameterConverters converters = new ParameterConverters();
		Type doublesType = SomeSteps.methodFor("aMethodWithListOfDoubles")
				.getGenericParameterTypes()[0];
		List<Double> doubles = (List<Double>) converters.convert(
				"3, 0.5, 0.0, 8.00, NaN, Infinity", doublesType);
		assertThat(doubles.get(0), equalTo(3.0d));
		assertThat(doubles.get(1), equalTo(0.5d));
		assertThat(doubles.get(2), equalTo(0.0d));
		assertThat(doubles.get(3), equalTo(8.00d));
		assertThat(doubles.get(4), equalTo(Double.NaN));
		assertThat(doubles.get(5), equalTo(Double.POSITIVE_INFINITY));

		Type floatsType = SomeSteps.methodFor("aMethodWithListOfFloats")
				.getGenericParameterTypes()[0];
		List<Float> floats = (List<Float>) converters.convert(
				"3, 0.5, 0.0, 8.00, NaN, -Infinity", floatsType);
		assertThat(floats.get(0), equalTo(3.0f));
		assertThat(floats.get(1), equalTo(0.5f));
		assertThat(floats.get(2), equalTo(0.0f));
		assertThat(floats.get(3), equalTo(8.00f));
		assertThat(floats.get(4), equalTo(Float.NaN));
		assertThat(floats.get(5), equalTo(Float.NEGATIVE_INFINITY));

		Type longsType = SomeSteps.methodFor("aMethodWithListOfLongs")
				.getGenericParameterTypes()[0];
		List<Long> longs = (List<Long>) converters
				.convert("3, 0, 8", longsType);
		assertThat(longs.get(0), equalTo(3L));
		assertThat(longs.get(1), equalTo(0L));
		assertThat(longs.get(2), equalTo(8L));

		Type intsType = SomeSteps.methodFor("aMethodWithListOfIntegers")
				.getGenericParameterTypes()[0];
		List<Integer> ints = (List<Integer>) converters.convert("3, 0, 8",
				intsType);
		assertThat(ints.get(0), equalTo(3));
		assertThat(ints.get(1), equalTo(0));
		assertThat(ints.get(2), equalTo(8));
	}

	@Test
	public void shouldConvertCommaSeparatedValuesToListOfStrings()
			throws IntrospectionException {
		ParameterConverters converters = new ParameterConverters();
		Type type = SomeSteps.methodFor("aMethodWithListOfStrings")
				.getGenericParameterTypes()[0];
		List<String> emptyList = Arrays.asList("a", "string");
		ensureValueIsConvertedToEmptyList(converters, type, "a, string ",
				emptyList);
	}

	@Test
	public void shouldConvertEmptyStringToEmptyListOfStrings()
			throws IntrospectionException {
		ParameterConverters converters = new ParameterConverters();
		Type type = SomeSteps.methodFor("aMethodWithListOfStrings")
				.getGenericParameterTypes()[0];
		List<String> emptyList = Arrays.asList();
		ensureValueIsConvertedToEmptyList(converters, type, "", emptyList);
		ensureValueIsConvertedToEmptyList(converters, type, " ", emptyList);
	}

	@SuppressWarnings("unchecked")
	private void ensureValueIsConvertedToEmptyList(
			ParameterConverters converters, Type type, String value,
			List<String> expected) {
		List<String> list = (List<String>) converters.convert(value, type);
		assertThat(list.size(), equalTo(expected.size()));
	}

	@Test
	public void shouldConvertMultilineTableParameter() throws ParseException,
			IntrospectionException {
		ParameterConverters converters = new ParameterConverters()
				.addConverters(new ExamplesTableConverter());
		Type type = SomeSteps.methodFor("aMethodWithExamplesTable")
				.getGenericParameterTypes()[0];
		String value = "|col1|col2|\n|row11|row12|\n|row21|row22|\n";
		ExamplesTable table = (ExamplesTable) converters.convert(value, type);
		assertThat(table.getRowCount(), equalTo(2));
		Map<String, String> row1 = table.getRow(0);
		assertThat(row1.get("col1"), equalTo("row11"));
		assertThat(row1.get("col2"), equalTo("row12"));
		Map<String, String> row2 = table.getRow(1);
		assertThat(row2.get("col1"), equalTo("row21"));
		assertThat(row2.get("col2"), equalTo("row22"));
	}

	@Test
    public void shouldConvertParameterFromMethodReturningValue()
            throws ParseException, IntrospectionException {
        Method method = SomeSteps.methodFor("aMethodReturningExamplesTable");
        ParameterConverters converters = new ParameterConverters()
        	.addConverters(new MethodReturningConverter(method, new SomeSteps()));
		String value = "|col1|col2|\n|row11|row12|\n|row21|row22|\n";
		ExamplesTable table = (ExamplesTable) converters.convert(value, ExamplesTable.class);
		assertThat(table.getRowCount(), equalTo(2));
		Map<String, String> row1 = table.getRow(0);
		assertThat(row1.get("col1"), equalTo("row11"));
		assertThat(row1.get("col2"), equalTo("row12"));
		Map<String, String> row2 = table.getRow(1);
		assertThat(row2.get("col1"), equalTo("row21"));
		assertThat(row2.get("col2"), equalTo("row22"));
    }

}
