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
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class DatabaseServiceImpl implements DatabaseService {

    public static final int MAX_COLUMN_CHART = 7;

    private static final ReentrantReadWriteLock emLocker = new ReentrantReadWriteLock();
    private static final Lock readLocker = emLocker.readLock();
    private static final Lock writeLocker = emLocker.writeLock();

    private void execute(Consumer<EntityManager> doIt) {
        try (EntityManager em = DBConfigurate.getEntityManager()) {
            em.getTransaction().begin();
            doIt.accept(em);
            em.getTransaction().commit();
        }
    }

    private <T> T execute(Function<EntityManager, T> doIt) {
        T rez;
        try (EntityManager em = DBConfigurate.getEntityManager()) {
            em.getTransaction().begin();
            rez = doIt.apply(em);
            em.getTransaction().commit();
        }
        return rez;
    }

    @Override
    public LastUserMsg getByUserId(Long userId) {
        List<LastUserMsg> lastUserMsgs;
        readLocker.lock();
        try {
            lastUserMsgs = execute(entityManager -> {
                return entityManager.createQuery("select lum from LastUserMsg lum where lum.userId = ?1", LastUserMsg.class)
                        .setParameter(1, userId)
                        .getResultList();
            });
        } finally {
            readLocker.unlock();
        }

        if (lastUserMsgs == null || lastUserMsgs.isEmpty()) {
            return null;
        }

        if (lastUserMsgs.size() > 1) {
            Set<Long> idForRemove = lastUserMsgs.parallelStream().map(LastUserMsg::getId).collect(Collectors.toSet());
            writeLocker.lock();
            try {
                execute(entityManager -> {
                    entityManager.createQuery("delete from LastUserMsg lum where lum.id in (?1)")
                            .setParameter(1, idForRemove)
                            .executeUpdate();
                });
            } finally {
                writeLocker.unlock();
            }
        } else {
            return lastUserMsgs.get(0);
        }
        return null;
    }

    @Override
    public void saveLastUserMessage(LastUserMsg lastUserMsg) {
        writeLocker.lock();
        try {
            execute(entityManager -> {
                entityManager.persist(lastUserMsg);
            });
        } finally {
            writeLocker.unlock();
        }
    }

    @Override
    public void delete(LastUserMsg lastUserMsg) {
        if (lastUserMsg == null) {
            return;
        }
        writeLocker.lock();
        try {
            execute(entityManager -> {
                entityManager.remove(lastUserMsg);
            });
        } finally {
            writeLocker.unlock();
        }
    }

    @Override
    public void saveUserChartData(UserChart userChart) {
        writeLocker.lock();
        try {
            execute(entityManager -> {
                entityManager.persist(userChart);
            });
        } finally {
            writeLocker.unlock();
        }
    }


    @Override
    public PeeAndPoopData getUsrData(Long userId) {
        List<?> rez;

        readLocker.lock();
        try {
            rez = execute(entityManager -> {
                return entityManager.createNativeQuery(
                                """
                                        select date, data, count(*) from user_chart
                                        where userId = ?1
                                        group by date, data
                                        order by date desc
                                        limit ?2
                                        """
                        )
                        .setParameter(1, userId)
                        .setParameter(2, MAX_COLUMN_CHART * 2) // pee + poop data
                        .getResultList();
            });
        } finally {
            readLocker.unlock();
        }

        LocalDate now = LocalDate.now();
        int[] pee = new int[MAX_COLUMN_CHART];
        int[] poop = new int[MAX_COLUMN_CHART];
        for (Object o : rez) {
            Object[] line = (Object[]) o;
            LocalDate date = (LocalDate) line[0];
            Byte type = (Byte) line[1];
            Integer count = (Integer) line[2];

            int i = (int) (now.toEpochDay() - date.toEpochDay());
            if (i < MAX_COLUMN_CHART) {
                if (type == PeePoopEnum.PEE.ordinal()) {
                    pee[i] = count;
                } else {
                    poop[i] = count;
                }
            }
        }

        return new PeeAndPoopData(pee, poop);
    }
}
