package de.janchristoph.wait_for_database;

import com.zaxxer.hikari.pool.HikariPool;
import liquibase.Liquibase;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.DatabaseStartupValidator;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Configures spring boot to wait for a running database.
 */
@Configuration
public class WaitForDatabaseConfiguration {

    @Bean
    public DatabaseStartupValidator databaseStartupValidator(DataSource dataSource) {
        DatabaseStartupValidator dsv = new DatabaseStartupValidator();
        dsv.setDataSource(dataSource);
        return dsv;
    }

    @Bean
    public static BeanFactoryPostProcessor dependsOnPostProcessor() {
        return new BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory bf) throws BeansException {
                // Let beans that need the database depend on the DatabaseStartupValidator
                // like the JPA EntityManagerFactory or Flyway
                String[] liquibase = bf.getBeanNamesForType(Liquibase.class);
                for (String str : liquibase) {
                    bf.getBeanDefinition(str).setDependsOn("databaseStartupValidator");
                }

                String[] jpa = bf.getBeanNamesForType(EntityManagerFactory.class);
                for (String str : jpa) {
                    bf.getBeanDefinition(str).setDependsOn("databaseStartupValidator");
                }

                String[] hikari = bf.getBeanNamesForType(HikariPool.class);
                for (String str : hikari) {
                    bf.getBeanDefinition(str).setDependsOn("databaseStartupValidator");
                }
            }
        };
    }

}
