package com.mar.service.db;

import com.mar.config.DBConfigurate;
import com.mar.data.PeeAndPoopData;
import com.mar.model.LastUserMsg;
import com.mar.model.PeePoopEnum;
import com.mar.model.UserChart;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
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

    @Override
    public void saveUserChartData(UserChart userChart) {
        try (EntityManager em = DBConfigurate.getEntityManager()) {
            em.getTransaction().begin();
            em.persist(userChart);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PeeAndPoopData getUsrData(Long userId) {
        List<?> rez;

        try (EntityManager em = DBConfigurate.getEntityManager()) {
            em.getTransaction().begin();

            rez = em.createNativeQuery(
                            """
                                    select date, data, count(*) from user_chart
                                    where userId = ?1
                                    group by date, data
                                    order by date desc
                                    limit 14
                                    """
                    ).setParameter(1, userId)
                    .getResultList();

            em.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LocalDate now = LocalDate.now();
        int[] pee = new int[7];
        int[] poop = new int[7];
        for (Object o : rez) {
            Object[] line = (Object[]) o;
            LocalDate date = (LocalDate) line[0];
            Byte type = (Byte) line[1];
            Integer count = (Integer) line[2];

            int i = now.getDayOfYear() - date.getDayOfYear();
            if (type == PeePoopEnum.PEE.ordinal()) {
                pee[i] = count;
            } else {
                poop[i] = count;
            }
        }

        return new PeeAndPoopData(pee, poop);
    }
}
