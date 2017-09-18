package org.jbehave.core.steps;

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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ParameterConverters.BooleanConverter;
import org.jbehave.core.steps.ParameterConverters.BooleanListConverter;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.EnumConverter;
import org.jbehave.core.steps.ParameterConverters.EnumListConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableParametersConverter;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.jbehave.core.steps.ParameterConverters.MethodReturningConverter;
import org.jbehave.core.steps.ParameterConverters.NumberConverter;
import org.jbehave.core.steps.ParameterConverters.NumberListConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConvertionFailed;
import org.jbehave.core.steps.ParameterConverters.StringListConverter;
import org.jbehave.core.steps.SomeSteps.MyParameters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class ParameterConvertersBehaviour {

    private static String NAN = new DecimalFormatSymbols().getNaN();
    private static String INFINITY = new DecimalFormatSymbols().getInfinity();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDefineDefaultConverters() {
        LoadFromClasspath resourceLoader = new LoadFromClasspath();
        TableTransformers tableTransformers = new TableTransformers();
        ParameterConverters converters = new ParameterConverters(resourceLoader, tableTransformers);
        ParameterConverter[] defaultConverters = converters.defaultConverters(resourceLoader, tableTransformers,
                Locale.ENGLISH, ",");
        assertThatDefaultConvertersInclude(defaultConverters, BooleanConverter.class, NumberConverter.class,
                NumberListConverter.class, StringListConverter.class, DateConverter.class, EnumConverter.class,
                EnumListConverter.class, ExamplesTableConverter.class, ExamplesTableParametersConverter.class);
    }

    private void assertThatDefaultConvertersInclude(ParameterConverter[] defaultConverters,
            @SuppressWarnings("unchecked") Class<? extends ParameterConverter>... converterTypes) {
        for (Class<? extends ParameterConverter> type : converterTypes) {
            boolean found = false;
            for (ParameterConverter converter : defaultConverters) {
                if (converter.getClass().isAssignableFrom(type)) {
                    found = true;
                }
            }
            if (!found) {
                throw new RuntimeException("Converter " + type + " should be in the list of default converters");
            }
        }
    }

    @Test
    public void shouldConvertValuesToNumbersWithDefaultNumberFormat() {
        NumberConverter converter = new NumberConverter();
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, ParameterConverters.DEFAULT_NUMBER_FORMAT_LOCAL);
    }

    @Test
    public void shouldConvertValuesToNumbersWithEnglishNumberFormat() {
        Locale locale = Locale.ENGLISH;
        ParameterConverter converter = new NumberConverter(NumberFormat.getInstance(locale));
        assertConverterForLocale(converter, locale);
    }

    private void assertConverterForLocale(ParameterConverter converter, Locale locale) {
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, locale);
        assertThat((Integer) converter.convertValue("100,000", Integer.class), equalTo(100000));
        assertThat((Long) converter.convertValue("100,000", Long.class), equalTo(100000L));
        assertThat((Float) converter.convertValue("100,000.01", Float.class), equalTo(100000.01f));
        assertThat((Double) converter.convertValue("100,000.01", Double.class), equalTo(100000.01d));        
        assertThat((Double) converter.convertValue("1,00,000.01", Double.class), equalTo(100000.01d)); //Hindi style       
        assertThat((BigDecimal) converter.convertValue("1,000,000.01", BigDecimal.class), equalTo(new BigDecimal("1000000.01")));
    }
    
    @Test
    public void shouldConvertValuesToNumbersWithEnglishNumberFormatInMultipleThreads() {
        final Locale locale = Locale.ENGLISH;
        final int threads = 3;
        final ParameterConverter converter = new NumberConverter(NumberFormat.getInstance(locale));
        final BlockingQueue<String> queue = new ArrayBlockingQueue<String>(threads);
        Thread t1 = new Thread(){
            @Override
            public void run(){
                assertConverterForLocale(converter, locale);
                queue.add(Thread.currentThread().getName());
            }
        };
        Thread t2 = new Thread(){
            @Override
            public void run(){
                assertConverterForLocale(converter, locale);
                queue.add(Thread.currentThread().getName());
            }
        };
        Thread t3 = new Thread(){
            @Override
            public void run(){
                assertConverterForLocale(converter, locale);
                queue.add(Thread.currentThread().getName());
            }
        };

        t1.start();
        t2.start();
        t3.start();
        
        for (int i = 0;i < threads;i++){
            try {
                System.out.println(queue.take() + " completed.");
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }
        }

    }
    @Test
    public void shouldConvertValuesToNumbersWithFrenchNumberFormat() {
        Locale locale = Locale.FRENCH;
        ParameterConverter converter = new NumberConverter(NumberFormat.getInstance(locale));
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, locale);
        assertThat((Float) converter.convertValue("100000,01", Float.class), equalTo(100000.01f));
        assertThat((Double) converter.convertValue("100000,01", Double.class), equalTo(100000.01d));
    }

    @Test
    public void shouldConvertValuesToNumbersWithGermanNumberFormat() {
        Locale locale = Locale.GERMAN;
        ParameterConverter converter = new NumberConverter(NumberFormat.getInstance(locale));        
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, locale);
        assertThat((BigDecimal) converter.convertValue("1.000.000,01", BigDecimal.class), equalTo(new BigDecimal("1000000.01")));
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
        assertThat(converter.accept(AtomicInteger.class), equalTo(true));
        assertThat(converter.accept(AtomicLong.class), equalTo(true));
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
        assertThat((BigDecimal) converter.convertValue("-3", BigDecimal.class), equalTo(new BigDecimal("-3"))); // negative
        assertThat(((AtomicInteger)converter.convertValue("3", AtomicInteger.class)).get(), equalTo(3));
        assertThat(((AtomicLong)converter.convertValue("3", AtomicLong.class)).get(), equalTo(3L));
        assertThat((Number) converter.convertValue("3", Number.class), equalTo((Number)3L));
    }

    @Test
    public void shouldFailToConvertInvalidNumbersWithNumberFormat() {
        NumberConverter converter = new NumberConverter();
        try {
            converter.convertValue("abc", Long.class);
            fail("Exception was not thrown");
        } catch (ParameterConvertionFailed e) {
            assertThat(e.getCause(), is(instanceOf(ParseException.class)));
        }
    }

    @Test
    public void shouldFailToConvertInvalidNumbersWithNumberFormat2()  {
        NumberConverter converter = new NumberConverter();
        try {
            converter.convertValue("12.34.56", BigDecimal.class);
            fail("Exception was not thrown");
        } catch (ParameterConvertionFailed e) {
            assertThat(e.getCause(), is(instanceOf(NumberFormatException.class)));
        }
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
        NumberFormat numberFormat = NumberFormat.getInstance(ParameterConverters.DEFAULT_NUMBER_FORMAT_LOCAL);
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
    public void shouldConvertMultilineTable() throws ParseException, IntrospectionException {
        ParameterConverter converter = new ExamplesTableConverter(
                new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers()));
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
    public void shouldConvertMultilineTableToParameters() throws ParseException, IntrospectionException {
        ParameterConverter converter = new ExamplesTableParametersConverter(
                new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers()));
        Type type = SomeSteps.methodFor("aMethodWithExamplesTableParameters").getGenericParameterTypes()[0];
        assertThat(converter.accept(type), is(true));
        assertThat(converter.accept(WrongType.class), is(false));
        assertThat(converter.accept(mock(Type.class)), is(false));
        String value = "|col1|col2|\n|row11|row12|\n|row21|row22|\n";
        @SuppressWarnings("unchecked")
        List<MyParameters> parameters = (List<MyParameters>) converter.convertValue(value, type);
        assertThat(parameters.size(), equalTo(2));
        MyParameters row1 = parameters.get(0);
        assertThat(row1.col1, equalTo("row11"));
        assertThat(row1.col2, equalTo("row12"));
        MyParameters row2 = parameters.get(1);
        assertThat(row2.col1, equalTo("row21"));
        assertThat(row2.col2, equalTo("row22"));
    }

    @Test
    public void shouldConvertSinglelineTableToParameters() throws ParseException, IntrospectionException {
        ParameterConverter converter = new ExamplesTableParametersConverter(
                new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers()));
        Type type = SomeSteps.methodFor("aMethodWithExamplesTableParameter").getGenericParameterTypes()[0];
        assertThat(converter.accept(type), is(true));
        assertThat(converter.accept(WrongType.class), is(false));
        assertThat(converter.accept(mock(Type.class)), is(false));
        String value = "|col1|col2|\n|row11|row12|\n";
        MyParameters parameters = (MyParameters) converter.convertValue(value, type);
        assertThat(parameters.col1, equalTo("row11"));
        assertThat(parameters.col2, equalTo("row12"));
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

    @Test(expected = ParameterConvertionFailed.class)
    public void shouldFailToConvertToUnknownType() throws ParseException, IntrospectionException {
        new ParameterConverters(new LoadFromClasspath(), new TableTransformers()).convert("abc", WrongType.class);
    }

    static class WrongType {

    }
    
    @Test
    public void shouldConvertEnum() throws IntrospectionException {
        ParameterConverter converter = new EnumConverter();
        assertThat(converter.accept(SomeEnum.class), equalTo(true));
        assertThat(converter.accept(WrongType.class), is(false));
        assertThat(converter.accept(mock(Type.class)), is(false));
        Type type = SomeSteps.methodFor("aMethodWithEnum").getGenericParameterTypes()[0];
        assertThat((SomeEnum) converter.convertValue("ONE", type), equalTo(SomeEnum.ONE));
    }

    @Test
    public void shouldConvertEnumFluently() {
        ParameterConverter converter = new FluentEnumConverter();
        assertThat(converter.accept(SomeEnum.class), equalTo(true));
        assertThat((SomeEnum) converter.convertValue("multiple words and 1 number", SomeEnum.class), equalTo(SomeEnum.MULTIPLE_WORDS_AND_1_NUMBER));
    }
    
    
    @Test(expected = ParameterConvertionFailed.class)
    public void shouldFailToConvertEnumForValueNotDefined() throws IntrospectionException {
        ParameterConverter converter = new EnumConverter();
        Type type = SomeSteps.methodFor("aMethodWithEnum").getGenericParameterTypes()[0];
        converter.convertValue("FOUR", type);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertEnumList() throws IntrospectionException {
        ParameterConverter converter = new EnumListConverter();
        Type type = SomeSteps.methodFor("aMethodWithEnumList").getGenericParameterTypes()[0];
        assertThat(converter.accept(type), equalTo(true));
        List<SomeEnum> list = (List<SomeEnum>)converter.convertValue("ONE,TWO,THREE", type);
        assertThat(list.get(0), equalTo(SomeEnum.ONE));
        assertThat(list.get(1), equalTo(SomeEnum.TWO));
        assertThat(list.get(2), equalTo(SomeEnum.THREE));
    }

    @Test
    public void shouldConvertBoolean() throws IntrospectionException {
        ParameterConverter converter = new BooleanConverter();
        assertThat(converter.accept(Boolean.TYPE), equalTo(true));
        assertThat(converter.accept(Boolean.class), equalTo(true));
        assertThat(converter.accept(WrongType.class), is(false));
        assertThat(converter.accept(mock(Type.class)), is(false));
        Type type = SomeSteps.methodFor("aMethodWithBoolean").getGenericParameterTypes()[0];
        assertThat((Boolean) converter.convertValue("true", type), is(true));
        assertThat((Boolean) converter.convertValue("false", type), is(false));
        assertThat((Boolean) converter.convertValue("whatever", type), is(false));
    }

    @Test
    public void shouldConvertBooleanWithCustomValues() throws IntrospectionException {
        ParameterConverter converter = new BooleanConverter("ON", "OFF");
        assertThat(converter.accept(Boolean.TYPE), equalTo(true));
        assertThat(converter.accept(Boolean.class), equalTo(true));
        assertThat(converter.accept(WrongType.class), is(false));
        assertThat(converter.accept(mock(Type.class)), is(false));
        Type type = SomeSteps.methodFor("aMethodWithBoolean").getGenericParameterTypes()[0];
        assertThat((Boolean) converter.convertValue("ON", type), is(true));
        assertThat((Boolean) converter.convertValue("OFF", type), is(false));
        assertThat((Boolean) converter.convertValue("whatever", type), is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertBooleanList() throws IntrospectionException {
        ParameterConverter converter = new BooleanListConverter();
        Type type = SomeSteps.methodFor("aMethodWithBooleanList").getGenericParameterTypes()[0];
        assertThat(converter.accept(type), equalTo(true));
        List<Boolean> list = (List<Boolean>) converter.convertValue("true,false,true", type);
        assertThat(list.get(0), is(true));
        assertThat(list.get(1), is(false));
        assertThat(list.get(2), is(true));
    }

    @Test(expected = ParameterConvertionFailed.class)
    public void shouldNotModifyListOfConvertersFromOriginalParameterConvertersWhenCreatingNewInstance() throws Exception {
        ParameterConverters original = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        original.newInstanceAdding(new FooToBarParameterConverter());

        ensureItStillDoesNotKnowHowToConvertFooToBar(original);
    }

    private void ensureItStillDoesNotKnowHowToConvertFooToBar(ParameterConverters original) {
        original.convert("foo", Bar.class);
    }

    private class Bar {
    }

    private class FooToBarParameterConverter implements ParameterConverter {
        public boolean accept(Type type) {
            return type == Bar.class;
        }

        public Object convertValue(String value, Type type) {
            return new Bar();
        }
    }
}
