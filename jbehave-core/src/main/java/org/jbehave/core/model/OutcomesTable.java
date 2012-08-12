package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hamcrest.Matcher;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.i18n.LocalizedKeywords;

public class OutcomesTable {

    private static final String NEWLINE = "\n";
    private static final String HEADER_SEPARATOR = "|";
    private static final String VALUE_SEPARATOR = "|";
	private static final String DEFAULT_DATE_FORMAT = "EEE MMM dd hh:mm:ss zzz yyyy";

    private final Keywords keywords;
	private final String dateFormat;
    private final List<Outcome<?>> outcomes = new ArrayList<Outcome<?>>();
    private final List<Outcome<?>> failedOutcomes = new ArrayList<Outcome<?>>();
    private UUIDExceptionWrapper failureCause;
    
    public OutcomesTable() {
        this(new LocalizedKeywords());
    }
    
    public OutcomesTable(Keywords keywords) {
        this(keywords, DEFAULT_DATE_FORMAT);
    }

    public OutcomesTable(Keywords keywords, String dateFormat) {
        this.keywords = keywords;
		this.dateFormat = dateFormat;
    }

    public <T> void addOutcome(String description, T value, Matcher<T> matcher) {
        outcomes.add(new Outcome<T>(description, value, matcher));
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

    public String getDateFormat(){
    	return dateFormat;
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
