package com.learnthinkcode.example.jbehave.service;

import com.learnthinkcode.example.jbehave.AbstractSpringJBehaveIntegrationTest;

public class AuthWithUserFlagsIntegrationTest extends AbstractSpringJBehaveIntegrationTest {

	@Override
	protected String storyPath() {
		return "classpath:/stories/authentication/user_flags.story";
	}
}
