package com.pa.spring.prueba1.pa_prueba1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuración de la base de datos para la aplicación Spring.
 * Define los beans necesarios para la conexión a la base de datos,
 * gestión de entidades JPA y manejo de transacciones.
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    /**
     * Permite acceder a las propiedades definidas en application.properties o application.yml
     */
    @Autowired
    private Environment env;

    /**
     * Define el bean de DataSource que establece la conexión con la base de datos.
     *
     * @return DataSource configurado con las propiedades del archivo de configuración.
     */
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        return dataSource;
    }

    /**
     * Configura el EntityManagerFactory de JPA, que se encarga de gestionar las entidades y su persistencia.
     *
     * @return Bean de LocalContainerEntityManagerFactoryBean con adaptador Hibernate.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        // Se establece el DataSource
        em.setDataSource(dataSource());

        // Se define el paquete donde están las entidades JPA
        em.setPackagesToScan("com.pa.spring.prueba1.pa_prueba1.model");

        // Se especifica el proveedor JPA (Hibernate)
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        // Propiedades específicas de Hibernate
        Properties properties = new Properties();
        properties.put("hibernate.dialect", env.getProperty("spring.jpa.properties.hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("spring.jpa.hibernate.ddl-auto"));
        properties.put("hibernate.show_sql", env.getProperty("spring.jpa.show-sql"));
        properties.put("hibernate.format_sql", env.getProperty("spring.jpa.properties.hibernate.format_sql"));

        em.setJpaProperties(properties);

        return em;
    }

    /**
     * Define el manejador de transacciones que utilizará JPA.
     *
     * @return PlatformTransactionManager con soporte para JPA.
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
}
