package com.rama.weatherviewer.repository;

import com.rama.weatherviewer.entity.Location;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LocationRepository extends AbstractHibernateRepository<Location, Long> {
    protected LocationRepository() {
        super(Location.class);
    }

    public List<Location> findByUsername(Long userId) {
        return entityManager.createQuery(
                "SELECT loc FROM Location loc WHERE loc.user.id = :userId", Location.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
