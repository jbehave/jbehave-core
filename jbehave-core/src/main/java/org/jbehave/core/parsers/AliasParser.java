package org.jbehave.core.parsers;

import java.util.Collection;
import java.util.Set;

import org.jbehave.core.model.Alias;

public interface AliasParser {

    Collection<Alias> parse(Set<String> aliasesAsStrings);

}
