package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.gson.Gson;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.AsJson;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParameterConvertersBehaviour {

    private static String NAN = new DecimalFormatSymbols().getNaN();
    private static String POSITIVE_INFINITY = new DecimalFormatSymbols().getInfinity();
    private static String NEGATIVE_INFINITY = "-" + POSITIVE_INFINITY;

    private static final String JSON_AS_STRING = "{\"string\":\"String1\",\"integer\":2,\"stringList\":[\"String2\",\"String3\"],"
            + "\"integerList\":[3,4]}";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @SuppressWarnings("unchecked")
    @Test
    public void shouldDefineDefaultConverters() {
        LoadFromClasspath resourceLoader = new LoadFromClasspath();
        TableTransformers tableTransformers = new TableTransformers();
        ParameterControls parameterControls = new ParameterControls();
        ParameterConverters converters = new ParameterConverters(resourceLoader, parameterControls, tableTransformers,
                true);
        ParameterConverter<?>[] defaultConverters = converters.defaultConverters(resourceLoader, parameterControls,
                tableTransformers, Locale.ENGLISH, ",");
        assertThatDefaultConvertersInclude(defaultConverters, BooleanConverter.class, NumberConverter.class,
                StringListConverter.class, DateConverter.class, EnumConverter.class, ExamplesTableConverter.class,
                ExamplesTableParametersConverter.class);
    }

    private void assertThatDefaultConvertersInclude(ParameterConverter<?>[] defaultConverters,
            Class<? extends ParameterConverter<?>>... converterTypes) {
        for (Class<? extends ParameterConverter<?>> type : converterTypes) {
            boolean found = false;
            for (ParameterConverter<?> converter : defaultConverters) {
                if (converter.getClass().isAssignableFrom(type)) {
                    found = true;
                }
            }
            if (!found) {
                new AssertionError("Converter " + type + " should be in the list of default converters");
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
        ParameterConverter<Number> converter = new NumberConverter(NumberFormat.getInstance(locale));
        assertConverterForLocale(converter, locale);
    }

    private void assertConverterForLocale(ParameterConverter<Number> converter, Locale locale) {
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, locale);
        assertThat(converter.convertValue("100,000", Integer.class), is((Number) 100000));
        assertThat(converter.convertValue("100,000", Long.class), is((Number) 100000L));
        assertThat(converter.convertValue("100,000.01", Float.class), is((Number) 100000.01f));
        assertThat(converter.convertValue("100,000.01", Double.class), is((Number) 100000.01d));
        assertThat(converter.convertValue("1,00,000.01", Double.class), is((Number) 100000.01d));
        assertThat(converter.convertValue("1,000,000.01", BigDecimal.class), is((Number) new BigDecimal("1000000.01")));
    }

    @Test
    public void shouldConvertValuesToNumbersWithEnglishNumberFormatInMultipleThreads() {
        final Locale locale = Locale.ENGLISH;
        final int threads = 3;
        final ParameterConverter<Number> converter = new NumberConverter(NumberFormat.getInstance(locale));
        final BlockingQueue<String> queue = new ArrayBlockingQueue<>(threads);
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
                new AssertionError(e.getMessage());
            }
        }

    }
    @Test
    public void shouldConvertValuesToNumbersWithFrenchNumberFormat() {
        Locale locale = Locale.FRENCH;
        ParameterConverter<Number> converter = new NumberConverter(NumberFormat.getInstance(locale));
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, locale);
        assertThat(converter.convertValue("100000,01", Float.class), is((Number) 100000.01f));
        assertThat(converter.convertValue("100000,01", Double.class), is((Number) 100000.01d));
    }

    @Test
    public void shouldConvertValuesToNumbersWithGermanNumberFormat() {
        Locale locale = Locale.GERMAN;
        ParameterConverter<Number> converter = new NumberConverter(NumberFormat.getInstance(locale));
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, locale);
        assertThat(converter.convertValue("1.000.000,01", BigDecimal.class), is((Number) new BigDecimal("1000000.01")));
    }

    private void assertThatAllNumberTypesAreAccepted(ParameterConverter<Number> converter) {
        Type[] numberTypes = {
                Byte.class,
                byte.class,
                Short.class,
                short.class,
                Integer.class,
                int.class,
                Float.class,
                float.class,
                Long.class,
                long.class,
                Double.class,
                double.class,
                BigInteger.class,
                BigDecimal.class,
                AtomicInteger.class,
                AtomicLong.class,
                Number.class
        };
        assertThatTypesAreAccepted(converter, numberTypes);
    }

    private void assertThatAllNumbersAreConverted(ParameterConverter<Number> converter, Locale locale) {
        DecimalFormatSymbols format = new DecimalFormatSymbols(locale);
        char dot = format.getDecimalSeparator();
        char minus = format.getMinusSign();
        assertThat(converter.convertValue("127", Byte.class), is((Number) Byte.MAX_VALUE));
        assertThat(converter.convertValue(minus + "128", byte.class), is((Number) Byte.MIN_VALUE));
        assertThat(converter.convertValue("32767", Short.class), is((Number) Short.MAX_VALUE));
        assertThat(converter.convertValue(minus + "32768", short.class), is((Number) Short.MIN_VALUE));
        assertThat(converter.convertValue("3", Integer.class), is((Number) 3));
        assertThat(converter.convertValue("3", int.class), is((Number) 3));
        assertThat(converter.convertValue("3" + dot + "0", Float.class), is((Number) 3.0f));
        assertThat(converter.convertValue("3" + dot + "0", float.class), is((Number) 3.0f));
        assertThat(converter.convertValue("3", Long.class), is((Number) 3L));
        assertThat(converter.convertValue("3", long.class), is((Number) 3L));
        assertThat(converter.convertValue("3" + dot + "0", Double.class), is((Number) 3.0d));
        assertThat(converter.convertValue("3" + dot + "0", double.class), is((Number) 3.0d));
        assertThat(converter.convertValue("3", BigInteger.class), is((Number) new BigInteger("3")));
        assertThat(converter.convertValue("3" + dot + "0", BigDecimal.class), is((Number) new BigDecimal("3.0")));
        assertThat(converter.convertValue("3" + dot + "00", BigDecimal.class), is((Number) new BigDecimal("3.00")));
        assertThat(converter.convertValue("30000000", BigDecimal.class), is((Number) new BigDecimal(30000000)));
        assertThat(converter.convertValue("3" + dot + "000", BigDecimal.class), is((Number) new BigDecimal("3.000")));
        assertThat(converter.convertValue("-3", BigDecimal.class), is((Number) new BigDecimal("-3")));
        assertThat(converter.convertValue("3", AtomicInteger.class).intValue(), is((Number) 3));
        assertThat(converter.convertValue("3", AtomicLong.class).longValue(), is((Number) 3L));
        assertThat(converter.convertValue("3", Number.class), is((Number) 3L));
    }

    @Test
    public void shouldFailToConvertInvalidNumbersWithNumberFormat() {
        expectedException.expect(ParameterConvertionFailed.class);
        expectedException.expectCause(CoreMatchers.<ParseException>instanceOf(ParseException.class));
        new NumberConverter().convertValue("abc", Long.class);
    }

    @Test
    public void shouldFailToConvertInvalidNumbersWithNumberFormat2()  {
        expectedException.expect(ParameterConvertionFailed.class);
        expectedException.expectCause(CoreMatchers.<NumberFormatException>instanceOf(NumberFormatException.class));
        new NumberConverter().convertValue("12.34.56", BigDecimal.class);
    }

    @Test
    public void shouldConvertNaNAndInfinityValuesToNumbers() {
        ParameterConverter<Number> converter = new NumberConverter();
        assertThat(converter.convertValue(NAN, Float.class), is((Number) Float.NaN));
        assertThat(converter.convertValue(POSITIVE_INFINITY, Float.class), is((Number) Float.POSITIVE_INFINITY));
        assertThat(converter.convertValue(NEGATIVE_INFINITY, Float.class), is((Number) Float.NEGATIVE_INFINITY));
        assertThat(converter.convertValue(NAN, Double.class), is((Number) Double.NaN));
        assertThat(converter.convertValue(POSITIVE_INFINITY, Double.class), is((Number) Double.POSITIVE_INFINITY));
        assertThat(converter.convertValue(NEGATIVE_INFINITY, Double.class), is((Number) Double.NEGATIVE_INFINITY));
    }

    @Test
    public void shouldConvertCommaSeparatedValuesToListOfNumbersWithDefaultFormat() {
        ParameterConverter<List<Number>> converter = new NumberListConverter();
        Type listOfNumbers = new TypeLiteral<List<Number>>(){}.getType();
        Type setOfNumbers = new TypeLiteral<Set<Number>>(){}.getType();
        assertThat(converter.accept(listOfNumbers), is(true));
        assertThat(converter.accept(setOfNumbers), is(false));
        List<Number> list = converter.convertValue("3, 0.5, 6.1f, 8.00", listOfNumbers);
        assertThatCollectionIs(list, 3L, 0.5, 6.1, 8L);
    }

    @Test
    public void shouldConvertCommaSeparatedValuesToSetOfNumbersWithDefaultFormat() {
        ParameterConverters parameterConverters = new ParameterConverters();
        Type setOfNumbers = new TypeLiteral<Set<Number>>(){}.getType();
        Set<Number> set = (Set<Number>)parameterConverters.convert("3, 0.5, 6.1, 8.00", setOfNumbers);
        assertThatCollectionIs(set, 3L, 0.5, 6.1d, 8L);
    }

    @Test
    public void shouldConvertCommaSeparatedValuesToListOfNumbersWithCustomFormat() {
        ParameterConverter<List<Number>> converter = new NumberListConverter(new DecimalFormat("#,####"), " ");
        Type type = new TypeLiteral<List<Number>>(){}.getType();
        List<Number> list = converter.convertValue("3,000 0.5 6.1f 8.00", type);
        assertThatCollectionIs(list, 3000L, 0.5, 6.1, 8L);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertCommaSeparatedValuesOfSpecificNumberTypes() {
        ParameterConverter converter = new NumberListConverter();

        Type doubleType = new TypeLiteral<List<Double>>(){}.getType();
        List<Double> doubles = (List<Double>) converter.convertValue(
                "3, 0.5, 0.0, 8.00, " + NAN + ", " + POSITIVE_INFINITY, doubleType);
        assertThatCollectionIs(doubles, 3.0, 0.5, 0.0, 8.0, Double.NaN, Double.POSITIVE_INFINITY);

        Type floatType = new TypeLiteral<List<Float>>(){}.getType();
        List<Float> floats = (List<Float>) converter.convertValue(
                "3, 0.5, 0.0, 8.00, " + NAN + ", " + NEGATIVE_INFINITY, floatType);
        assertThatCollectionIs(floats, 3.0f, 0.5f, 0.0f, 8.0f, Float.NaN, Float.NEGATIVE_INFINITY);

        Type longType = new TypeLiteral<List<Long>>(){}.getType();
        List<Long> longs = (List<Long>) converter.convertValue("3, 0, 8", longType);
        assertThatCollectionIs(longs, 3L, 0L, 8L);

        Type intType = new TypeLiteral<List<Integer>>(){}.getType();
        List<Integer> ints = (List<Integer>) converter.convertValue("3, 0, 8", intType);
        assertThatCollectionIs(ints, 3, 0, 8);
    }

    @Test
    public void shouldFailToConvertCommaSeparatedValuesOfInvalidNumbers() {
        expectedException.expect(ParameterConvertionFailed.class);
        new NumberListConverter().convertValue("3x, x.5", new TypeLiteral<List<Number>>(){}.getType());
    }

    @Test
    public void shouldConvertCommaSeparatedValuesToListOfStrings() {
        ParameterConverter<List<String>> converter = new StringListConverter();
        Type listOfStrings = new TypeLiteral<List<String>>(){}.getType();
        Type listOfNumbers = new TypeLiteral<List<Number>>(){}.getType();
        Type setOfNumbers = new TypeLiteral<Set<Number>>(){}.getType();
        assertThat(converter.accept(listOfStrings), is(true));
        assertThat(converter.accept(listOfNumbers), is(false));
        assertThat(converter.accept(setOfNumbers), is(false));
        assertThatCollectionIs(converter.convertValue("a, string ", listOfStrings), "a", "string");
        assertThatCollectionIs(converter.convertValue(" ", listOfStrings));
    }

    @Test
    public void shouldConvertDateWithDefaultFormat() throws ParseException {
        ParameterConverter<Date> converter = new DateConverter();
        Type type = Date.class;
        assertThatTypesAreAccepted(converter, type);
        String date = "01/01/2010";
        assertThat(converter.convertValue(date, type), is(DateConverter.DEFAULT_FORMAT.parse(date)));
    }

    @Test
    public void shouldConvertDateWithCustomFormat() throws ParseException {
        DateFormat customFormat = new SimpleDateFormat("yyyy-MM-dd");
        ParameterConverter<Date> converter = new DateConverter(customFormat);
        Type type = Date.class;
        assertThatTypesAreAccepted(converter, type);
        String date = "2010-01-01";
        assertThat(converter.convertValue(date, type), is(customFormat.parse(date)));
    }

    @Test
    public void shouldFailToConvertDateWithInvalidFormat() {
        expectedException.expect(ParameterConvertionFailed.class);
        new DateConverter().convertValue("dd+MM+yyyy", Date.class);
    }

    @Test
    public void shouldConvertMultilineTable() {
        ParameterConverter<ExamplesTable> converter = new ExamplesTableConverter(
                new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers()));
        Type type = ExamplesTable.class;
        assertThatTypesAreAccepted(converter, type);
        String value = "|col1|col2|\n|row11|row12|\n|row21|row22|\n";
        ExamplesTable table = converter.convertValue(value, type);
        assertThat(table.getRowCount(), is(2));
        Map<String, String> row1 = table.getRow(0);
        assertThat(row1.get("col1"), is("row11"));
        assertThat(row1.get("col2"), is("row12"));
        Map<String, String> row2 = table.getRow(1);
        assertThat(row2.get("col1"), is("row21"));
        assertThat(row2.get("col2"), is("row22"));
    }

    @Test
    public void shouldConvertMultilineTableToParameters() {
        ParameterConverter converter = new ExamplesTableParametersConverter(
                new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers()));
        Type type = new TypeLiteral<List<MyParameters>>(){}.getType();
        assertThatTypesAreAccepted(converter, type);
        String value = "|col1|col2|\n|row11|row12|\n|row21|row22|\n";
        @SuppressWarnings("unchecked")
        List<MyParameters> parameters = (List<MyParameters>) converter.convertValue(value, type);
        assertThat(parameters.size(), is(2));
        MyParameters row1 = parameters.get(0);
        assertThat(row1.col1, is("row11"));
        assertThat(row1.col2, is("row12"));
        MyParameters row2 = parameters.get(1);
        assertThat(row2.col1, is("row21"));
        assertThat(row2.col2, is("row22"));
    }

    @Test
    public void shouldConvertSinglelineTableToParameters() {
        ParameterConverter converter = new ExamplesTableParametersConverter(
                new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers()));
        Type type = MyParameters.class;
        assertThatTypesAreAccepted(converter, type);
        String value = "|col1|col2|\n|row11|row12|\n";
        MyParameters parameters = (MyParameters) converter.convertValue(value, type);
        assertThat(parameters.col1, is("row11"));
        assertThat(parameters.col2, is("row12"));
    }

    @Test
    public void shouldConvertParameterFromMethodReturningValue() throws IntrospectionException {
        Method method = SomeSteps.methodFor("aMethodReturningExamplesTable");
        ParameterConverter converter = new MethodReturningConverter(method, new SomeSteps());
        assertThatTypesAreAccepted(converter, method.getReturnType());
        String value = "|col1|col2|\n|row11|row12|\n|row21|row22|\n";
        ExamplesTable table = (ExamplesTable) converter.convertValue(value, ExamplesTable.class);
        assertThat(table.getRowCount(), is(2));
        Map<String, String> row1 = table.getRow(0);
        assertThat(row1.get("col1"), is("row11"));
        assertThat(row1.get("col2"), is("row12"));
        Map<String, String> row2 = table.getRow(1);
        assertThat(row2.get("col1"), is("row21"));
        assertThat(row2.get("col2"), is("row22"));
    }

    @Test
    public void shouldFailToConvertParameterFromFailingMethodReturningValue() throws IntrospectionException {
        expectedException.expect(ParameterConvertionFailed.class);
        Method method = SomeSteps.methodFor("aFailingMethodReturningExamplesTable");
        ParameterConverter converter = new MethodReturningConverter(method, new SomeSteps());
        String value = "|col1|col2|\n|row11|row12|\n|row21|row22|\n";
        converter.convertValue(value, ExamplesTable.class);
    }

    @Test
    public void shouldFailToConvertToUnknownType() {
        expectedException.expect(ParameterConvertionFailed.class);
        new ParameterConverters(new LoadFromClasspath(), new TableTransformers()).convert("abc", WrongType.class);
    }

    @Test
    public void shouldConvertEnum() {
        ParameterConverter<Enum<?>> converter = new EnumConverter();
        Type type = SomeEnum.class;
        assertThatTypesAreAccepted(converter, type);
        assertThat(converter.convertValue("ONE", type), is((Enum)SomeEnum.ONE));
    }

    @Test
    public void shouldConvertEnumFluently() {
        ParameterConverter<Enum<?>> converter = new FluentEnumConverter();
        Type type = SomeEnum.class;
        assertThat(converter.accept(type), is(true));
        assertThat(converter.convertValue("multiple words and 1 number", type), is((Enum)SomeEnum.MULTIPLE_WORDS_AND_1_NUMBER));
    }

    @Test
    public void shouldFailToConvertEnumForValueNotDefined() {
        expectedException.expect(ParameterConvertionFailed.class);
        new EnumConverter().convertValue("FOUR", SomeEnum.class);
    }

    @Test
    public void shouldConvertEnumList() {
        ParameterConverter converter = new EnumListConverter();
        Type type = new TypeLiteral<List<SomeEnum>>(){}.getType();

        assertThat(converter.accept(type), is(true));
        List<Enum> list = (List<Enum>)converter.convertValue("ONE,TWO,THREE", type);
        assertThatCollectionIs(list, SomeEnum.ONE, SomeEnum.TWO, SomeEnum.THREE);
    }

    @Test
    public void shouldConvertBoolean() {
        ParameterConverter<Boolean> converter = new BooleanConverter();
        Type type = Boolean.TYPE;
        assertThatTypesAreAccepted(converter, type, Boolean.class);
        assertThat(converter.convertValue("true", type), is(true));
        assertThat(converter.convertValue("false", type), is(false));
        assertThat(converter.convertValue("whatever", type), is(false));
    }

    @Test
    public void shouldConvertBooleanWithCustomValues() {
        ParameterConverter<Boolean> converter = new BooleanConverter("ON", "OFF");
        Type type = Boolean.TYPE;
        assertThatTypesAreAccepted(converter, type, Boolean.class);
        assertThat(converter.convertValue("ON", type), is(true));
        assertThat(converter.convertValue("OFF", type), is(false));
        assertThat(converter.convertValue("whatever", type), is(false));
    }

    @Test
    public void shouldConvertBooleanList() {
        ParameterConverter<List<Boolean>> converter = new BooleanListConverter();
        Type type = new TypeLiteral<List<Boolean>>(){}.getType();
        assertThat(converter.accept(type), is(true));
        List<Boolean> list = converter.convertValue("true,false,true", type);
        assertThatCollectionIs(list, true, false, true);
    }

    @Test
    public void shouldNotModifyListOfConvertersFromOriginalParameterConvertersWhenCreatingNewInstance() {
        expectedException.expect(ParameterConvertionFailed.class);
        ParameterConverters original = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        original.newInstanceAdding(new FooToBarParameterConverter());
        original.convert("foo", Bar.class);
    }

    @Test
    public void shouldConvertToCustomObjectUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        assertThat((Bar)parameterConverters.convert("foo", Bar.class), is(Bar.INSTANCE));
    }

    @Test
    public void shouldConvertToListOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<List<Bar>>(){}.getType();
        List<Bar> list = (List<Bar>)parameterConverters.convert("foo", type);
        assertThatCollectionIs(list, Bar.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertToLinkedListOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<LinkedList<Bar>>(){}.getType();
        assertThatCollectionIs((LinkedList<Bar>) parameterConverters.convert("foo", type), Bar.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertToSortedSetOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<SortedSet<Bar>>(){}.getType();
        Set<Bar> set  = (Set<Bar>) parameterConverters.convert("foo", type);
        assertThatCollectionIs(set, Bar.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertToNavigableSetOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<NavigableSet<Bar>>(){}.getType();
        Set<Bar> set  = (Set<Bar>) parameterConverters.convert("foo", type);
        assertThatCollectionIs(set, Bar.INSTANCE);
    }

    @Test
    public void shouldNotConvertToAnyCollectionOfCustomObjectsUsingCustomConverter() {
        expectedException.expect(ParameterConvertionFailed.class);
        expectedException.expectMessage(
                "No parameter converter for java.util.Collection<org.jbehave.core.steps.ParameterConvertersBehaviour$Bar>");
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<Collection<Bar>>(){}.getType();
        parameterConverters.convert("foo", type);
    }

    @Test
    public void shouldNotConvertToListOfCustomObjectsWhenElementConverterIsNotAdded() {
        expectedException.expect(ParameterConvertionFailed.class);
        expectedException.expectMessage(
                "No parameter converter for java.util.List<org.jbehave.core.steps.ParameterConvertersBehaviour$Bar>");
        ParameterConverters parameterConverters = new ParameterConverters(new TableTransformers());
        Type type = new TypeLiteral<List<Bar>>(){}.getType();
        parameterConverters.convert("foo", type);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertEmptyStringToEmptyCollection() {
        ParameterConverters parameterConverters = new ParameterConverters();
        Type type = new TypeLiteral<List<Boolean>>(){}.getType();
        List<Boolean> list = (List<Boolean>) parameterConverters.convert("", type);
        assertThatCollectionIs(list);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertBlankStringToEmptyCollection() {
        ParameterConverters parameterConverters = new ParameterConverters();
        Type type = new TypeLiteral<Set<Number>>(){}.getType();
        Set<Number> set = (Set<Number>) parameterConverters.convert(" \t\n\r", type);
        assertThatCollectionIs(set);
    }

    @Test
    public void shouldCreateJsonFromStringInput() {
        // Given
        ParameterConverters.JsonFactory factory = new ParameterConverters.JsonFactory();

        // When
        MyJsonDto json = (MyJsonDto) factory.createJson(JSON_AS_STRING, MyJsonDto.class);

        // Then
        assertThat(new Gson().toJson(json), equalTo(JSON_AS_STRING));
    }

    @Test
    public void shouldCreateJsonFromResourceInput() {
        // Given
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ParameterConverters.JsonFactory factory = new ParameterConverters.JsonFactory(resourceLoader);

        // When
        String resourcePath = "/path/to/json";
        when(resourceLoader.loadResourceAsText(resourcePath)).thenReturn(JSON_AS_STRING);
        MyJsonDto json = (MyJsonDto) factory.createJson(resourcePath, MyJsonDto.class);

        // Then
        assertThat(new Gson().toJson(json), equalTo(JSON_AS_STRING));
    }

    @Test
    public void shouldMapJsonToType() {
        // Given
        ParameterConverters.JsonFactory factory = new ParameterConverters.JsonFactory();

        // When
        String jsonAsString = "{\"string\":\"11\",\"integer\":22,\"stringList\":[\"1\",\"1\"],\"integerList\":[2,2]}";
        MyJsonDto jsonDto = (MyJsonDto) factory.createJson(jsonAsString, MyJsonDto.class);

        // Then
        assertThat(jsonDto.string, equalTo("11"));
        assertThat(jsonDto.integer, equalTo(22));
        assertThat(jsonDto.stringList, equalTo(asList("1", "1")));
        assertThat(jsonDto.integerList, equalTo(asList(2, 2)));
    }

    @Test
    public void shouldMapListOfJsonsToType() {
        // Given
        ParameterConverters.JsonFactory factory = new ParameterConverters.JsonFactory();

        // When
        String jsonAsString = "{\"string\":\"11\",\"integer\":22,\"stringList\":[\"1\",\"1\"],\"integerList\":[2,2]}";
        String listOfJsonsAsString = String.format("[%s, %s]", jsonAsString, jsonAsString);
        MyJsonDto[] jsonList = (MyJsonDto[]) factory.createJson(listOfJsonsAsString, MyJsonDto[].class);

        // Then
        for (MyJsonDto jsonDto : jsonList) {
            assertThat(jsonDto.string, equalTo("11"));
            assertThat(jsonDto.integer, equalTo(22));
            assertThat(jsonDto.stringList, equalTo(asList("1", "1")));
            assertThat(jsonDto.integerList, equalTo(asList(2, 2)));
        }
    }

    @Test
    public void shouldPutNullsIfValuesOfObjectNotFoundInJson() {
        // Given
        ParameterConverters.JsonFactory factory = new ParameterConverters.JsonFactory();

        // When
        String jsonAsString = "{\"integer\":22,\"stringList\":[\"1\",\"1\"]}";
        MyJsonDto jsonDto = (MyJsonDto) factory.createJson(jsonAsString, MyJsonDto.class);

        // Then
        assertThat(jsonDto.string, equalTo(null));
        assertThat(jsonDto.integer, equalTo(22));
        assertThat(jsonDto.stringList, equalTo(asList("1", "1")));
        assertThat(jsonDto.integerList, equalTo(null));
    }

    @Test
    public void shouldPutAllNullsIfNoJsonArgumentsMatched() {
        // Given
        ParameterConverters.JsonFactory factory = new ParameterConverters.JsonFactory();

        // When
        String jsonAsString = "{\"string2\":\"11\",\"integer2\":22,\"stringList2\":[\"1\",\"1\"],\"integerList2\":[2,2]}";
        MyJsonDto jsonDto = (MyJsonDto) factory.createJson(jsonAsString, MyJsonDto.class);

        // Then
        assertThat(jsonDto.string, equalTo(null));
        assertThat(jsonDto.integer, equalTo(null));
        assertThat(jsonDto.stringList, equalTo(null));
        assertThat(jsonDto.integerList, equalTo(null));
    }

    @Test
    public void shouldNotBeEqualJsonWithWhitespaces() {
        // Given
        ParameterConverters.JsonFactory factory = new ParameterConverters.JsonFactory();

        // When
        String jsonAsString = "{ \"string\" : \"11\" , \"integer\" : 22 , \"stringList\" : [ \"1\" , \"1\" ] , "
                + "\"integerList\" : [ 2 , 2 ] }";
        MyJsonDto jsonDto = (MyJsonDto) factory.createJson(jsonAsString, MyJsonDto.class);

        // Then
        assertThat(new Gson().toJson(jsonDto), not(equalTo(jsonAsString)));
    }

    @Test
    public void shouldBeEqualDtosConvertedFromJsonWithWhitespaces() {
        // Given
        ParameterConverters.JsonFactory factory = new ParameterConverters.JsonFactory();

        // When
        String jsonAsString = "{ \"string\" : \"11\" , \"integer\" : 22 , \"stringList\" : [ \"1\" , \"1\" ] , "
                + "\"integerList\" : [ 2 , 2 ] }";
        MyJsonDto convertedJsonDto = (MyJsonDto) factory.createJson(jsonAsString, MyJsonDto.class);
        MyJsonDto createdJsonDto = new MyJsonDto("11", 22, asList("1", "1"), asList(2, 2));

        // Then
        assertThat(createdJsonDto.getString(), equalTo(convertedJsonDto.getString()));
        assertThat(createdJsonDto.getInteger(), equalTo(convertedJsonDto.getInteger()));
        assertThat(createdJsonDto.getStringList(), equalTo(convertedJsonDto.getStringList()));
        assertThat(createdJsonDto.getIntegerList(), equalTo(convertedJsonDto.getIntegerList()));
    }

    @Test
    public void shouldAceeptParameterizedTypesAutomatically () {
        ParameterConverter<Set<Bar>> parameterizedTypeConverter = new AbstractParameterConverter<Set<Bar>>() {
            @Override
            public Set<Bar> convertValue(String value, Type type) {
                throw new IllegalStateException("Not implemented");
            }
        };
        assertThat(parameterizedTypeConverter.accept(new TypeLiteral<Set<Bar>>(){}.getType()), is(true));
        assertThat(parameterizedTypeConverter.accept(new TypeLiteral<Set<Number>>(){}.getType()), is(false));
        assertThat(parameterizedTypeConverter.accept(new TypeLiteral<Set>(){}.getType()), is(false));
        assertThat(parameterizedTypeConverter.accept(new TypeLiteral<List<Bar>>(){}.getType()), is(false));
    }

    @AsJson
    public static class MyJsonDto {

        private String string;
        private Integer integer;
        private List<String> stringList;
        private List<Integer> integerList;

        public String getString() {
            return string;
        }

        public Integer getInteger() {
            return integer;
        }

        public List<String> getStringList() {
            return stringList;
        }

        public List<Integer> getIntegerList() {
            return integerList;
        }

        public MyJsonDto(String string, Integer integer, List<String> stringList, List<Integer> integerList) {
            this.string = string;
            this.integer = integer;
            this.stringList = stringList;
            this.integerList = integerList;
        }
    }

    private <T> void assertThatCollectionIs(Collection<T> collection, T... expected) {
        if ( expected.length > 0 ) {
            assertThat(collection, containsInAnyOrder(expected));
        } else {
            assertThat(collection, Matchers.<T>empty());
        }
    }

    private static void assertThatTypesAreAccepted(ParameterConverter<?> converter, Type... types) {
        for (Type type : types) {
            assertThat(converter.accept(type), is(true));
        }
        assertThat(converter.accept(WrongType.class), is(false));
        assertThat(converter.accept(mock(Type.class)), is(false));
    }

    static class WrongType {
    }

    static class Bar implements Comparable<Bar> {

        private static Bar INSTANCE = new Bar();

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Bar;
        }

        @Override
        public int compareTo(Bar o) {
            return 0;
        }
    }

    private class FooToBarParameterConverter extends AbstractParameterConverter<Bar> {
        @Override
        public Bar convertValue(String value, Type type) {
            return new Bar();
        }
    }

}
