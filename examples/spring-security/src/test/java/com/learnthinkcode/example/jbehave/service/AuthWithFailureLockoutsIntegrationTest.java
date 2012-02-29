package com.learnthinkcode.example.jbehave.service;

import com.learnthinkcode.example.jbehave.AbstractSpringJBehaveIntegrationTest;

public class AuthWithFailureLockoutsIntegrationTest extends AbstractSpringJBehaveIntegrationTest {

	@Override
	protected String storyPath() {
		return "classpath:/stories/authentication/failure_lockouts.story";
	}
}
