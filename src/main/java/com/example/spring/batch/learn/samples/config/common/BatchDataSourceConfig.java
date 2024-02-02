package com.example.spring.batch.learn.samples.config.common;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchDataSourceConfig {

    @ConditionalOnProperty(value = "database.building.type", havingValue = "embedded")
    public class EmbeddedDataSourceConfig {
        @Bean
        public DataSource batchDataSource() {
            return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
                    .addScript("/org/springframework/batch/core/schema-h2.sql")
                    .setName("spring-batch")
                    .build();
        }
    }

    @ConditionalOnProperty(value = "database.building.type", havingValue = "remote")
    public class RemoteDataSourceConfig {
        @Bean
        @ConfigurationProperties(prefix = "database.spring-batch")
        public DataSource batchDataSource() {
            return new HikariDataSource();
        }
    }

    @Bean
    public JdbcTransactionManager batchTransactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }
}
