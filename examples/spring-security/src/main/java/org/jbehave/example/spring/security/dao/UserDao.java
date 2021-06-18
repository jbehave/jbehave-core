package org.jbehave.example.spring.security.dao;

import org.jbehave.example.spring.security.domain.User;

public interface UserDao {

    public User load(Long id);

    public User persist(User user);

    public User findUserByOrganizationAndUsername(Long organizationId, String username);

}
