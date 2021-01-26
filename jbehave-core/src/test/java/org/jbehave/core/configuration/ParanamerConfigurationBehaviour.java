package org.jbehave.core.configuration;

import org.junit.jupiter.api.Test;

import com.thoughtworks.paranamer.CachingParanamer;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.instanceOf;

class ParanamerConfigurationBehaviour {

    @Test
    void shouldUseCachingParanamer() {
        assertThat(new ParanamerConfiguration().paranamer(), instanceOf(CachingParanamer.class));
    }
    
}
