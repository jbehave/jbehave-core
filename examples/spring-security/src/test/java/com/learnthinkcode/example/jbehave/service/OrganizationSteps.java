package com.learnthinkcode.example.jbehave.service;

import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.learnthinkcode.example.jbehave.dao.OrganizationDao;
import com.learnthinkcode.example.jbehave.domain.AuthenticationPolicy;
import com.learnthinkcode.example.jbehave.domain.AuthenticationPolicyBuilder;
import com.learnthinkcode.example.jbehave.domain.Organization;
import com.learnthinkcode.example.jbehave.service.OrganizationManager;

@Component("organizationSteps")
public class OrganizationSteps {

  @Autowired
  private OrganizationDao organizationDao;

  @Autowired
  private OrganizationManager organizationManager;

  @Given("an organization named $orgNames")
  @Alias("organizations named $orgNames")
  public void createOrganizationWithName(List<String> orgNames) {
    for (String orgName : orgNames) {
      Organization org = new Organization();
      org.setName(orgName);
      org.setAuthenticationPolicy(new AuthenticationPolicy());
      organizationDao.persist(org);
    }
  }

  @Given("a default authentication policy for $orgName")
  public void updateOrganizationWithDefaultAuthPolicy(String orgName) {
    Organization org = organizationDao.findByName(orgName);
    org.setAuthenticationPolicy(new AuthenticationPolicy());
    organizationDao.persist(org);
  }

  @Given("authentication policy for $orgName: $authPolicyTable")
  public void updateOrganizationWithDefaultAuthPolicy(String orgName, ExamplesTable table) {
    Organization org = organizationDao.findByName(orgName);
    Map<String, String> row = table.getRow(0);
    org.setAuthenticationPolicy(new AuthenticationPolicyBuilder(row).build());
    organizationDao.persist(org);
  }

  @When("current organization is $orgName")
  public void setCurrentOrganization(String orgName) {
    Organization org = organizationDao.findByName(orgName);
    organizationManager.setOrganization(org);
  }
}
