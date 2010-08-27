package com.learnthinkcode.example.jbehave.service;

import com.learnthinkcode.example.jbehave.AbstractSpringJBehaveIntegrationTest;

public class AuthWithDefaultPolicyIntegrationTest extends AbstractSpringJBehaveIntegrationTest {

	@Override
	protected String storyPath() {
		return "classpath:/stories/authentication/default_policy.story";
	}
}
