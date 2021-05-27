package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.beans.IntrospectionException;
import java.io.File;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.AsJson;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.ParameterConverters.FromStringParameterConverter;
import org.jbehave.core.steps.ParameterConverters.BooleanConverter;
import org.jbehave.core.steps.ParameterConverters.BooleanListConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.ParameterConverters.CurrencyConverter;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.EnumConverter;
import org.jbehave.core.steps.ParameterConverters.EnumListConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableParametersConverter;
import org.jbehave.core.steps.ParameterConverters.FileConverter;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.jbehave.core.steps.ParameterConverters.MethodReturningConverter;
import org.jbehave.core.steps.ParameterConverters.NumberConverter;
import org.jbehave.core.steps.ParameterConverters.NumberListConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConversionFailed;
import org.jbehave.core.steps.ParameterConverters.PatternConverter;
import org.jbehave.core.steps.ParameterConverters.StringListConverter;
import org.jbehave.core.steps.SomeSteps.MyParameters;
import org.junit.jupiter.api.Test;

class ParameterConvertersBehaviour {

    private static String NAN = new DecimalFormatSymbols().getNaN();
    private static String POSITIVE_INFINITY = new DecimalFormatSymbols().getInfinity();
    private static String NEGATIVE_INFINITY = "-" + POSITIVE_INFINITY;

    private static final String JSON_AS_STRING = "{\"string\":\"String1\",\"integer\":2,\"stringList\":[\"String2\",\"String3\"],"
            + "\"integerList\":[3,4]}";

    @SuppressWarnings("unchecked")
    @Test
    void shouldDefineDefaultConverters() {
        Keywords keywords = new LocalizedKeywords();
        LoadFromClasspath resourceLoader = new LoadFromClasspath();
        TableTransformers tableTransformers = new TableTransformers();
        ParameterControls parameterControls = new ParameterControls();
        ParameterConverters converters = new ParameterConverters(resourceLoader, parameterControls, tableTransformers,
                true);
        TableParsers tableParsers = new TableParsers(keywords, converters);
        ParameterConverter<?, ?>[] defaultConverters = converters.defaultConverters(keywords, resourceLoader, parameterControls,
                tableParsers, tableTransformers, Locale.ENGLISH, ",");
        assertThatDefaultConvertersInclude(defaultConverters, BooleanConverter.class, NumberConverter.class,
                StringListConverter.class,
                DateConverter.class,
                CurrencyConverter.class,
                PatternConverter.class,
                FileConverter.class,
                EnumConverter.class,
                ExamplesTableConverter.class,
                ExamplesTableParametersConverter.class);
    }

    private void assertThatDefaultConvertersInclude(ParameterConverter<?, ?>[] defaultConverters,
                                                    Class<? extends FromStringParameterConverter<?>>... converterTypes) {
        for (Class<? extends FromStringParameterConverter<?>> type : converterTypes) {
            boolean found = false;
            for (ParameterConverter<?, ?> converter : defaultConverters) {
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
    void shouldConvertValuesToNumbersWithDefaultNumberFormat() {
        NumberConverter converter = new NumberConverter();
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, ParameterConverters.DEFAULT_NUMBER_FORMAT_LOCAL);
    }

    @Test
    void shouldConvertValuesToNumbersWithEnglishNumberFormat() {
        Locale locale = Locale.ENGLISH;
        FromStringParameterConverter<Number> converter = new NumberConverter(NumberFormat.getInstance(locale));
        assertConverterForLocale(converter, locale);
    }

    private void assertConverterForLocale(FromStringParameterConverter<Number> converter, Locale locale) {
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
    void shouldConvertValuesToNumbersWithEnglishNumberFormatInMultipleThreads() {
        final Locale locale = Locale.ENGLISH;
        final int threads = 3;
        final FromStringParameterConverter<Number> converter = new NumberConverter(NumberFormat.getInstance(locale));
        final BlockingQueue<String> queue = new ArrayBlockingQueue<>(threads);
        Thread t1 = new Thread() {
            @Override
            public void run() {
                assertConverterForLocale(converter, locale);
                queue.add(Thread.currentThread().getName());
            }
        };
        Thread t2 = new Thread() {
            @Override
            public void run() {
                assertConverterForLocale(converter, locale);
                queue.add(Thread.currentThread().getName());
            }
        };
        Thread t3 = new Thread() {
            @Override
            public void run() {
                assertConverterForLocale(converter, locale);
                queue.add(Thread.currentThread().getName());
            }
        };

        t1.start();
        t2.start();
        t3.start();

        for (int i = 0;i < threads;i++) {
            try {
                System.out.println(queue.take() + " completed.");
            } catch (InterruptedException e) {
                new AssertionError(e.getMessage());
            }
        }

    }
    @Test
    void shouldConvertValuesToNumbersWithFrenchNumberFormat() {
        Locale locale = Locale.FRENCH;
        FromStringParameterConverter<Number> converter = new NumberConverter(NumberFormat.getInstance(locale));
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, locale);
        assertThat(converter.convertValue("100000,01", Float.class), is((Number) 100000.01f));
        assertThat(converter.convertValue("100000,01", Double.class), is((Number) 100000.01d));
    }

    @Test
    void shouldConvertValuesToNumbersWithGermanNumberFormat() {
        Locale locale = Locale.GERMAN;
        FromStringParameterConverter<Number> converter = new NumberConverter(NumberFormat.getInstance(locale));
        assertThatAllNumberTypesAreAccepted(converter);
        assertThatAllNumbersAreConverted(converter, locale);
        assertThat(converter.convertValue("1.000.000,01", BigDecimal.class), is((Number) new BigDecimal("1000000.01")));
    }

    private void assertThatAllNumberTypesAreAccepted(FromStringParameterConverter<Number> converter) {
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

    private void assertThatAllNumbersAreConverted(FromStringParameterConverter<Number> converter, Locale locale) {
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
    void shouldFailToConvertInvalidNumbersWithNumberFormat() {
        NumberConverter converter = new NumberConverter();
        ParameterConversionFailed exception = assertThrows(ParameterConversionFailed.class,
                () -> converter.convertValue("abc", Long.class));
        assertThat(exception.getCause(), instanceOf(ParseException.class));
    }

    @Test
    void shouldFailToConvertInvalidNumbersWithNumberFormat2()  {
        NumberConverter converter = new NumberConverter();
        ParameterConversionFailed exception = assertThrows(ParameterConversionFailed.class,
                () -> converter.convertValue("12.34.56", BigDecimal.class));
        assertThat(exception.getCause(), instanceOf(NumberFormatException.class));
    }

    @Test
    void shouldConvertNaNAndInfinityValuesToNumbers() {
        FromStringParameterConverter<Number> converter = new NumberConverter();
        assertThat(converter.convertValue(NAN, Float.class), is((Number) Float.NaN));
        assertThat(converter.convertValue(POSITIVE_INFINITY, Float.class), is((Number) Float.POSITIVE_INFINITY));
        assertThat(converter.convertValue(NEGATIVE_INFINITY, Float.class), is((Number) Float.NEGATIVE_INFINITY));
        assertThat(converter.convertValue(NAN, Double.class), is((Number) Double.NaN));
        assertThat(converter.convertValue(POSITIVE_INFINITY, Double.class), is((Number) Double.POSITIVE_INFINITY));
        assertThat(converter.convertValue(NEGATIVE_INFINITY, Double.class), is((Number) Double.NEGATIVE_INFINITY));
    }

    @Test
    void shouldConvertCommaSeparatedValuesToListOfNumbersWithDefaultFormat() {
        FromStringParameterConverter<List<Number>> converter = new NumberListConverter();
        Type listOfNumbers = new TypeLiteral<List<Number>>() {}.getType();
        Type setOfNumbers = new TypeLiteral<Set<Number>>() {}.getType();
        assertThat(converter.canConvertTo(listOfNumbers), is(true));
        assertThat(converter.canConvertTo(setOfNumbers), is(false));
        List<Number> list = converter.convertValue("3, 0.5, 6.1f, 8.00", listOfNumbers);
        assertThatCollectionIs(list, 3L, 0.5, 6.1, 8L);
    }

    @Test
    void shouldConvertCommaSeparatedValuesToSetOfNumbersWithDefaultFormat() {
        ParameterConverters parameterConverters = new ParameterConverters();
        Type setOfNumbers = new TypeLiteral<Set<Number>>() {}.getType();
        Set<Number> set = (Set<Number>)parameterConverters.convert("3, 0.5, 6.1, 8.00", setOfNumbers);
        assertThatCollectionIs(set, 3L, 0.5, 6.1d, 8L);
    }

    @Test
    void shouldConvertCommaSeparatedValuesToListOfNumbersWithCustomFormat() {
        DecimalFormat numberFormat = new DecimalFormat("#,####", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        FromStringParameterConverter<List<Number>> converter = new NumberListConverter(numberFormat, " ");
        Type type = new TypeLiteral<List<Number>>() {}.getType();
        List<Number> list = converter.convertValue("3,000 0.5 6.1f 8.00", type);
        assertThatCollectionIs(list, 3000L, 0.5, 6.1, 8L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldConvertCommaSeparatedValuesOfSpecificNumberTypes() {
        FromStringParameterConverter converter = new NumberListConverter();

        Type doubleType = new TypeLiteral<List<Double>>() {}.getType();
        List<Double> doubles = (List<Double>) converter.convertValue(
                "3, 0.5, 0.0, 8.00, " + NAN + ", " + POSITIVE_INFINITY, doubleType);
        assertThatCollectionIs(doubles, 3.0, 0.5, 0.0, 8.0, Double.NaN, Double.POSITIVE_INFINITY);

        Type floatType = new TypeLiteral<List<Float>>() {}.getType();
        List<Float> floats = (List<Float>) converter.convertValue(
                "3, 0.5, 0.0, 8.00, " + NAN + ", " + NEGATIVE_INFINITY, floatType);
        assertThatCollectionIs(floats, 3.0f, 0.5f, 0.0f, 8.0f, Float.NaN, Float.NEGATIVE_INFINITY);

        Type longType = new TypeLiteral<List<Long>>() {}.getType();
        List<Long> longs = (List<Long>) converter.convertValue("3, 0, 8", longType);
        assertThatCollectionIs(longs, 3L, 0L, 8L);

        Type intType = new TypeLiteral<List<Integer>>() {}.getType();
        List<Integer> ints = (List<Integer>) converter.convertValue("3, 0, 8", intType);
        assertThatCollectionIs(ints, 3, 0, 8);
    }

    @Test
    void shouldConvertCommaSeparatedValuesToArrayWithDefaultFormat() {
        ParameterConverters converters = new ParameterConverters();
        assertThat(converters.convert("1,2,3", int[].class), is(new int[] {1, 2, 3}));
    }

    @Test
    void shouldConvertEmptyStringToEmptyArray() {
        ParameterConverters converters = new ParameterConverters();
        assertThat(converters.convert("", int[].class), is(new int[0]));
    }

    @Test
    void shouldFailToConvertToArrayOfCustomObjectsIfNoConverterFound() {
        ParameterConverters converters = new ParameterConverters();
        ParameterConversionFailed exception = assertThrows(ParameterConversionFailed.class,
                () -> converters.convert("foo", Bar[].class));
        assertThat(exception.getMessage(), is(equalTo(
                "No parameter converter for class [Lorg.jbehave.core.steps.ParameterConvertersBehaviour$Bar;")));
    }

    @Test
    void shouldFailToConvertCommaSeparatedValuesOfInvalidNumbers() {
        NumberListConverter converter = new NumberListConverter();
        assertThrows(ParameterConversionFailed.class,
                () -> converter.convertValue("3x, x.5", new TypeLiteral<List<Number>>() {}.getType()));
    }

    @Test
    void shouldConvertCommaSeparatedValuesToListOfStrings() {
        FromStringParameterConverter<List<String>> converter = new StringListConverter();
        Type listOfStrings = new TypeLiteral<List<String>>() {}.getType();
        Type listOfNumbers = new TypeLiteral<List<Number>>() {}.getType();
        Type setOfNumbers = new TypeLiteral<Set<Number>>() {}.getType();
        assertThat(converter.canConvertTo(listOfStrings), is(true));
        assertThat(converter.canConvertTo(listOfNumbers), is(false));
        assertThat(converter.canConvertTo(setOfNumbers), is(false));
        assertThatCollectionIs(converter.convertValue("a, string ", listOfStrings), "a", "string");
        assertThatCollectionIs(converter.convertValue(" ", listOfStrings));
    }

    @Test
    void shouldConvertDateWithDefaultFormat() throws ParseException {
        FromStringParameterConverter<Date> converter = new DateConverter();
        Type type = Date.class;
        assertThatTypesAreAccepted(converter, type);
        String date = "01/01/2010";
        assertThat(converter.convertValue(date, type), is(DateConverter.DEFAULT_FORMAT.parse(date)));
    }

    @Test
    void shouldConvertDateWithCustomFormat() throws ParseException {
        DateFormat customFormat = new SimpleDateFormat("yyyy-MM-dd");
        FromStringParameterConverter<Date> converter = new DateConverter(customFormat);
        Type type = Date.class;
        assertThatTypesAreAccepted(converter, type);
        String date = "2010-01-01";
        assertThat(converter.convertValue(date, type), is(customFormat.parse(date)));
    }

    @Test
    void shouldFailToConvertDateWithInvalidFormat() {
        DateConverter dateConverter = new DateConverter();
        assertThrows(ParameterConversionFailed.class, () -> dateConverter.convertValue("dd+MM+yyyy", Date.class));
    }

    @Test
    void shouldConvertCurrency() {
        CurrencyConverter converter = new CurrencyConverter();
        Type type = Currency.class;
        assertThatTypesAreAccepted(converter, type);
        assertThat(converter.convertValue("USD", type), is(Currency.getInstance("USD")));
    }

    @Test
    void shouldConvertPattern() {
        PatternConverter converter = new PatternConverter();
        Type type = Pattern.class;
        assertThatTypesAreAccepted(converter, type);
        assertThat(converter.convertValue(".*", type).pattern(), is(Pattern.compile(".*").pattern()));
    }

    @Test
    void shouldConvertFile() {
        FileConverter converter = new FileConverter();
        Type type = File.class;
        assertThatTypesAreAccepted(converter, type);
        assertThat(converter.convertValue(".", type), is(new File(".")));
    }

    @Test
    void shouldConvertMultilineTable() {
        FromStringParameterConverter<ExamplesTable> converter = new ExamplesTableConverter(
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
    void shouldConvertMultilineTableToParameters() {
        FromStringParameterConverter converter = new ExamplesTableParametersConverter(
                new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers()));
        Type type = new TypeLiteral<List<MyParameters>>() {}.getType();
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
    void shouldConvertSinglelineTableToParameters() {
        FromStringParameterConverter converter = new ExamplesTableParametersConverter(
                new ExamplesTableFactory(new LoadFromClasspath(), new TableTransformers()));
        Type type = MyParameters.class;
        assertThatTypesAreAccepted(converter, type);
        String value = "|col1|col2|\n|row11|row12|\n";
        MyParameters parameters = (MyParameters) converter.convertValue(value, type);
        assertThat(parameters.col1, is("row11"));
        assertThat(parameters.col2, is("row12"));
    }

    @Test
    void shouldConvertParameterFromMethodReturningValue() throws IntrospectionException {
        Method method = SomeSteps.methodFor("aMethodReturningExamplesTable");
        FromStringParameterConverter converter = new MethodReturningConverter(method, new SomeSteps());
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
    void shouldFailToConvertParameterFromFailingMethodReturningValue() throws IntrospectionException {
        Method method = SomeSteps.methodFor("aFailingMethodReturningExamplesTable");
        FromStringParameterConverter converter = new MethodReturningConverter(method, new SomeSteps());
        String value = "|col1|col2|\n|row11|row12|\n|row21|row22|\n";
        assertThrows(ParameterConversionFailed.class, () -> converter.convertValue(value, ExamplesTable.class));
    }

    @Test
    void shouldFailToConvertToUnknownType() {
        ParameterConverters converters = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        assertThrows(ParameterConversionFailed.class, () -> converters.convert("abc", WrongType.class));
    }

    @Test
    void shouldConvertEnum() {
        FromStringParameterConverter<Enum<?>> converter = new EnumConverter();
        Type type = SomeEnum.class;
        assertThatTypesAreAccepted(converter, type);
        assertThat(converter.convertValue("ONE", type), is((Enum)SomeEnum.ONE));
    }

    @Test
    void shouldConvertEnumFluently() {
        FromStringParameterConverter<Enum<?>> converter = new FluentEnumConverter();
        Type type = SomeEnum.class;
        assertThat(converter.canConvertTo(type), is(true));
        assertThat(converter.convertValue("multiple words and 1 number", type), is((Enum)SomeEnum.MULTIPLE_WORDS_AND_1_NUMBER));
    }

    @Test
    void shouldFailToConvertEnumForValueNotDefined() {
        EnumConverter enumConverter = new EnumConverter();
        assertThrows(ParameterConversionFailed.class, () -> enumConverter.convertValue("FOUR", SomeEnum.class));
    }

    @Test
    void shouldConvertEnumList() {
        FromStringParameterConverter converter = new EnumListConverter();
        Type type = new TypeLiteral<List<SomeEnum>>() {}.getType();

        assertThat(converter.canConvertTo(type), is(true));
        List<Enum> list = (List<Enum>)converter.convertValue("ONE,TWO,THREE", type);
        assertThatCollectionIs(list, SomeEnum.ONE, SomeEnum.TWO, SomeEnum.THREE);
    }

    @Test
    void shouldConvertBoolean() {
        FromStringParameterConverter<Boolean> converter = new BooleanConverter();
        Type type = Boolean.TYPE;
        assertThatTypesAreAccepted(converter, type, Boolean.class);
        assertThat(converter.convertValue("true", type), is(true));
        assertThat(converter.convertValue("false", type), is(false));
        assertThat(converter.convertValue("whatever", type), is(false));
    }

    @Test
    void shouldConvertBooleanWithCustomValues() {
        FromStringParameterConverter<Boolean> converter = new BooleanConverter("ON", "OFF");
        Type type = Boolean.TYPE;
        assertThatTypesAreAccepted(converter, type, Boolean.class);
        assertThat(converter.convertValue("ON", type), is(true));
        assertThat(converter.convertValue("OFF", type), is(false));
        assertThat(converter.convertValue("whatever", type), is(false));
    }

    @Test
    void shouldConvertBooleanList() {
        FromStringParameterConverter<List<Boolean>> converter = new BooleanListConverter();
        Type type = new TypeLiteral<List<Boolean>>() {}.getType();
        assertThat(converter.canConvertTo(type), is(true));
        List<Boolean> list = converter.convertValue("true,false,true", type);
        assertThatCollectionIs(list, true, false, true);
    }

    @Test
    void shouldNotModifyListOfConvertersFromOriginalParameterConvertersWhenCreatingNewInstance() {
        ParameterConverters original = new ParameterConverters(new LoadFromClasspath(), new TableTransformers());
        original.newInstanceAdding(new FooToBarParameterConverter());
        assertThrows(ParameterConversionFailed.class, () -> original.convert("foo", Bar.class));
    }

    @Test
    void shouldConvertToCustomObjectUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        assertThat((Bar)parameterConverters.convert("foo", Bar.class), is(Bar.INSTANCE));
    }

    @Test
    void shouldConvertToListOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<List<Bar>>() {}.getType();
        List<Bar> list = (List<Bar>)parameterConverters.convert("foo", type);
        assertThatCollectionIs(list, Bar.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldConvertToLinkedListOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<LinkedList<Bar>>() {}.getType();
        assertThatCollectionIs((LinkedList<Bar>) parameterConverters.convert("foo", type), Bar.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldConvertToSortedSetOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<SortedSet<Bar>>() {}.getType();
        Set<Bar> set  = (Set<Bar>) parameterConverters.convert("foo", type);
        assertThatCollectionIs(set, Bar.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldConvertToNavigableSetOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<NavigableSet<Bar>>() {}.getType();
        Set<Bar> set  = (Set<Bar>) parameterConverters.convert("foo", type);
        assertThatCollectionIs(set, Bar.INSTANCE);
    }

    @Test
    void shouldNotConvertToAnyCollectionOfCustomObjectsUsingCustomConverter() {
        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath());
        parameterConverters.addConverters(new FooToBarParameterConverter());
        Type type = new TypeLiteral<Collection<Bar>>() {}.getType();
        ParameterConversionFailed exception = assertThrows(ParameterConversionFailed.class,
                () -> parameterConverters.convert("foo", type));
        assertThat(exception.getMessage(), is(equalTo("No parameter converter for "
                + "java.util.Collection<org.jbehave.core.steps.ParameterConvertersBehaviour$Bar>")));
    }

    @Test
    void shouldNotConvertToListOfCustomObjectsWhenElementConverterIsNotAdded() {
        ParameterConverters parameterConverters = new ParameterConverters(new TableTransformers());
        Type type = new TypeLiteral<List<Bar>>() {}.getType();
        ParameterConversionFailed exception = assertThrows(ParameterConversionFailed.class,
                () -> parameterConverters.convert("foo", type));
        assertThat(exception.getMessage(), is(equalTo(
                "No parameter converter for java.util.List<org.jbehave.core.steps.ParameterConvertersBehaviour$Bar>")));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldConvertEmptyStringToEmptyCollection() {
        ParameterConverters parameterConverters = new ParameterConverters();
        Type type = new TypeLiteral<List<Boolean>>() {}.getType();
        List<Boolean> list = (List<Boolean>) parameterConverters.convert("", type);
        assertThatCollectionIs(list);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldConvertBlankStringToEmptyCollection() {
        ParameterConverters parameterConverters = new ParameterConverters();
        Type type = new TypeLiteral<Set<Number>>() {}.getType();
        Set<Number> set = (Set<Number>) parameterConverters.convert(" \t\n\r", type);
        assertThatCollectionIs(set);
    }

    @Test
    void shouldCreateJsonFromStringInput() {
        // Given
        ParameterConverters.JsonFactory factory = new ParameterConverters.JsonFactory();

        // When
        MyJsonDto json = (MyJsonDto) factory.createJson(JSON_AS_STRING, MyJsonDto.class);

        // Then
        assertThat(new Gson().toJson(json), equalTo(JSON_AS_STRING));
    }

    @Test
    void shouldCreateJsonFromResourceInput() {
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
    void shouldMapJsonToType() {
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
    void shouldMapListOfJsonsToType() {
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
    void shouldPutNullsIfValuesOfObjectNotFoundInJson() {
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
    void shouldPutAllNullsIfNoJsonArgumentsMatched() {
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
    void shouldNotBeEqualJsonWithWhitespaces() {
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
    void shouldBeEqualDtosConvertedFromJsonWithWhitespaces() {
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
    void shouldAceeptParameterizedTypesAutomatically () {
        FromStringParameterConverter<Set<Bar>> parameterizedTypeConverter = new FromStringParameterConverter<Set<Bar>>() {
            @Override
            public Set<Bar> convertValue(String value, Type type) {
                throw new IllegalStateException("Not implemented");
            }
        };
        assertThat(parameterizedTypeConverter.canConvertTo(new TypeLiteral<Set<Bar>>() {}.getType()), is(true));
        assertThat(parameterizedTypeConverter.canConvertTo(new TypeLiteral<Set<Number>>() {}.getType()), is(false));
        assertThat(parameterizedTypeConverter.canConvertTo(new TypeLiteral<Set>() {}.getType()), is(false));
        assertThat(parameterizedTypeConverter.canConvertTo(new TypeLiteral<List<Bar>>() {}.getType()), is(false));
    }

    @Test
    void shouldConvertDuration() {
        assertThat(new ParameterConverters().convert("PT1S", Duration.class), is(Duration.ofSeconds(1)));
    }

    @Test
    void shouldConvertInstant() {
        assertThat(new ParameterConverters().convert("2019-07-04T21:50:35.00Z", Instant.class),
                is(Instant.ofEpochSecond(1562277035)));
    }

    @Test
    void shouldConvertLocalDateTime() {
        assertThat(new ParameterConverters().convert("2019-07-04T21:50:35.123", LocalDateTime.class),
                is(LocalDateTime.of(2019, 7, 4, 21, 50, 35, 123_000_000)));
    }

    @Test
    void shouldConvertLocalDate() {
        assertThat(new ParameterConverters().convert("2019-07-04", LocalDate.class), is(LocalDate.of(2019, 7, 4)));
    }

    @Test
    void shouldConvertLocalTime() {
        assertThat(new ParameterConverters().convert("21:50:35.123", LocalTime.class),
                is(LocalTime.of(21, 50, 35, 123_000_000)));
    }

    @Test
    void shouldConvertMonthDay() {
        assertThat(new ParameterConverters().convert("--07-04", MonthDay.class), is(MonthDay.of(7, 4)));
    }

    @Test
    void shouldConvertOffsetDateTime() {
        assertThat(new ParameterConverters().convert("2019-07-04T21:50:35.123Z", OffsetDateTime.class),
                is(OffsetDateTime.of(2019, 7, 4, 21, 50, 35, 123_000_000, ZoneOffset.UTC)));
    }

    @Test
    void shouldConvertOffsetTime() {
        assertThat(new ParameterConverters().convert("21:50:35.123Z", OffsetTime.class),
                is(OffsetTime.of(21, 50, 35, 123_000_000, ZoneOffset.UTC)));
    }

    @Test
    void shouldConvertPeriod() {
        assertThat(new ParameterConverters().convert("P1Y2M3D", Period.class), is(Period.of(1, 2, 3)));
    }

    @Test
    void shouldConvertYearMonth() {
        assertThat(new ParameterConverters().convert("2019-07", YearMonth.class), is(YearMonth.of(2019, 7)));
    }

    @Test
    void shouldConvertYear() {
        assertThat(new ParameterConverters().convert("2019", Year.class), is(Year.of(2019)));
    }

    @Test
    void shouldConvertZonedDateTime() {
        assertThat(new ParameterConverters().convert("2019-07-04T21:50:35.123Z", ZonedDateTime.class),
                is(ZonedDateTime.of(2019, 7, 4, 21, 50, 35, 123_000_000, ZoneOffset.UTC)));
    }

    @Test
    void shouldConvertZoneId() {
        assertThat(new ParameterConverters().convert("Europe/Minsk", ZoneId.class), is(ZoneId.of("Europe/Minsk")));
    }

    @Test
    void shouldConvertZoneOffset() {
        assertThat(new ParameterConverters().convert("+03:00", ZoneOffset.class), is(ZoneOffset.ofHours(3)));
    }

    @Test
    void shouldConvertEnumWithFluentEnumConverter() {
        ParameterConverters converters = new ParameterConverters().addConverters(new FluentEnumConverter());
        assertThat(converters.convert("ONE", SomeEnum.class), is(SomeEnum.ONE));
    }

    @Test
    void shouldConvertStringViaChainOfConverters() {
        ParameterConverters converters = new ParameterConverters();
        converters.addConverters(new FirstParameterConverter(), new SecondParameterConverter(),
                new ThirdParameterConverter());
        String input = "|key|\n|value|";
        Object convertedValue = converters.convert(input, ThirdConverterOutput.class);
        assertThat(convertedValue, instanceOf(ThirdConverterOutput.class));
        ThirdConverterOutput output = (ThirdConverterOutput) convertedValue;
        assertThat(output.getOutput(), is(input + "\nfirstsecondthird"));
    }

    @Test
    void shouldConvertFromStringUsingChainableParameterConverterIfDefaultDoesNotExist() {
        ParameterConverters converters = new ParameterConverters();
        converters.addConverters(new StringContainerConverter());
        String inputValue = "string value";
        Object convertedValue = converters.convert(inputValue, StringContainer.class);
        assertThat(convertedValue, instanceOf(StringContainer.class));
        StringContainer output = (StringContainer) convertedValue;
        assertThat(output.getOutput(), is(inputValue));
    }

    @Test
    void shouldCheckIfWeCanConvertFromType() {
        StringContainerConverter simpleChainableConverter = new StringContainerConverter();
        assertTrue(simpleChainableConverter.canConvertFrom(String.class));
        assertFalse(simpleChainableConverter.canConvertFrom(Number.class));

        ListToSetConverter collectionChainableConverter = new ListToSetConverter();
        assertTrue(collectionChainableConverter.canConvertFrom(new TypeLiteral<List<String>>() {}.getType()));
        assertFalse(collectionChainableConverter.canConvertFrom(new TypeLiteral<Set<String>>() {}.getType()));

        BooleanConverter simpleConverter = new BooleanConverter();
        assertTrue(simpleConverter.canConvertFrom(String.class));
        assertFalse(simpleConverter.canConvertFrom(Boolean.class));
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
        if (expected.length > 0) {
            assertThat(collection, containsInAnyOrder(expected));
        } else {
            assertThat(collection, Matchers.<T>empty());
        }
    }

    private static void assertThatTypesAreAccepted(FromStringParameterConverter<?> converter, Type... types) {
        for (Type type : types) {
            assertThat(converter.canConvertTo(type), is(true));
        }
        assertThat(converter.canConvertTo(WrongType.class), is(false));
        assertThat(converter.canConvertTo(mock(Type.class)), is(false));
        assertThat(converter.canConvertTo(new TypeLiteral<List<Map<Object, Object>>>() {}.getType()), is(false));
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

    private class FooToBarParameterConverter extends FromStringParameterConverter<Bar> {
        @Override
        public Bar convertValue(String value, Type type) {
            return new Bar();
        }
    }

    private class FirstParameterConverter extends AbstractParameterConverter<ExamplesTable, FirstConverterOutput> {
        @Override
        public FirstConverterOutput convertValue(ExamplesTable value, Type type) {
            return new FirstConverterOutput(value.asString() + "first");
        }
    }

    private class FirstConverterOutput extends StringContainer {
        private FirstConverterOutput(String output) {
            super(output);
        }
    }

    private class SecondParameterConverter extends AbstractParameterConverter<FirstConverterOutput, SecondConverterOutput> {
        @Override
        public SecondConverterOutput convertValue(FirstConverterOutput value, Type type) {
            return new SecondConverterOutput(value.getOutput() + "second");
        }
    }

    private class SecondConverterOutput extends StringContainer {
        private SecondConverterOutput(String output) {
            super(output);
        }
    }

    private class ThirdParameterConverter extends AbstractParameterConverter<SecondConverterOutput, ThirdConverterOutput> {
        @Override
        public ThirdConverterOutput convertValue(SecondConverterOutput value, Type type) {
            return new ThirdConverterOutput(value.getOutput() + "third");
        }
    }

    private class ThirdConverterOutput extends StringContainer {
        private ThirdConverterOutput(String output) {
            super(output);
        }
    }

    private class StringContainer {
        private final String output;

        private StringContainer(String output) {
            this.output = output;
        }

        String getOutput() {
            return output;
        }
    }

    private class StringContainerConverter extends AbstractParameterConverter<String, StringContainer> {
        @Override
        public StringContainer convertValue(String value, Type type) {
            return new StringContainer(value);
        }
    }

    private class ListToSetConverter extends AbstractParameterConverter<List<String>, Set<String>> {

        @Override
        public Set<String> convertValue(List<String> value, Type type) {
            return new HashSet<>(value);
        }

    }
}
