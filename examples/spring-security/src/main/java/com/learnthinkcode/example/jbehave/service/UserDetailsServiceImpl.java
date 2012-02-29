package com.learnthinkcode.example.jbehave.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.learnthinkcode.example.jbehave.dao.UserDao;
import com.learnthinkcode.example.jbehave.domain.Organization;
import com.learnthinkcode.example.jbehave.domain.User;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserDao userDao;

  @Autowired
  private OrganizationManager organizationManager;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
    Organization org = organizationManager.getOrganization();
    User user = userDao.findUserByOrganizationAndUsername(org.getId(), username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }
    return new UserDetailsImpl(user, org.getAuthenticationPolicy());
  }
}
