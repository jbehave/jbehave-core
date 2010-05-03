package org.jbehave.core.reporters;

import java.io.PrintStream;

/**
 * Creates {@link PrintStream} instances.
 * </p>
 * Implementations will be responsible for providing all the parameters
 * required by the factory.  E.g. the {@link FilePrintStreamFactory} provides
 * the file path and configuration for the creation.
 */
public interface PrintStreamFactory {

    PrintStream createPrintStream();

}
