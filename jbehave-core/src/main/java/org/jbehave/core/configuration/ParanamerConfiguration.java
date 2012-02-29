package org.jbehave.core.configuration;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

/**
 * The configuration that uses:
 * <ul>
 * <li>{@link Paranamer}: {@link CachingParanamer}</li>
 * </ul>
 */
public class ParanamerConfiguration extends MostUsefulConfiguration {

    public ParanamerConfiguration() {
        useParanamer(new CachingParanamer(new BytecodeReadingParanamer()));
    }

}
