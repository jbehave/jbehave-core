package org.jbehave.core.parsers;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.Validate.isTrue;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.Alias;
import org.jbehave.core.model.AliasVariant;
import org.jbehave.core.steps.StepType;

public class JsonAliasParser implements AliasParser {

    private final Keywords keywords;

    public JsonAliasParser(Keywords keywords) {
        this.keywords = keywords;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Alias> parse(Set<String> aliasesAsStrings) {
        if (aliasesAsStrings.isEmpty()) {
            return Collections.emptyList();
        }

        Gson gson = new Gson();
        Type aliasType = new TypeToken<List<AliasContainer>>() {}.getType();

        return aliasesAsStrings.stream()
                               .map(json -> (List<AliasContainer>) gson.fromJson(json, aliasType))
                               .flatMap(List::stream)
                               .map(JsonAliasParser::validate)
                               .collect(groupingBy(AliasContainer::getName, collectingAndThen(toList(),
                                       JsonAliasParser::mergeAliases)))
                               .values()
                               .stream()
                               .map(this::convert)
                               .collect(toList());
    }

    // https://github.com/google/gson/issues/61
    private static AliasContainer validate(AliasContainer alias) {
        isTrue(alias.getName() != null, "The 'name' property that identifies step must be set");
        Set<AliasEntry> aliases = alias.getAliases();
        isTrue(aliases != null, "The 'aliases' property must be set");
        aliases.forEach(entry -> isTrue(entry.getName() != null, "The 'name' property of alias must be set"));
        return alias;
    }

    private static AliasContainer mergeAliases(List<AliasContainer> aliases) {
        if (aliases.size() == 1) {
            return aliases.get(0);
        }

        return aliases.stream().reduce((l, r) -> {
            l.getAliases().addAll(r.getAliases());
            return l;
        }).get();
    }

    private Alias convert(AliasContainer container) {
        String name = container.getName();
        StepType stepType = keywords.stepTypeFor(name);

        List<AliasVariant> variants = container.getAliases().stream().map(alias -> {
            String aliasName = alias.getName();
            StepType aliasType = keywords.stepTypeFor(alias.getName());

            if (aliasType != stepType) {
                throw new InvalidAliasType(aliasName, stepType);
            }

            return keywords.stepWithoutStartingWord(aliasName, aliasType);
        }).map(AliasVariant::new).collect(Collectors.toList());

        return new Alias(keywords.stepWithoutStartingWord(name, stepType), stepType, variants);
    }

    @SuppressWarnings("unused")
    private static final class AliasContainer {

        private String name;
        private Set<AliasEntry> aliases;

        private String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        private Set<AliasEntry> getAliases() {
            return aliases;
        }

        private void setAliases(Set<AliasEntry> aliases) {
            this.aliases = aliases;
        }

    }

    @SuppressWarnings("unused")
    private static final class AliasEntry {

        private String name;

        private String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof AliasEntry)) {
                return false;
            }

            AliasEntry other = (AliasEntry) obj;
            return Objects.equals(name, other.name);
        }

    }

    @SuppressWarnings("serial")
    public static class InvalidAliasType extends RuntimeException {

        public InvalidAliasType(String alias, StepType stepType) {
            super(String.format("The alias '%s' must be of type '%s'", alias, stepType));
        }

    }
}
