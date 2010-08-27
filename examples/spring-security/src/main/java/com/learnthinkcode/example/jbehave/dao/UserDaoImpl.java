package com.learnthinkcode.example.jbehave.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import com.learnthinkcode.example.jbehave.domain.User;

@Repository("userDao")
public class UserDaoImpl extends HibernateDaoSupport implements UserDao {

  @Autowired
  public UserDaoImpl(SessionFactory sessionFactory) {
    setSessionFactory(sessionFactory);
  }

  @Override
  public User load(Long id) {
    return (User) getHibernateTemplate().load(User.class, id);
  }

  @Override
  public User persist(User user) {
    getHibernateTemplate().saveOrUpdate(user);
    return user;
  }

  @Override
  @SuppressWarnings("unchecked")
  public User findUserByOrganizationAndUsername(Long organizationId, String username) {
    List<User> query = getHibernateTemplate().find("from User where organization.id = ? and username = ?", new Object[] { organizationId, username });
    if (query.size() == 1) {
      return (User) query.get(0);
    }
    return null;
  }

}
