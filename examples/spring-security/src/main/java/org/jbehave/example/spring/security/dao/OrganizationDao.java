package org.jbehave.example.spring.security.dao;

import org.jbehave.example.spring.security.domain.Organization;

public interface OrganizationDao {

    public Organization load(Long id);

    public Organization persist(Organization organization);

    public Organization findByName(String orgName);
}
