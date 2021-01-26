package org.jbehave.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.hamcrest.Matchers;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.junit.jupiter.api.Test;

class ScanningStepsFactoryBehaviour {

	@Test
	void shouldScanStepsFromRootClass() {
		InjectableStepsFactory factory = new ScanningStepsFactory(
				new MostUsefulConfiguration(), this.getClass());
		List<CandidateSteps> candidateSteps = factory.createCandidateSteps();
		assertThat(candidateSteps.size(), Matchers.greaterThan(1));
	}

	@Test
	void shouldScanStepsFromPackagesAndIgnoreClassesNotAnnotated() {
		InjectableStepsFactory factory = new ScanningStepsFactory(
				new MostUsefulConfiguration(), "org.jbehave.core.steps.scan",
				"org.jbehave.core.steps.scan2");
		List<CandidateSteps> candidateSteps = factory.createCandidateSteps();
		assertThat(candidateSteps.size(), Matchers.equalTo(3));
	}

	@Test
	void shouldNotFindAnyStepsFromNonAnnotatedClasses() {
		InjectableStepsFactory factory = new ScanningStepsFactory(
				new MostUsefulConfiguration(), "org.jbehave.core.steps.scan2");
		List<CandidateSteps> candidateSteps = factory.createCandidateSteps();
		assertThat(candidateSteps.size(), Matchers.equalTo(0));
	}

	@Test
	void shouldScanStepsFromPackagesAndFilterMatchingNames() {
		InjectableStepsFactory factory = new ScanningStepsFactory(
				new MostUsefulConfiguration(), "org.jbehave.core.steps.scan")
				.matchingNames(".*GivenWhen.*").notMatchingNames(".*GivenWhenThen");
		List<CandidateSteps> candidateSteps = factory.createCandidateSteps();
		assertThat(candidateSteps.size(), Matchers.equalTo(1));
	}

	@Test
	void shouldNotFindAnyStepsFromInexistingPackage() {
		InjectableStepsFactory factory = new ScanningStepsFactory(
				new MostUsefulConfiguration(), "org.jbehave.inexisting");
		List<CandidateSteps> candidateSteps = factory.createCandidateSteps();
		assertThat(candidateSteps.size(), Matchers.equalTo(0));
	}

}
