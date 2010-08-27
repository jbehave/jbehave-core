package com.learnthinkcode.example.jbehave.service;

import java.util.Map;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.model.ExamplesTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.learnthinkcode.example.jbehave.dao.OrganizationDao;
import com.learnthinkcode.example.jbehave.dao.UserDao;
import com.learnthinkcode.example.jbehave.domain.Organization;
import com.learnthinkcode.example.jbehave.domain.User;
import com.learnthinkcode.example.jbehave.domain.UserBuilder;

@Component("userSteps")
public class UserSteps {

  @Autowired
  private UserDao userDao;

  @Autowired
  private OrganizationDao organizationDao;

  @Given("a user for $orgName with username $username and password $password")
  public void createUserWithUsernameAndPassword(String orgName, String username, String password) {
    Organization org = organizationDao.findByName(orgName);
    User user = new User();
    user.setOrganization(org);
    user.setUsername(username);
    user.setPasswordCleartext(password);
    userDao.persist(user);
  }

  @Given("the users for $orgName: $userTable")
  public void createUsersFromTable(String orgName, ExamplesTable table) {
    Organization org = organizationDao.findByName(orgName);
    for (Map<String, String> row : table.getRows()) {
      userDao.persist(new UserBuilder(org, row).build());
    }
  }

  @Given("user for $orgName $username is disabled")
  public void setUserDisabled(String orgName, String username) {
    Organization org = organizationDao.findByName(orgName);
    User user = userDao.findUserByOrganizationAndUsername(org.getId(), username);
    user.setEnabled(false);
    userDao.persist(user);
  }

  @Given("user for $orgName $username is enabled")
  public void setUserEnabled(String orgName, String username) {
    Organization org = organizationDao.findByName(orgName);
    User user = userDao.findUserByOrganizationAndUsername(org.getId(), username);
    user.setEnabled(true);
    userDao.persist(user);
  }

  @Given("user for $orgName $username is expired")
  public void setUserExpired(String orgName, String username) {
    Organization org = organizationDao.findByName(orgName);
    User user = userDao.findUserByOrganizationAndUsername(org.getId(), username);
    user.setExpired(true);
    userDao.persist(user);
  }
}
