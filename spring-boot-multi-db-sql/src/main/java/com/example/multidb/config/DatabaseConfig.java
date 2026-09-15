package com.example.multidb.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration(proxyBeanMethods = false)
public class DatabaseConfig {
    @Bean(destroyMethod = "close")
    HikariDataSource productsDataSource(@Value("${app.datasource.products-url}") String url) {
        return initializedDataSource(url, "products");
    }

    @Bean(destroyMethod = "close")
    HikariDataSource customersDataSource(@Value("${app.datasource.customers-url}") String url) {
        return initializedDataSource(url, "customers");
    }

    @Bean(destroyMethod = "close")
    HikariDataSource ordersDataSource(@Value("${app.datasource.orders-url}") String url) {
        return initializedDataSource(url, "orders");
    }

    @Bean
    JdbcTemplate productsJdbcTemplate(@Qualifier("productsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    JdbcTemplate customersJdbcTemplate(@Qualifier("customersDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    JdbcTemplate ordersJdbcTemplate(@Qualifier("ordersDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private HikariDataSource initializedDataSource(String url, String name) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setPoolName(name + "-pool");
        dataSource.setMaximumPoolSize(4);
        dataSource.setMinimumIdle(1);
        try {
            new ResourceDatabasePopulator(new ClassPathResource("db/" + name + ".sql"))
                    .execute(dataSource);
            return dataSource;
        } catch (RuntimeException exception) {
            dataSource.close();
            throw exception;
        }
    }
}
