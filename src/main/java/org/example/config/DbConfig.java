package org.example.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.example.model.constants.DbConstants;

@Configuration
@EnableTransactionManagement
public class DbConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(DbConstants.DbSourceData.DRIVER_CLASS_NAME);
        ds.setUrl(DbConstants.DbSourceData.URL);
        ds.setUsername(DbConstants.DbSourceData.USERNAME);
        ds.setPassword(DbConstants.DbSourceData.PASSWORD);
        return ds;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(dataSource());
        factory.setPackagesToScan(DbConstants.EntityPackage.PACKAGE_TO_SCAN);
        factory.setHibernateProperties(hibernateProperties());
        return factory;
    }

    private Properties hibernateProperties() {
        Properties props = new Properties();
        props.put(DbConstants.HibernateProperties.DIALECT_PROPERTY_1, DbConstants.HibernateProperties.DIALECT_PROPERTY_2);
        props.put(DbConstants.HibernateProperties.HIBERNATE_PROPERTY_1, DbConstants.HibernateProperties.HIBERNATE_PROPERTY_2);
        props.put(DbConstants.HibernateProperties.SQL_PROPERTY_1, DbConstants.HibernateProperties.SQL_PROPERTY_2);
        return props;
    }

    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }
}

