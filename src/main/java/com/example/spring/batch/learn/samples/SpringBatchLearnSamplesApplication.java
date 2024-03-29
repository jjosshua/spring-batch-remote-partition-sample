package com.example.spring.batch.learn.samples;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchIntegration
@EnableBatchProcessing(
        dataSourceRef = "batchDataSource", transactionManagerRef = "batchTransactionManager"
)
@SpringBootApplication
public class SpringBatchLearnSamplesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchLearnSamplesApplication.class, args);
    }

}
