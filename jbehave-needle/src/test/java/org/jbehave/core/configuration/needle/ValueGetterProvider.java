package org.jbehave.core.configuration.needle;

import org.jbehave.core.steps.needle.ValueGetter;
import org.needle4j.injection.InjectionProvider;
import org.needle4j.injection.InjectionTargetInformation;

public class ValueGetterProvider implements InjectionProvider<ValueGetter> {

	@Override
    public boolean verify(InjectionTargetInformation target) {
		return target.getType().isAssignableFrom(ValueGetter.class);
	}

	@Override
    public ValueGetter getInjectedObject(Class<?> injectionPointType) {
		return new ValueGetter() {

			@Override
            public Object getValue() {
				return VALUE;
			}

		};
	}

	@Override
    public Object getKey(InjectionTargetInformation target) {
		return target.getType().getCanonicalName();
	}
}
