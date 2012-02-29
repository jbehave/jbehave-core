package com.learnthinkcode.example.jbehave.util;

import org.springframework.security.providers.encoding.ShaPasswordEncoder;

public abstract class SecurityUtils {
  private static String SYSTEM_WIDE_SALT = "jb3h4v3";

  public static String encodePassword(String passwordCleartext) {
    ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
    return encoder.encodePassword(passwordCleartext, SYSTEM_WIDE_SALT);
  }
}
