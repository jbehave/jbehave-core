package org.jbehave.example.spring.security.dao;

import java.util.List;

import org.hibernate.SessionFactory;
import org.jbehave.example.spring.security.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;


@Repository("userDao")
public class UserDaoImpl extends HibernateDaoSupport implements UserDao {

  @Autowired
  public UserDaoImpl(SessionFactory sessionFactory) {
    setSessionFactory(sessionFactory);
  }

  public User load(Long id) {
    return (User) getHibernateTemplate().load(User.class, id);
  }

  public User persist(User user) {
    getHibernateTemplate().saveOrUpdate(user);
    return user;
  }

  @SuppressWarnings("unchecked")
  public User findUserByOrganizationAndUsername(Long organizationId, String username) {
    List<User> query = getHibernateTemplate().find("from User where organization.id = ? and username = ?", new Object[] { organizationId, username });
    if (query.size() == 1) {
      return (User) query.get(0);
    }
    return null;
  }

}
