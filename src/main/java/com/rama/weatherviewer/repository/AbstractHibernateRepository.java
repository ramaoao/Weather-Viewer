package com.rama.weatherviewer.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.Serializable;
import java.util.Optional;

public abstract class AbstractHibernateRepository<T, ID extends Serializable> {
    @PersistenceContext
    protected EntityManager entityManager;
    protected Class<T> clazz;

    protected AbstractHibernateRepository(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void save(T entity) {
        entityManager.persist(entity);
    }

    public void saveAndFlush(T entity) {
        save(entity);
        entityManager.flush();
    }

    public Optional<T> find(ID id) {
        return Optional.ofNullable(entityManager.find(clazz, id));
    }

    public void deleteById(ID id) {
        T entity = entityManager.getReference(clazz, id);
        entityManager.remove(entity);
    }

    public T getReference(ID id) {
        return entityManager.getReference(clazz, id);
    }
}
