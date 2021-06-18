package org.jbehave.example.spring.security.steps;

import java.util.List;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.jbehave.example.spring.security.dao.OrganizationDao;
import org.jbehave.example.spring.security.dao.UserDao;
import org.jbehave.example.spring.security.domain.Organization;
import org.jbehave.example.spring.security.domain.User;
import org.jbehave.example.spring.security.domain.UserBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        List<Parameters> parametersList = table.getRowsAsParameters(true);
        for (Parameters parameters : parametersList) {
            userDao.persist(new UserBuilder(org, parameters, table.getHeaders()).build());
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
