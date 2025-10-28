package com.mar.config;

import com.mar.model.LastUserMsg;
import jakarta.persistence.EntityManager;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

@Slf4j
@UtilityClass
public class DBConfigurate {

    private static SessionFactory sessionFactory;
    private static EntityManager entityManager;

    public static EntityManager getEntityManager() {
        synchronized (DBConfigurate.class) {
            if (entityManager == null) {
                entityManager = getSessionFactory().createEntityManager();
            }
            return entityManager;
        }
    }

    private static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            initSessionFactory();
        }
        return sessionFactory;
    }

    private void initSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();

            configuration.addAnnotatedClass(LastUserMsg.class);

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Exception e) {
            log.error("Cannot create Session factory for H2.", e);
            throw new RuntimeException("There is issue in hibernate util");
        }
    }

}
