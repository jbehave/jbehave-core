package org.jbehave.example.spring.security.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.SessionFactory;
import org.jbehave.example.spring.security.domain.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository("organizationDao")
public class OrganizationDaoImpl implements OrganizationDao {

  @Autowired
  private SessionFactory sessionFactory;

  @Override
  @Transactional
  public Organization load(Long id) {
    return (Organization) sessionFactory.getCurrentSession().load(Organization.class, id);
  }

  @Override
  @Transactional
  public Organization persist(Organization organization) {
    sessionFactory.getCurrentSession().saveOrUpdate(organization);
    return organization;
  }

  @Override
  @SuppressWarnings("unchecked")
  @Transactional
  public Organization findByName(String orgName) {
    List<Organization> query = sessionFactory.getCurrentSession()
            .createQuery("from Organization where name = :name")
		    .setParameter("name", orgName)
            .list();
    if (query.size() == 1) {
      return query.get(0);
    }
    return null;
  }

}
