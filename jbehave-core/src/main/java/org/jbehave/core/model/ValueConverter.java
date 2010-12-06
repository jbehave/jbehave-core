package org.jbehave.core.model;

import java.lang.reflect.Type;

/**
 * Converts values.
 */
public interface ValueConverter {
    Object convert(String value, Type type);
}
