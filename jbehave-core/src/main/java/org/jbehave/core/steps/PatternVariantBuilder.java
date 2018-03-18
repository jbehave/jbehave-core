package org.jbehave.core.steps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

/**
 * <p>
 * Builds a set of pattern variants of given pattern input, supporting a custom
 * directives. Depending on the directives present, one or more resulting
 * variants are created.
 * </p>
 * <p>
 * Currently supported directives are
 * </p>
 * <table border="1">
 * <thead>
 * <tr>
 * <td>Pattern</td>
 * <td>Result</td>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>..A {x|y} B..</td>
 * <td>
 * <ul>
 * <li>..A x B..</li>
 * <li>..A y B..</li>
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td>..A {x|y|} B..</td>
 * <td>
 * <ul>
 * <li>..A x B..</li>
 * <li>..A y B..</li>
 * <li>..A B..</li>
 * </ul>
 * </td>
 * </tr>
 * <tr>
 * <td>..A {x} B..</td>
 * <td>
 * <ul>
 * <li>..A x B..</li>
 * </ul>
 * </td>
 * </tr>
 * </table>
 * 
 * <p>
 * These directives can be used to conveniently create several variants of a
 * step pattern, without having to repeat it as a whole as one or more aliases.
 * </p>
 * <p>
 * Examples:
 * </p>
 * <ul>
 * <li>
 * <p>
 * <code>
 * 
 * @Then("the result {must |has to |}be $x")<br> public void checkResult(int
 *            x)...<br></code>
 *            </p>
 *            <p>
 *            Would match any of these variants from a story file:
 *            <ul>
 *            <li>Then the result must be 42</li> <li>Then the result has to be
 *            42</li> <li>Then the result be 42</li>
 *            </ul>
 *            </p>
 *            </li> <li>
 *            <p>
 *            <code>
 * @When("$A {+|plus|is added to} $B")<br> public void add(int A, int B)...<br>
 *           </code>
 *           </p>
 *           <p>
 *           Would match any of these variants from a story file:
 *           <ul>
 *           <li>When 42 + 23</li> <li>When 42 plus 23</li> <li>When 42 is added
 *           to 23</li>
 *           </ul>
 *           </p>
 *           </li>
 *           </ul>
 * 
 * @author Daniel Schneller
 */
public class PatternVariantBuilder {

    /**
     * Regular expression that locates patterns to be evaluated in the input
     * pattern.
     */
    private final Pattern regex = Pattern.compile("([^\\n{]*+)(\\{(([^|}]++)(\\|)?+)*+\\})([^\\n]*+)");

    private final Set<String> variants;

    private final String input;

    /**
     * Creates a builder and calculates all variants for given input. When there
     * are no variants found in the input, it will itself be the only result.
     * 
     * @param input to be evaluated
     */
    public PatternVariantBuilder(String input) {
        this.input = input;
        this.variants = variantsFor(input);
    }

    public String getInput() {
        return input;
    }

    /**
     * <p>
     * Parses the {@link #input} received at construction and generates the
     * variants. When there are multiple patterns in the input, the method will
     * recurse on itself to generate the variants for the tailing end after the
     * first matched pattern.
     * </p>
     * <p>
     * Generated variants are stored in a {@link Set}, so there will never be
     * any duplicates, even if the input's patterns were to result in such.
     * </p>
     */
    private Set<String> variantsFor(String input) {
        // Store current invocation's results
        Set<String> variants = new HashSet<>();

        Matcher m = regex.matcher(input);
        boolean matches = m.matches();

        if (!matches) {
            // if the regex does not find any patterns,
            // simply add the input as is
            variants.add(input);
            // end recursion
            return variants;
        }

        // isolate the part before the first pattern
        String head = m.group(1);

        // isolate the pattern itself, removing its wrapping {}
        String patternGroup = m.group(2).replaceAll("[\\{\\}]", "");

        // isolate the remaining part of the input
        String tail = m.group(6);

        // split the pattern into its options and add an empty
        // string if it ends with a separator
        List<String> patternParts = new ArrayList<>();
        patternParts.addAll(asList(patternGroup.split("\\|")));
        if (patternGroup.endsWith("|")) {
            patternParts.add("");
        }

        // Iterate over the current pattern's
        // variants and construct the result.
        for (String part : patternParts) {
            StringBuilder builder = new StringBuilder();
            if (head != null) {
                builder.append(head);
            }
            builder.append(part);

            // recurse on the tail of the input
            // to handle the next pattern
            Set<String> tails = variantsFor(tail);

            // append all variants of the tail end
            // and add each of them to the part we have
            // built up so far.
            for (String tailVariant : tails) {
                StringBuilder tailBuilder = new StringBuilder(builder.toString());
                tailBuilder.append(tailVariant);
                variants.add(tailBuilder.toString());
            }
        }
        return variants;
    }

    /**
     * Returns a new copy set of all variants with no whitespace compression.
     * 
     * @return a {@link Set} of all variants without whitespace compression
     * @see #allVariants(boolean)
     */
    public Set<String> allVariants() {
        return allVariants(false);
    }

    /**
     * <p>
     * Returns a new copy set of all variants. Any two or more consecutive white
     * space characters will be condensed into a single space if boolean flag is
     * set.
     * </p>
     * <p>
     * Otherwise, any whitespace will be left as is.
     * </p>
     * 
     * @param compressWhitespace whether or not to compress whitespace
     * @return a {@link Set} of all variants
     */
    public Set<String> allVariants(boolean compressWhitespace) {
        if (!compressWhitespace) {
            return new HashSet<>(variants);
        }
        Set<String> compressed = new HashSet<>();
        for (String variant : variants) {
            compressed.add(variant.replaceAll("\\s{2,}", " "));
        }
        return compressed;
    }

}
