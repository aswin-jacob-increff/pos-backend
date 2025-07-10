package org.example.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.example.pojo.UserPojo;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends AbstractDao<UserPojo> {

    public UserDao() {
        super(UserPojo.class);
    }

    public UserPojo getByEmail(String email) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserPojo> query = cb.createQuery(UserPojo.class);
        Root<UserPojo> root = query.from(UserPojo.class);
        
        query.select(root)
             .where(cb.equal(cb.lower(root.get("email")), email.toLowerCase().trim()));
        
        return em.createQuery(query).getResultList().stream().findFirst().orElse(null);
    }

    @Override
    protected void updateEntity(UserPojo existing, UserPojo updated) {
        existing.setEmail(updated.getEmail());
        existing.setPassword(updated.getPassword());
        existing.setRole(updated.getRole());
    }
}
