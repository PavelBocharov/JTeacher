package com.mar.config;

import com.mar.model.LastUserMsg;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

@Slf4j
@UtilityClass
public class DBConfigurate {

    public static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();

            configuration.addAnnotatedClass(LastUserMsg.class);

            return configuration.buildSessionFactory(serviceRegistry);
        } catch (Exception e) {
            log.error("Cannot create Session factory for H2.", e);
            throw new RuntimeException("There is issue in hibernate util");
        }
    }

}
