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

    public OutcomesTable(Keywords keywords, Map<Type, String> formats) {
        this.keywords = keywords;
        this.formats = formats;
    }

    /**
     * @deprecated Use {@link #OutcomesTable(Keywords,Map<Type,String>)}
     */
    @Deprecated
    public OutcomesTable(Keywords keywords, String dateFormat) {
        this(keywords, addToFormats(Date.class, dateFormat));
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

    public Map<Type, String> getFormats(){
        return formats;
    }

    public String getFormat(Type type){
        return formats.get(type);
    }

    /**
     * @deprecated Use {@link #getFormat(Type)}
     */
    @Deprecated
    public String getDateFormat(){
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
        return map;
    }

    private static Map<Type,String> addToFormats(Type type, String format) {
        Map<Type,String> map = defaultFormats();
        map.put(type, format);
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

}
