package com.mar.config;

import com.mar.StartAppCommand;
import com.mar.model.LastUserMsg;
import com.mar.model.Question;
import com.mar.model.Type;
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
        String rootDir = System.getProperty(StartAppCommand.ROOT_DIR);
        String dbFile = rootDir.endsWith("/") ? rootDir + "h2" : rootDir + "/h2";
        log.debug("DataBase file: {}", dbFile);
        try {
            Configuration configuration = new Configuration();
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .applySetting("hibernate.connection.url", "jdbc:h2:file:" + dbFile)
                    .build();

            configuration.addAnnotatedClasses(
                    LastUserMsg.class, Type.class, Question.class
            );

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Exception e) {
            log.error("Cannot create Session factory for H2.", e);
            throw new RuntimeException("There is issue in hibernate util");
        }
    }

}
