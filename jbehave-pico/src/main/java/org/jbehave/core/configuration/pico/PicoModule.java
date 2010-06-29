package org.jbehave.core.configuration.pico;

import org.picocontainer.MutablePicoContainer;

public interface PicoModule {
    
    void configure(MutablePicoContainer container);
    
}
