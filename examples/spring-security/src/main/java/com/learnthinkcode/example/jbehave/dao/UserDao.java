package com.learnthinkcode.example.jbehave.dao;

import com.learnthinkcode.example.jbehave.domain.User;

public interface UserDao {

  public User load(Long id);

  public User persist(User user);

  public User findUserByOrganizationAndUsername(Long organizationId, String username);

}
