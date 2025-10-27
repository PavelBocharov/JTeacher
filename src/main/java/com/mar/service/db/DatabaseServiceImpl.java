package com.mar.service.db;

import com.mar.config.DBConfigurate;
import com.mar.model.LastUserMsg;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

@Slf4j
public class DatabaseServiceImpl implements DatabaseService {

    @Override
    public LastUserMsg getByUserId(Long userId) {
        try (SessionFactory sessionFactory = DBConfigurate.buildSessionFactory()) {
            Session session = sessionFactory.openSession();
            return session.createSelectionQuery("from LastUserMsg lum where lum.userId = " + userId, LastUserMsg.class)
                    .getSingleResultOrNull();
        }
    }

    @Override
    public void saveLastUserMessage(LastUserMsg lastUserMsg) {
        try (SessionFactory sessionFactory = DBConfigurate.buildSessionFactory()) {
            sessionFactory.inTransaction(session -> {
                session.createSelectionQuery("from LastUserMsg", LastUserMsg.class)
                        .getResultList()
                        .forEach(lum -> log.debug("Get last user msg: {}", lum));
                session.persist(lastUserMsg);
            });
        }
    }

    @Override
    public void delete(LastUserMsg lastUserMsg) {
        try (SessionFactory sessionFactory = DBConfigurate.buildSessionFactory()) {
            sessionFactory.inTransaction(session -> {
                if (lastUserMsg != null) {
                    session.remove(lastUserMsg);
                }
            });
        }
    }
}
