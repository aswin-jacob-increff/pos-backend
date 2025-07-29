package org.example.dao;

import org.example.pojo.UserPojo;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class UserDao extends AbstractDao<UserPojo> {

    public UserDao() {
        super(UserPojo.class);
    }

    public UserPojo getByEmail(String email) {
        List<UserPojo> results = getByParams("email", email);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    protected void updateEntity(UserPojo existing, UserPojo updated) {
        existing.setEmail(updated.getEmail());
        existing.setPassword(updated.getPassword());
        existing.setRole(updated.getRole());
    }
}
