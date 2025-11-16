package com.mar.service.db;

import com.mar.config.DBConfigurate;
import com.mar.model.LastUserMsg;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DatabaseServiceImpl implements DatabaseService {

    @Override
    public LastUserMsg getByUserId(Long userId) {
        try (EntityManager em = DBConfigurate.getEntityManager()) {

            List<LastUserMsg> lums = em.createQuery("select lum from LastUserMsg lum where lum.userId = ?1", LastUserMsg.class)
                    .setParameter(1, userId)
                    .getResultList();

            if (lums == null || lums.isEmpty()) {
                return null;
            }

            if (lums.size() > 1) {
                em.getTransaction().begin();
                for (LastUserMsg lum : lums) {
                    em.remove(lum);
                }
                em.getTransaction().commit();
            } else {
                return lums.get(0);
            }
        } catch (Exception e) {
            log.error("Cannot get last user msg by ID = {}", userId, e);
        }
        return null;
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
