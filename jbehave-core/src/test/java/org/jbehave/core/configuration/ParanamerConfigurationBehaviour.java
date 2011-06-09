package org.jbehave.core.configuration;

import org.junit.Test;

import com.thoughtworks.paranamer.CachingParanamer;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.instanceOf;

public class ParanamerConfigurationBehaviour {

    @Test
    public void shouldUseCachingParanamer() {
        assertThat(new ParanamerConfiguration().paranamer(), instanceOf(CachingParanamer.class));
    }
    
}
