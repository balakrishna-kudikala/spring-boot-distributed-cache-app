package com.springboot.xoriant.distributed.cache.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactory",
        basePackages = {"com.springboot.xoriant.distributed.cache.repository"})
public class JPAConfig {

    @Value("${jpa.datasource.url}")
    private String databaseUrl;

    @Value("${jpa.datasource.username}")
    private String username;

    @Value("${jpa.datasource.password}")
    private String password;

    @Value("${jpa.datasource.driverClassName}")
    private String driverClassName;

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "jpa.datasource")
    public DataSource jpaDataSource(){
        return DataSourceBuilder.create().driverClassName(driverClassName).url(databaseUrl).username(username).password(password).build();
    }

    @Bean(name = "entityManager")
    public EntityManager entityManager() {
        return entityManagerFactory(jpaDataSource()).createEntityManager();
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public EntityManagerFactory entityManagerFactory(DataSource jpaDataSource) {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.H2);
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(jpaDataSource());
        emf.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        emf.setPackagesToScan("com.springboot.xoriant.distributed.cache.model");
        emf.setPersistenceUnitName("jpaDatasource");
        emf.afterPropertiesSet();
        return emf.getObject();
    }
}
