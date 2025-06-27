package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
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
        String jpql = "SELECT u FROM UserPojo u WHERE LOWER(u.email) = :email";
        TypedQuery<UserPojo> query = em.createQuery(jpql, UserPojo.class);
        query.setParameter("email", email.toLowerCase().trim());
        return query.getResultList().stream().findFirst().orElse(null);
    }
}
