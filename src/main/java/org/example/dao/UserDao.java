package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.example.pojo.UserPojo;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager em;

    public void insert(UserPojo user) {
        em.persist(user);
    }

    public UserPojo getByEmail(String email) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserPojo> query = cb.createQuery(UserPojo.class);
        Root<UserPojo> root = query.from(UserPojo.class);
        
        query.select(root)
             .where(cb.equal(cb.lower(root.get("email")), email.toLowerCase().trim()));
        
        return em.createQuery(query).getResultList().stream().findFirst().orElse(null);
    }
}
