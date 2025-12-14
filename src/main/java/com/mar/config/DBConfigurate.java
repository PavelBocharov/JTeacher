package com.mar.config;

import com.mar.StartAppCommand;
import com.mar.model.LastUserMsg;
import com.mar.model.Question;
import com.mar.model.Type;
import com.mar.model.UserChart;
import jakarta.persistence.EntityManager;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.sqlite.SQLiteDataSource;

@Slf4j
@UtilityClass
public class DBConfigurate {

    private static SessionFactory sessionFactory;

    static {
        String rootDir = System.getProperty(StartAppCommand.ROOT_DIR);
        String dbFile = rootDir.endsWith("/") ? rootDir + "library.db" : rootDir + "/library.db";
        log.debug("DataBase file: {}", dbFile);
        try {
            Configuration configuration = new Configuration();
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .applySetting("hibernate.connection.url", "jdbc:sqlite:" + dbFile)
                    .applySetting("type", SQLiteDataSource.class)
                    .build();

            configuration.addAnnotatedClasses(
                    LastUserMsg.class, Type.class, Question.class, UserChart.class
            );

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Exception e) {
            log.error("Cannot create Session factory.", e);
            throw new RuntimeException(e);
        }
    }

    public static EntityManager getEntityManager() {
        return sessionFactory.createEntityManager();
    }


    public static void shutdown() {
        if (sessionFactory != null) sessionFactory.close();
    }

}
