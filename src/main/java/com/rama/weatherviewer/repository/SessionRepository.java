package com.rama.weatherviewer.repository;

import com.rama.weatherviewer.entity.Session;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class SessionRepository extends AbstractHibernateRepository<Session, UUID> {
    public SessionRepository() {
        super(Session.class);
    }

    public int deleteExpiredSession() {
        return entityManager.createQuery(
                "DELETE FROM Session s WHERE s.expiresAt < :now")
                .setParameter("now", Instant.now())
                .executeUpdate();
    }
}
