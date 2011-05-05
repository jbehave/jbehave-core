package org.jbehave.core.configuration;

import org.junit.Test;

import com.thoughtworks.paranamer.CachingParanamer;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.MatcherAssert.assertThat;

public class ParanamerConfigurationBehaviour {

    @Test
    public void shouldUseCachingParanamer() {
        assertThat(new ParanamerConfiguration().paranamer(), is(CachingParanamer.class));
    }
    
}
