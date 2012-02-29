package com.learnthinkcode.example.jbehave.dao;

import com.learnthinkcode.example.jbehave.domain.Organization;

public interface OrganizationDao {

  public Organization load(Long id);

  public Organization persist(Organization organization);

  public Organization findByName(String orgName);
}
