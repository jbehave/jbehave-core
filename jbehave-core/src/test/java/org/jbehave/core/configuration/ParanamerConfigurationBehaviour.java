package org.jbehave.core.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import com.thoughtworks.paranamer.CachingParanamer;

import org.junit.jupiter.api.Test;

class ParanamerConfigurationBehaviour {

    @Test
    void shouldUseCachingParanamer() {
        assertThat(new ParanamerConfiguration().paranamer(), instanceOf(CachingParanamer.class));
    }
    
}
