package org.jbehave.core.i18n;

public class NullCoder extends StringCoder {

    @Override
    public String canonicalize(String input) {
        return input;
    }

}
