package org.jbehave.core.steps;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.core.steps.ParameterConverters.MethodReturningConverter;
import org.jbehave.core.steps.ParameterConverters.NumberConverter;
import org.jbehave.core.steps.ParameterConverters.NumberListConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConvertionFailed;
import org.jbehave.core.steps.ParameterConverters.StringListConverter;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class ParameterConvertersBehaviour {

    private static String NAN = new DecimalFormatSymbols().getNaN();
    private static String INFINITY = new DecimalFormatSymbols().getInfinity();

    @Test
    public void shouldConvertValuesToNumbersWithDefaultNumberFormat() {
        NumberConverter converter = new NumberConverter();
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, Locale.getDefault());
    }

    @Test
    public void shouldConvertValuesToNumbersWithLocalizedNumberFormat() {
        ParameterConverter enConverter = new NumberConverter(NumberFormat.getInstance(Locale.ENGLISH));
        assertThatAllNumberTypesAreAccepted(enConverter);
        assertThatAllNumbersAreConverted(enConverter, Locale.ENGLISH);
        assertThat((Integer) enConverter.convertValue("100,000", Integer.class), equalTo(100000));
        assertThat((Long) enConverter.convertValue("100,000", Long.class), equalTo(100000L));
        assertThat((Float) enConverter.convertValue("100,000.01", Float.class), equalTo(100000.01f));
        assertThat((Double) enConverter.convertValue("100,000.01", Double.class), equalTo(100000.01d));        
        assertThat((Double) enConverter.convertValue("1,00,000.01", Double.class), equalTo(100000.01d)); //Hindi style       
        ParameterConverter frConverter = new NumberConverter(NumberFormat.getInstance(Locale.FRENCH));
        assertThatAllNumberTypesAreAccepted(frConverter);
        assertThatAllNumbersAreConverted(frConverter, Locale.FRENCH);
        assertThat((Float) frConverter.convertValue("100000,01", Float.class), equalTo(100000.01f));
        assertThat((Double) frConverter.convertValue("100000,01", Double.class), equalTo(100000.01d));
    }

    private void assertThatAllNumberTypesAreAccepted(ParameterConverter converter) {
        assertThat(converter.accept(Byte.class), equalTo(true));
        assertThat(converter.accept(byte.class), equalTo(true));
        assertThat(converter.accept(Short.class), equalTo(true));
        assertThat(converter.accept(short.class), equalTo(true));
        assertThat(converter.accept(Integer.class), equalTo(true));
        assertThat(converter.accept(int.class), equalTo(true));
        assertThat(converter.accept(Float.class), equalTo(true));
        assertThat(converter.accept(float.class), equalTo(true));
        assertThat(converter.accept(Long.class), equalTo(true));
        assertThat(converter.accept(long.class), equalTo(true));
        assertThat(converter.accept(Double.class), equalTo(true));
        assertThat(converter.accept(double.class), equalTo(true));
        assertThat(converter.accept(BigInteger.class), equalTo(true));
        assertThat(converter.accept(BigDecimal.class), equalTo(true));
        assertThat(converter.accept(Number.class), equalTo(true));
        assertThat(converter.accept(WrongType.class), equalTo(false));        
    }

    private void assertThatAllNumbersAreConverted(ParameterConverter converter, Locale locale) {
    	DecimalFormatSymbols format = new DecimalFormatSymbols(locale);
    	char dot = format.getDecimalSeparator();
    	char minus = format.getMinusSign();
        assertThat((Byte) converter.convertValue("127", Byte.class), equalTo(Byte.MAX_VALUE));
		assertThat((Byte) converter.convertValue(minus + "128", byte.class), equalTo(Byte.MIN_VALUE));
        assertThat((Short) converter.convertValue("32767", Short.class), equalTo(Short.MAX_VALUE));
        assertThat((Short) converter.convertValue(minus + "32768", short.class), equalTo(Short.MIN_VALUE));
        assertThat((Integer) converter.convertValue("3", Integer.class), equalTo(3));
        assertThat((Integer) converter.convertValue("3", int.class), equalTo(3));
		assertThat((Float) converter.convertValue("3" + dot + "0", Float.class), equalTo(3.0f));
        assertThat((Float) converter.convertValue("3" + dot + "0", float.class), equalTo(3.0f));
        assertThat((Long) converter.convertValue("3", Long.class), equalTo(3L));
        assertThat((Long) converter.convertValue("3", long.class), equalTo(3L));
        assertThat((Double) converter.convertValue("3" + dot + "0", Double.class), equalTo(3.0d));
        assertThat((Double) converter.convertValue("3" + dot + "0", double.class), equalTo(3.0d));
        assertThat((BigInteger) converter.convertValue("3", BigInteger.class), equalTo(new BigInteger("3")));
        assertThat((BigDecimal) converter.convertValue("3" + dot + "0", BigDecimal.class), equalTo(new BigDecimal("3.0")));
        assertThat((BigDecimal) converter.convertValue("3" + dot + "00", BigDecimal.class), equalTo(new BigDecimal("3.00"))); // currency
        assertThat((BigDecimal) converter.convertValue("30000000", BigDecimal.class), equalTo(new BigDecimal(30000000))); // 7 or more digits
        assertThat((BigDecimal) converter.convertValue("3" + dot + "000", BigDecimal.class), equalTo(new BigDecimal("3.000"))); // something else!
        assertThat((Number) converter.convertValue("3", Number.class), equalTo((Number)3L));
    }

    @Test(expected = ParameterConvertionFailed.class)
    public void shouldFailToConvertInvalidNumbersWithNumberFormat() throws ParseException, IntrospectionException {
        ParameterConverter converter = new NumberConverter();
        converter.convertValue("abc", Long.class);
    }

    @Test
    public void shouldConvertNaNAndInfinityValuesToNumbers() {
        ParameterConverter converter = new NumberConverter();
        assertThat((Float) converter.convertValue(NAN, Float.class), equalTo(Float.NaN));
        assertThat((Float) converter.convertValue(INFINITY, Float.class), equalTo(Float.POSITIVE_INFINITY));
        assertThat((Float) converter.convertValue("-"+INFINITY, Float.class), equalTo(Float.NEGATIVE_INFINITY));
        assertThat((Double) converter.convertValue(NAN, Double.class), equalTo(Double.NaN));
        assertThat((Double) converter.convertValue(INFINITY, Double.class), equalTo(Double.POSITIVE_INFINITY));
        assertThat((Double) converter.convertValue("-"+INFINITY, Double.class), equalTo(Double.NEGATIVE_INFINITY));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertCommaSeparatedValuesToListOfNumbersWithDefaultFormat() throws ParseException, IntrospectionException {
        ParameterConverter converter = new NumberListConverter();
        Type listOfNumbers = SomeSteps.methodFor("aMethodWithListOfNumbers").getGenericParameterTypes()[0];
        Type setOfNumbers = SomeSteps.methodFor("aMethodWithSetOfNumbers").getGenericParameterTypes()[0];
        assertThat(converter.accept(listOfNumbers), is(true));
        assertThat(converter.accept(setOfNumbers), is(false));
        List<Number> list = (List<Number>) converter.convertValue("3, 0.5, 6.1f, 8.00", listOfNumbers);
        NumberFormat numberFormat = NumberFormat.getInstance();
        assertThat(list.get(0), equalTo(numberFormat.parse("3")));
        assertThat(list.get(1), equalTo(numberFormat.parse("0.5")));
        assertThat(list.get(2), equalTo(numberFormat.parse("6.1f")));
        assertThat(list.get(3), equalTo(numberFormat.parse("8.00")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertCommaSeparatedValuesToListOfNumbersWithCustomFormat() throws ParseException,
            IntrospectionException {
        NumberFormat numberFormat = new DecimalFormat("#,####");
        ParameterConverter converter = new NumberListConverter(numberFormat, " ");
        Type type = SomeSteps.methodFor("aMethodWithListOfNumbers").getGenericParameterTypes()[0];
        List<Number> list = (List<Number>) converter.convertValue("3,000 0.5 6.1f 8.00", type);
        assertThat(list.get(0), equalTo(numberFormat.parse("3,000")));
        assertThat(list.get(1), equalTo(numberFormat.parse("0.5")));
        assertThat(list.get(2), equalTo(numberFormat.parse("6.1f")));
        assertThat(list.get(3), equalTo(numberFormat.parse("8.00")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertCommaSeparatedValuesOfSpecificNumberTypes() throws ParseException, IntrospectionException {
        ParameterConverter converter = new NumberListConverter(NumberFormat.getInstance(Locale.ENGLISH), ",");
        Type doublesType = SomeSteps.methodFor("aMethodWithListOfDoubles").getGenericParameterTypes()[0];
        
        List<Double> doubles = (List<Double>) converter.convertValue("3, 0.5, 0.0, 8.00, "+NAN+","+INFINITY, doublesType);
        assertThat(doubles.get(0), equalTo(3.0d));
        assertThat(doubles.get(1), equalTo(0.5d));
        assertThat(doubles.get(2), equalTo(0.0d));
        assertThat(doubles.get(3), equalTo(8.00d));
        assertThat(doubles.get(4), equalTo(Double.NaN));
        assertThat(doubles.get(5), equalTo(Double.POSITIVE_INFINITY));
        
        Type floatsType = SomeSteps.methodFor("aMethodWithListOfFloats").getGenericParameterTypes()[0];
        List<Float> floats = (List<Float>) converter.convertValue("3, 0.5, 0.0, 8.00, "+NAN+", -"+INFINITY, floatsType);
        assertThat(floats.get(0), equalTo(3.0f));
        assertThat(floats.get(1), equalTo(0.5f));
        assertThat(floats.get(2), equalTo(0.0f));
        assertThat(floats.get(3), equalTo(8.00f));
        assertThat(floats.get(4), equalTo(Float.NaN));
        assertThat(floats.get(5), equalTo(Float.NEGATIVE_INFINITY));

        Type longsType = SomeSteps.methodFor("aMethodWithListOfLongs").getGenericParameterTypes()[0];
        List<Long> longs = (List<Long>) converter.convertValue("3, 0, 8", longsType);
        assertThat(longs.get(0), equalTo(3L));
        assertThat(longs.get(1), equalTo(0L));
        assertThat(longs.get(2), equalTo(8L));

        Type intsType = SomeSteps.methodFor("aMethodWithListOfIntegers").getGenericParameterTypes()[0];
        List<Integer> ints = (List<Integer>) converter.convertValue("3, 0, 8", intsType);
        assertThat(ints.get(0), equalTo(3));
        assertThat(ints.get(1), equalTo(0));
        assertThat(ints.get(2), equalTo(8));
    }

    @Test(expected = ParameterConvertionFailed.class)
    public void shouldFailToConvertCommaSeparatedValuesOfInvalidNumbers() throws ParseException, IntrospectionException {
        ParameterConverter converter = new NumberListConverter();
        Type type = SomeSteps.methodFor("aMethodWithListOfNumbers").getGenericParameterTypes()[0];
        converter.convertValue("3x, x.5", type);
    }

    @Test
    public void shouldConvertCommaSeparatedValuesToListOfStrings() throws IntrospectionException {
        ParameterConverter converter = new StringListConverter();
        Type listOfStrings = SomeSteps.methodFor("aMethodWithListOfStrings").getGenericParameterTypes()[0];
        Type listOfNumbers = SomeSteps.methodFor("aMethodWithListOfNumbers").getGenericParameterTypes()[0];
        Type setOfNumbers = SomeSteps.methodFor("aMethodWithSetOfNumbers").getGenericParameterTypes()[0];
        assertThat(converter.accept(listOfStrings), is(true));
        assertThat(converter.accept(listOfNumbers), is(false));
        assertThat(converter.accept(setOfNumbers), is(false));
        ensureValueIsConvertedToList(converter, listOfStrings, "a, string ", Arrays.asList("a", "string"));
        ensureValueIsConvertedToList(converter, listOfStrings, " ", Arrays.asList(new String[] {}));
    }

    @SuppressWarnings("unchecked")
    private void ensureValueIsConvertedToList(ParameterConverter converter, Type type, String value,
            List<String> expected) {
        List<String> list = (List<String>) converter.convertValue(value, type);
        assertThat(list.size(), equalTo(expected.size()));
    }

    @Test
    public void shouldConvertDateWithDefaultFormat() throws ParseException, IntrospectionException {
        ParameterConverter converter = new DateConverter();
        assertThat(converter.accept(Date.class), equalTo(true));
        assertThat(converter.accept(WrongType.class), is(false));
        assertThat(converter.accept(mock(Type.class)), is(false));
        Type type = SomeSteps.methodFor("aMethodWithDate").getGenericParameterTypes()[0];
        String date = "01/01/2010";
        assertThat((Date) converter.convertValue(date, type), equalTo(DateConverter.DEFAULT_FORMAT.parse(date)));
    }

    @Test
    public void shouldConvertDateWithCustomFormat() throws ParseException, IntrospectionException {
        DateFormat customFormat = new SimpleDateFormat("yyyy-MM-dd");
        ParameterConverter converter = new DateConverter(customFormat);
        assertThat(converter.accept(Date.class), equalTo(true));
        assertThat(converter.accept(WrongType.class), is(false));
        assertThat(converter.accept(mock(Type.class)), is(false));
        Type type = SomeSteps.methodFor("aMethodWithDate").getGenericParameterTypes()[0];
        String date = "2010-01-01";
        assertThat((Date) converter.convertValue(date, type), equalTo(customFormat.parse(date)));
    }

    @Test(expected = ParameterConvertionFailed.class)
    public void shouldFailToConvertDateWithInvalidFormat() throws ParseException, IntrospectionException {
        Type type = SomeSteps.methodFor("aMethodWithDate").getGenericParameterTypes()[0];
        ParameterConverter converter = new DateConverter();
        String date = "dd+MM+yyyy";
        converter.convertValue(date, type);
    }

    @Test
    public void shouldConvertMultilineTableParameter() throws ParseException, IntrospectionException {
        ParameterConverter converter = new ExamplesTableConverter();
        assertThat(converter.accept(ExamplesTable.class), is(true));
        assertThat(converter.accept(WrongType.class), is(false));
        assertThat(converter.accept(mock(Type.class)), is(false));
        Type type = SomeSteps.methodFor("aMethodWithExamplesTable").getGenericParameterTypes()[0];
        String value = "|col1|col2|\n|row11|row12|\n|row21|row22|\n";
        ExamplesTable table = (ExamplesTable) converter.convertValue(value, type);
        assertThat(table.getRowCount(), equalTo(2));
        Map<String, String> row1 = table.getRow(0);
        assertThat(row1.get("col1"), equalTo("row11"));
        assertThat(row1.get("col2"), equalTo("row12"));
        Map<String, String> row2 = table.getRow(1);
        assertThat(row2.get("col1"), equalTo("row21"));
        assertThat(row2.get("col2"), equalTo("row22"));
    }

    @Test
    public void shouldConvertParameterFromMethodReturningValue() throws ParseException, IntrospectionException {
        Method method = SomeSteps.methodFor("aMethodReturningExamplesTable");
        ParameterConverter converter = new MethodReturningConverter(method, new SomeSteps());
        assertThat(converter.accept(method.getReturnType()), is(true));
        assertThat(converter.accept(WrongType.class), is(false));
        assertThat(converter.accept(mock(Type.class)), is(false));
        String value = "|col1|col2|\n|row11|row12|\n|row21|row22|\n";
        ExamplesTable table = (ExamplesTable) converter.convertValue(value, ExamplesTable.class);
        assertThat(table.getRowCount(), equalTo(2));
        Map<String, String> row1 = table.getRow(0);
        assertThat(row1.get("col1"), equalTo("row11"));
        assertThat(row1.get("col2"), equalTo("row12"));
        Map<String, String> row2 = table.getRow(1);
        assertThat(row2.get("col1"), equalTo("row21"));
        assertThat(row2.get("col2"), equalTo("row22"));
    }

    @Test(expected = ParameterConvertionFailed.class)
    public void shouldFailToConvertParameterFromFailingMethodReturningValue() throws ParseException,
            IntrospectionException {
        Method method = SomeSteps.methodFor("aFailingMethodReturningExamplesTable");
        ParameterConverter converter = new MethodReturningConverter(method, new SomeSteps());
        String value = "|col1|col2|\n|row11|row12|\n|row21|row22|\n";
        converter.convertValue(value, ExamplesTable.class);
    }

    static class WrongType {

    }
}
