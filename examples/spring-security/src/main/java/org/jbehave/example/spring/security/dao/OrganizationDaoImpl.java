package org.jbehave.example.spring.security.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.jbehave.example.spring.security.domain.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;


@Repository("organizationDao")
public class OrganizationDaoImpl extends HibernateDaoSupport implements OrganizationDao {

  @Autowired
  public OrganizationDaoImpl(SessionFactory sessionFactory) {
    setSessionFactory(sessionFactory);
  }

  public Organization load(Long id) {
    return (Organization) getHibernateTemplate().load(Organization.class, id);
  }

  public Organization persist(Organization organization) {
    getHibernateTemplate().saveOrUpdate(organization);
    return organization;
  }

  @SuppressWarnings("unchecked")
  public Organization findByName(String orgName) {
    List<Organization> query = getHibernateTemplate().find("from Organization where name = ?", new Object[] { orgName });
    if (query.size() == 1) {
      return (Organization) query.get(0);
    }
    return null;
  }

}
