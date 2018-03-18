package org.jbehave.examples.core.steps;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.core.CoreStory;

import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

public class NamedAnnotationsSteps {

    private static final String NOT_SUPPORTED = "notSupported";
    private static final String SUPPORTED = "supported";

    private List<SimpleMessage> messages = new LinkedList<SimpleMessage>();

    @Given("a message with <actionSupportability> action and <eventTypeSupportability> event")
    public void given(@Named("actionSupportability") String actionSupportability,
            @Named("eventTypeSupportability") String eventTypeSupportability,
            @Named("supportedActions") String supportedActions,
            @Named("supportedEventTypes") String supportedEventTypes,
            @Named("notSupportedActions") String notSupportedActions,
            @Named("notSupportedEventTypes") String notSupportedEventTypes) {
        List<String> actions = new LinkedList<String>();
        if (actionSupportability.equals(SUPPORTED)) {
            actions.addAll(asList(supportedActions.split(",")));
        } else if (actionSupportability.equals(NOT_SUPPORTED)) {
            actions.addAll(asList(notSupportedActions.split(",")));
        } else {
            throw new IllegalStateException("wrong action supportability parameter: " + actionSupportability);
        }
        List<String> eventTypes = new LinkedList<String>();
        if (eventTypeSupportability.equals(SUPPORTED)) {
            eventTypes.addAll(asList(supportedEventTypes.split(",")));
        } else if (eventTypeSupportability.equals(NOT_SUPPORTED)) {
            eventTypes.addAll(asList(notSupportedEventTypes.split(",")));
        } else {
            throw new IllegalStateException("wrong evenType supportability parameter: " + eventTypeSupportability);
        }

        for (String givenAction : actions) {
            for (String givenEventType : eventTypes) {
                SimpleMessage message = new SimpleMessageBuilder().withAction(givenAction)
                        .withEventType(givenEventType).build();
                messages.add(message);
            }
        }

    }

    @When("it is received")
    public void when() {
        for (SimpleMessage message : messages) {
            System.out.println("Message [" + message + "] received");
        }
    }

    @Then("message is consumed without error")
    public void then() {
        for (SimpleMessage message : messages) {
            System.out.println("Message [" + message + "] consumed");
        }
    }

    public static class SimpleMessage {

        private String action;
        private String evenType;

        public SimpleMessage(String action, String eventType) {
            this.action = action;
            this.evenType = eventType;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("action", action).append("eventType", evenType).toString();
        }
    }

    public static class SimpleMessageBuilder {

        private String action;
        private String eventType;

        public SimpleMessageBuilder withAction(String action) {
            this.action = action;
            return this;
        }

        public SimpleMessageBuilder withEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public SimpleMessage build() {
            return new SimpleMessage(action, eventType);
        }
        
        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }
    }

}
