package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.hamcrest.CoreMatchers;
import org.jbehave.core.io.LoadFromClasspath;
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
                fail("Converter " + type + " should be in the list of default converters");
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
        assertEquals(100000, converter.convertValue("100,000", Integer.class));
        assertEquals(100000L, converter.convertValue("100,000", Long.class));
        assertEquals(100000.01f, converter.convertValue("100,000.01", Float.class));
        assertEquals(100000.01d, converter.convertValue("100,000.01", Double.class));
        assertEquals(100000.01d, converter.convertValue("1,00,000.01", Double.class)); //Hindi style
        assertEquals(new BigDecimal("1000000.01"), converter.convertValue("1,000,000.01", BigDecimal.class));
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
                fail(e.getMessage());
            }
        }

    }
    @Test
    public void shouldConvertValuesToNumbersWithFrenchNumberFormat() {
        Locale locale = Locale.FRENCH;
        ParameterConverter<Number> converter = new NumberConverter(NumberFormat.getInstance(locale));
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, locale);
        assertEquals(100000.01f, converter.convertValue("100000,01", Float.class));
        assertEquals(100000.01d, converter.convertValue("100000,01", Double.class));
    }

    @Test
    public void shouldConvertValuesToNumbersWithGermanNumberFormat() {
        Locale locale = Locale.GERMAN;
        ParameterConverter<Number> converter = new NumberConverter(NumberFormat.getInstance(locale));
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, locale);
        assertEquals(new BigDecimal("1000000.01"), converter.convertValue("1.000.000,01", BigDecimal.class));
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
        assertEquals(Byte.MAX_VALUE, converter.convertValue("127", Byte.class));
        assertEquals(Byte.MIN_VALUE, converter.convertValue(minus + "128", byte.class));
        assertEquals(Short.MAX_VALUE, converter.convertValue("32767", Short.class));
        assertEquals(Short.MIN_VALUE, converter.convertValue(minus + "32768", short.class));
        assertEquals(3, converter.convertValue("3", Integer.class));
        assertEquals(3, converter.convertValue("3", int.class));
        assertEquals(3.0f, converter.convertValue("3" + dot + "0", Float.class));
        assertEquals(3.0f, converter.convertValue("3" + dot + "0", float.class));
        assertEquals(3L, converter.convertValue("3", Long.class));
        assertEquals(3L, converter.convertValue("3", long.class));
        assertEquals(3.0d, converter.convertValue("3" + dot + "0", Double.class));
        assertEquals(3.0d, converter.convertValue("3" + dot + "0", double.class));
        assertEquals(new BigInteger("3"), converter.convertValue("3", BigInteger.class));
        assertEquals(new BigDecimal("3.0"), converter.convertValue("3" + dot + "0", BigDecimal.class));
        assertEquals(new BigDecimal("3.00"), converter.convertValue("3" + dot + "00", BigDecimal.class)); // currency
        assertEquals(new BigDecimal(30000000), converter.convertValue("30000000", BigDecimal.class)); // 7 or more digits
        assertEquals(new BigDecimal("3.000"), converter.convertValue("3" + dot + "000", BigDecimal.class));  // something else!
        assertEquals(new BigDecimal("-3"), converter.convertValue("-3", BigDecimal.class)); // negative
        assertEquals(3, converter.convertValue("3", AtomicInteger.class).intValue());
        assertEquals(3L, converter.convertValue("3", AtomicLong.class).longValue());
        assertEquals(3L, converter.convertValue("3", Number.class));
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
        assertEquals(Float.NaN, converter.convertValue(NAN, Float.class));
        assertEquals(Float.POSITIVE_INFINITY, converter.convertValue(POSITIVE_INFINITY, Float.class));
        assertEquals(Float.NEGATIVE_INFINITY, converter.convertValue(NEGATIVE_INFINITY, Float.class));
        assertEquals(Double.NaN, converter.convertValue(NAN, Double.class));
        assertEquals(Double.POSITIVE_INFINITY, converter.convertValue(POSITIVE_INFINITY, Double.class));
        assertEquals(Double.NEGATIVE_INFINITY, converter.convertValue(NEGATIVE_INFINITY, Double.class));
    }

    @Test
    public void shouldConvertCommaSeparatedValuesToListOfNumbersWithDefaultFormat() {
        ParameterConverter<List<Number>> converter = new NumberListConverter();
        Type listOfNumbers = new TypeLiteral<List<Number>>(){}.getType();
        Type setOfNumbers = new TypeLiteral<Set<Number>>(){}.getType();
        assertTrue(converter.accept(listOfNumbers));
        assertFalse(converter.accept(setOfNumbers));
        List<Number> list = converter.convertValue("3, 0.5, 6.1f, 8.00", listOfNumbers);
        assertEquals(asList(3L, 0.5, 6.1, 8L), list);
    }

    @Test
    public void shouldConvertCommaSeparatedValuesToSetOfNumbersWithDefaultFormat() {
        ParameterConverters parameterConverters = new ParameterConverters();
        Type setOfNumbers = new TypeLiteral<Set<Number>>(){}.getType();
        assertEquals(new HashSet<>(asList(3L, 0.5, 6.1, 8L)),
                parameterConverters.convert("3, 0.5, 6.1f, 8.00", setOfNumbers));
    }

    @Test
    public void shouldConvertCommaSeparatedValuesToListOfNumbersWithCustomFormat() {
        ParameterConverter<List<Number>> converter = new NumberListConverter(new DecimalFormat("#,####"), " ");
        Type type = new TypeLiteral<List<Number>>(){}.getType();
        List<Number> list = converter.convertValue("3,000 0.5 6.1f 8.00", type);
        assertEquals(asList(3000L, 0.5, 6.1, 8L), list);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertCommaSeparatedValuesOfSpecificNumberTypes() {
        ParameterConverter converter = new NumberListConverter();
        Type doublesType = new TypeLiteral<List<Double>>(){}.getType();

        List<Double> doubles = (List<Double>) converter.convertValue(
                "3, 0.5, 0.0, 8.00, " + NAN + ", " + POSITIVE_INFINITY, doublesType);
        assertEquals(asList(3.0, 0.5, 0.0, 8.0, Double.NaN, Double.POSITIVE_INFINITY), doubles);

        Type floatsType = new TypeLiteral<List<Float>>(){}.getType();
        List<Float> floats = (List<Float>) converter.convertValue(
                "3, 0.5, 0.0, 8.00, " + NAN + ", " + NEGATIVE_INFINITY, floatsType);
        assertEquals(asList(3.0f, 0.5f, 0.0f, 8.0f, Float.NaN, Float.NEGATIVE_INFINITY), floats);

        Type longsType = new TypeLiteral<List<Long>>(){}.getType();
        List<Long> longs = (List<Long>) converter.convertValue("3, 0, 8", longsType);
        assertEquals(asList(3L, 0L, 8L), longs);

        Type intsType = new TypeLiteral<List<Integer>>(){}.getType();
        List<Integer> ints = (List<Integer>) converter.convertValue("3, 0, 8", intsType);
        assertEquals(asList(3, 0, 8), ints);
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
        assertTrue(converter.accept(listOfStrings));
        assertFalse(converter.accept(listOfNumbers));
        assertFalse(converter.accept(setOfNumbers));
        assertEquals(asList("a", "string"), converter.convertValue("a, string ", listOfStrings));
        assertEquals(Collections.emptyList(), converter.convertValue(" ", listOfStrings));
    }

    @Test
    public void shouldConvertDateWithDefaultFormat() throws ParseException {
        ParameterConverter<Date> converter = new DateConverter();
        Type type = Date.class;
        assertThatTypesAreAccepted(converter, type);
        String date = "01/01/2010";
        assertEquals(DateConverter.DEFAULT_FORMAT.parse(date), converter.convertValue(date, type));
    }

    @Test
    public void shouldConvertDateWithCustomFormat() throws ParseException {
        DateFormat customFormat = new SimpleDateFormat("yyyy-MM-dd");
        ParameterConverter<Date> converter = new DateConverter(customFormat);
        Type type = Date.class;
        assertThatTypesAreAccepted(converter, type);
        String date = "2010-01-01";
        assertEquals(customFormat.parse(date), converter.convertValue(date, type));
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
        assertThat(table.getRowCount(), equalTo(2));
        Map<String, String> row1 = table.getRow(0);
        assertThat(row1.get("col1"), equalTo("row11"));
        assertThat(row1.get("col2"), equalTo("row12"));
        Map<String, String> row2 = table.getRow(1);
        assertThat(row2.get("col1"), equalTo("row21"));
        assertThat(row2.get("col2"), equalTo("row22"));
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
        assertThat(parameters.size(), equalTo(2));
        MyParameters row1 = parameters.get(0);
        assertThat(row1.col1, equalTo("row11"));
        assertThat(row1.col2, equalTo("row12"));
        MyParameters row2 = parameters.get(1);
        assertThat(row2.col1, equalTo("row21"));
        assertThat(row2.col2, equalTo("row22"));
    }

    @Test
    public void shouldConvertSinglelineTableToParameters() {
        ParameterConverter converter = new ExamplesTableParametersConverter(
                new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers()));
        Type type = MyParameters.class;
        assertThatTypesAreAccepted(converter, type);
        String value = "|col1|col2|\n|row11|row12|\n";
        MyParameters parameters = (MyParameters) converter.convertValue(value, type);
        assertEquals("row11", parameters.col1);
        assertEquals("row12", parameters.col2);
    }

    @Test
    public void shouldConvertParameterFromMethodReturningValue() throws IntrospectionException {
        Method method = SomeSteps.methodFor("aMethodReturningExamplesTable");
        ParameterConverter converter = new MethodReturningConverter(method, new SomeSteps());
        assertThatTypesAreAccepted(converter, method.getReturnType());
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
        assertEquals(SomeEnum.ONE, converter.convertValue("ONE", type));
    }

    @Test
    public void shouldConvertEnumFluently() {
        ParameterConverter<Enum<?>> converter = new FluentEnumConverter();
        Type type = SomeEnum.class;
        assertTrue(converter.accept(type));
        assertEquals(SomeEnum.MULTIPLE_WORDS_AND_1_NUMBER, converter.convertValue("multiple words and 1 number", type));
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
        assertTrue(converter.accept(type));
        assertEquals(asList(SomeEnum.ONE, SomeEnum.TWO, SomeEnum.THREE), converter.convertValue("ONE,TWO,THREE", type));
    }

    @Test
    public void shouldConvertBoolean() {
        ParameterConverter<Boolean> converter = new BooleanConverter();
        Type type = Boolean.TYPE;
        assertThatTypesAreAccepted(converter, type, Boolean.class);
        assertTrue(converter.convertValue("true", type));
        assertFalse(converter.convertValue("false", type));
        assertFalse(converter.convertValue("whatever", type));
    }

    @Test
    public void shouldConvertBooleanWithCustomValues() {
        ParameterConverter<Boolean> converter = new BooleanConverter("ON", "OFF");
        Type type = Boolean.TYPE;
        assertThatTypesAreAccepted(converter, type, Boolean.class);
        assertTrue(converter.convertValue("ON", type));
        assertFalse(converter.convertValue("OFF", type));
        assertFalse(converter.convertValue("whatever", type));
    }

    @Test
    public void shouldConvertBooleanList() {
        ParameterConverter<List<Boolean>> converter = new BooleanListConverter();
        Type type = new TypeLiteral<List<Boolean>>(){}.getType();
        assertTrue(converter.accept(type));
        List<Boolean> list = converter.convertValue("true,false,true", type);
        assertEquals(asList(true, false, true), list);
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
        assertEquals(new Bar(), parameterConverters.convert("foo", Bar.class));
    }

    @Test
    public void shouldConvertToListOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<List<Bar>>(){}.getType();
        assertEquals(singletonList(new Bar()), parameterConverters.convert("foo", type));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertToLinkedListOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<LinkedList<Bar>>(){}.getType();
        LinkedList<Bar> foo = (LinkedList<Bar>) parameterConverters.convert("foo", type);
        assertEquals(singletonList(new Bar()), foo);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertToSortedSetOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<SortedSet<Bar>>(){}.getType();
        TreeSet<Bar> foo  = (TreeSet<Bar>) parameterConverters.convert("foo", type);
        assertEquals(new TreeSet<>(singletonList(new Bar())), foo);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertToNavigableSetOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<NavigableSet<Bar>>(){}.getType();
        TreeSet<Bar> foo  = (TreeSet<Bar>) parameterConverters.convert("foo", type);
        assertEquals(new TreeSet<>(singletonList(new Bar())), foo);
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

    private static void assertThatTypesAreAccepted(ParameterConverter<?> converter, Type... types) {
        for (Type type : types) {
            assertTrue(converter.accept(type));
        }
        assertFalse(converter.accept(WrongType.class));
        assertFalse(converter.accept(mock(Type.class)));
    }

    static class WrongType {
    }

    private class Bar implements Comparable<Bar> {
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
