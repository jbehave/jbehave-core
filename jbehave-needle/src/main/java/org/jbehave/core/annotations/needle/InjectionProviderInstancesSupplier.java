package org.jbehave.core.annotations.needle;

import java.util.Set;

import de.akquinet.jbosscc.needle.NeedleTestcase;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;

/**
 * <a href="http://javadocs.techempower.com/jdk18/api/java/util/function/Supplier.html">Supplies</a> a Set of
 * InjectionProvider instances that are created outside the {@link NeedleStepFactory} lifecycle.
 * 
 * @author Jan Galinski, Holisticon AG (jan.galinski@holisticon.de)
 * @author Simon Zambrovski, Holisticon AG (simon.zambrovski@holisticon.de)
 */
public interface InjectionProviderInstancesSupplier {

	/**
	 * <a href="http://javadocs.techempower.com/jdk18/api/java/util/function/Supplier.html">Supplies</a> a Set of
	 * InjectionProvider instances that are created outside the {@link NeedleFactory} lifecycle.
	 * 
	 * @return InjectionProviders that can be added to {@link NeedleTestcase}
	 */
	Set<InjectionProvider<?>> get();
}
