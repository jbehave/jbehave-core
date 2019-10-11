package org.jbehave.example.spring.security.util;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class SecurityUtils
{
	private static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

	public static String encodePassword(String passwordCleartext)
	{
		return PASSWORD_ENCODER.encode(passwordCleartext);
	}
}
