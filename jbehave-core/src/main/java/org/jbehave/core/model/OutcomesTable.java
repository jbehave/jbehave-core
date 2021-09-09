package org.jbehave.core.model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hamcrest.Matcher;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;

/**
 * Represents a tabular structure that holds {@link Outcome}s to be verified by invoking method {@link #verify()}. If
 * verification fails an {@link OutcomesFailed} exception is thrown.
 * <p>The Outcomes Tables allows the specification of {@link Keywords} for the outcome fields, as well as rendering
 * formats for different types. The default formats include:
 * <ul>
 *     <li>Date: "EEE MMM dd hh:mm:ss zzz yyyy"</li>
 *     <li>Number: "0.###"</li>
 *     <li>Boolean: "yes,no"</li>
 * </ul>
 * These formats can be overridden as well as new ones added. The formats can be retrieved via methods
 * {@link #getFormat(Type)} and {@link #getFormat(String)}.</p>
 */
public class OutcomesTable {

    private static final String NEWLINE = "\n";
    private static final String HEADER_SEPARATOR = "|";
    private static final String VALUE_SEPARATOR = "|";

    private final Keywords keywords;
    private final Map<Type,String> formats;
    private final List<Outcome<?>> outcomes = new ArrayList<>();
    private final List<Outcome<?>> failedOutcomes = new ArrayList<>();
    private UUIDExceptionWrapper failureCause;

    public OutcomesTable() {
        this(new LocalizedKeywords());
    }

    public OutcomesTable(Keywords keywords) {
        this(keywords, defaultFormats());
    }

    public OutcomesTable(Map<Type, String> formats) {
        this(new LocalizedKeywords(), formats);
    }

    public OutcomesTable(Keywords keywords, Map<Type, String> formats) {
        this.keywords = keywords;
        this.formats = mergeWithDefaults(formats);
    }

    /**
     * @deprecated Use {@link #OutcomesTable(Keywords, Map)}
     */
    @Deprecated
    public OutcomesTable(Keywords keywords, String dateFormat) {
        this(keywords, mergeWithDefaults(Date.class, dateFormat));
    }

    public <T> void addOutcome(String description, T value, Matcher<T> matcher) {
        outcomes.add(new Outcome<>(description, value, matcher));
    }

    public void verify() {
        boolean failed = false;
        failedOutcomes.clear();
        for (Outcome<?> outcome : outcomes) {
            if (!outcome.isVerified()) {
                failedOutcomes.add(outcome);
                failed = true;
                break;
            }
        }
        if (failed) {
            failureCause = new UUIDExceptionWrapper(new OutcomesFailed(this));
            throw failureCause;
        }
    }

    public UUIDExceptionWrapper failureCause() {
        return failureCause;
    }

    public List<Outcome<?>> getOutcomes() {
        return outcomes;
    }

    public List<Outcome<?>> getFailedOutcomes() {
        return failedOutcomes;
    }

    public List<String> getOutcomeFields() {
        return keywords.outcomeFields();
    }

    public Map<Type, String> getFormats() {
        return formats;
    }

    public String getFormat(Type type) {
        return formats.get(type);
    }

    public String getFormat(String typeName) {
        try {
            return getFormat(Class.forName(typeName));
        } catch (ClassNotFoundException e) {
            throw new FormatTypeInvalid(typeName, e);
        }
    }

    /**
     * @deprecated Use {@link #getFormat(Type)}
     */
    @Deprecated
    public String getDateFormat() {
        return getFormat(Date.class);
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> iterator = getOutcomeFields().iterator(); iterator.hasNext();) {
            sb.append(HEADER_SEPARATOR).append(iterator.next());
            if (!iterator.hasNext()) {
                sb.append(HEADER_SEPARATOR).append(NEWLINE);
            }
        }
        for (Outcome<?> outcome : outcomes) {
            sb.append(VALUE_SEPARATOR).append(outcome.getDescription()).append(VALUE_SEPARATOR).append(
                    outcome.getValue()).append(VALUE_SEPARATOR).append(outcome.getMatcher()).append(VALUE_SEPARATOR)
                    .append(outcome.isVerified()).append(VALUE_SEPARATOR).append(NEWLINE);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    private static Map<Type,String> defaultFormats() {
        Map<Type,String> map = new HashMap<>();
        map.put(Date.class, "EEE MMM dd hh:mm:ss zzz yyyy");
        map.put(Number.class, "0.###");
        map.put(Boolean.class, "yes,no");
        return map;
    }

    private static Map<Type,String> mergeWithDefaults(Type type, String format) {
        Map<Type,String> map = defaultFormats();
        map.put(type, format);
        return map;
    }

    private Map<Type, String> mergeWithDefaults(Map<Type, String> formats) {
        Map<Type,String> map = defaultFormats();
        map.putAll(formats);
        return map;
    }

    public static class Outcome<T> {

        private final String description;
        private final T value;
        private final Matcher<T> matcher;
        private final boolean verified;

        public Outcome(String description, T value, Matcher<T> matcher) {
            this.description = description;
            this.value = value;
            this.matcher = matcher;
            this.verified = matcher.matches(value);
        }

        public String getDescription() {
            return description;
        }

        public T getValue() {
            return value;
        }

        public Matcher<T> getMatcher() {
            return matcher;
        }

        public boolean isVerified() {
            return verified;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @SuppressWarnings("serial")
    public static class OutcomesFailed extends UUIDExceptionWrapper {
        private transient OutcomesTable outcomes;

        public OutcomesFailed(OutcomesTable outcomes) {
            this.outcomes = outcomes;
        }

        public OutcomesTable outcomesTable() {
            return outcomes;
        }

    }

    public static class FormatTypeInvalid extends RuntimeException {
        public FormatTypeInvalid(String type, Throwable e) {
            super(type, e);
        }
    }
}
