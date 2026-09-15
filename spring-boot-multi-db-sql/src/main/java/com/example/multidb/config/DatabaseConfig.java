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

/**
 * 서로 독립된 H2 DB 3개의 연결 풀과 SQL 실행 도구를 등록합니다.
 * 빈 메서드끼리 직접 호출하지 않고 매개변수로 주입하므로 프록시가 필요하지 않습니다.
 */
@Configuration(proxyBeanMethods = false)
public class DatabaseConfig {
    /** 설정 파일의 상품 DB URL을 주입받으며, 컨테이너 종료 시 연결 풀을 닫습니다. */
    @Bean(destroyMethod = "close")
    HikariDataSource productsDataSource(@Value("${app.datasource.products-url}") String url) {
        return initializedDataSource(url, "products");
    }

    /** 설정 파일의 고객 DB URL을 주입받으며, 컨테이너 종료 시 연결 풀을 닫습니다. */
    @Bean(destroyMethod = "close")
    HikariDataSource customersDataSource(@Value("${app.datasource.customers-url}") String url) {
        return initializedDataSource(url, "customers");
    }

    /** 설정 파일의 주문 DB URL을 주입받으며, 컨테이너 종료 시 연결 풀을 닫습니다. */
    @Bean(destroyMethod = "close")
    HikariDataSource ordersDataSource(@Value("${app.datasource.orders-url}") String url) {
        return initializedDataSource(url, "orders");
    }

    /** 상품 DataSource만 사용하도록 지정한 JdbcTemplate을 등록합니다. */
    @Bean
    JdbcTemplate productsJdbcTemplate(@Qualifier("productsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /** 고객 DataSource만 사용하도록 지정한 JdbcTemplate을 등록합니다. */
    @Bean
    JdbcTemplate customersJdbcTemplate(@Qualifier("customersDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /** 주문 DataSource만 사용하도록 지정한 JdbcTemplate을 등록합니다. */
    @Bean
    JdbcTemplate ordersJdbcTemplate(@Qualifier("ordersDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /** 연결 풀을 구성하고 해당 DB의 테이블·초기 데이터를 준비한 뒤 반환합니다. */
    private HikariDataSource initializedDataSource(String url, String name) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        // 이 예제의 로컬 H2 메모리 DB에 사용할 계정입니다.
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setPoolName(name + "-pool");
        // DB별로 연결을 최대 4개까지 사용하고, 유휴 연결은 최소 1개를 유지하도록 합니다.
        dataSource.setMaximumPoolSize(4);
        dataSource.setMinimumIdle(1);
        try {
            // 클래스패스의 DB별 SQL을 실행하여 저장소가 사용되기 전에 초기화합니다.
            new ResourceDatabasePopulator(new ClassPathResource("db/" + name + ".sql"))
                    .execute(dataSource);
            return dataSource;
        } catch (RuntimeException exception) {
            // 빈 등록 도중 초기화가 실패해도 이미 만든 연결이 남지 않도록 정리합니다.
            dataSource.close();
            throw exception;
        }
    }
}
