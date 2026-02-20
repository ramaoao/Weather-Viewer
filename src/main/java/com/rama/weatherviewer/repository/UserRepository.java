package com.rama.weatherviewer.repository;

import com.rama.weatherviewer.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository extends AbstractHibernateRepository<User, Long> {
    public UserRepository() {
        super(User.class);
    }

    public Optional<User> findByUsername(String username) {
            return entityManager.createQuery(
                    "SELECT user FROM User user WHERE user.username = :username", User.class)
                    .setParameter("username", username)
                    .setMaxResults(1)
                    .getResultList()
                    .stream()
                    .findFirst();
    }
}
