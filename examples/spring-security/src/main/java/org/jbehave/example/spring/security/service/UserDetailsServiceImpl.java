package org.jbehave.example.spring.security.service;

import org.jbehave.example.spring.security.dao.UserDao;
import org.jbehave.example.spring.security.domain.Organization;
import org.jbehave.example.spring.security.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserDao userDao;

  @Autowired
  private OrganizationManager organizationManager;

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
    Organization org = organizationManager.getOrganization();
    User user = userDao.findUserByOrganizationAndUsername(org.getId(), username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }
    return new UserDetailsImpl(user, org.getAuthenticationPolicy());
  }
}
