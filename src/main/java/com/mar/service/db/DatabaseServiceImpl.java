package com.mar.service.db;

import com.mar.config.DBConfigurate;
import com.mar.model.LastUserMsg;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseServiceImpl implements DatabaseService {

    @Override
    public LastUserMsg getByUserId(Long userId) {
        return DBConfigurate.getEntityManager()
                .createQuery("select lum from LastUserMsg lum where lum.userId = ?1", LastUserMsg.class)
                .setParameter(1, userId)
                .getSingleResult();
    }

    @Override
    public void saveLastUserMessage(LastUserMsg lastUserMsg) {
        EntityManager em = DBConfigurate.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(lastUserMsg);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(LastUserMsg lastUserMsg) {
        if (lastUserMsg == null) {
            return;
        }
        EntityManager em = DBConfigurate.getEntityManager();
        try {
            em.getTransaction().begin();
            em.remove(lastUserMsg);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException(e);
        }
    }
}
