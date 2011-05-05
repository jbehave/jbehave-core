package com.learnthinkcode.example.jbehave.service;

import com.learnthinkcode.example.jbehave.AbstractSpringJBehaveIntegrationTest;

public class AuthWithExpiredPasswordsIntegrationTest extends AbstractSpringJBehaveIntegrationTest {

	@Override
	protected String storyPath() {
		return "classpath:/stories/authentication/expired_passwords.story";
	}
}
