package org.jbehave.examples.core.converters;

import java.lang.reflect.Type;

import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConvertionFailed;
import org.jbehave.examples.core.model.Trader;
import org.jbehave.examples.core.persistence.TraderPersister;

public class TraderConverter extends AbstractParameterConverter<Trader> {
    private TraderPersister persister;

    public TraderConverter(TraderPersister persister) {
        this.persister = persister;
    }

    @Override
    public Trader convertValue(String value, Type type) {
        Trader trader = persister.retrieveTrader(value);
        if (trader == null) {
            throw new ParameterConvertionFailed("Trader not found for name " + value, null);
        }
        return trader;
    }

}
