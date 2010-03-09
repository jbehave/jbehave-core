package org.jbehave.scenario.reporters;

import java.io.PrintStream;

/**
 * Creates {@link PrintStream} instances for named stories
 */
public interface PrintStreamFactory {

    PrintStream getPrintStream();

}
