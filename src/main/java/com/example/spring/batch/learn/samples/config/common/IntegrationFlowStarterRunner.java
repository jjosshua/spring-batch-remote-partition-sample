package com.example.spring.batch.learn.samples.config.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IntegrationFlowStarterRunner implements ApplicationRunner, ApplicationContextAware {

    @Autowired
    private BeanFactory beanFactory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Application started, integrationFlows are going to start.");
        var integrationFlowContext = beanFactory.getBean(IntegrationFlowContext.class);
        integrationFlowContext.getRegistry().entrySet().forEach(e-> {
            log.info("integrationFlow [" + e.getKey() + "] is going to start.");
            e.getValue().start();
        });
        log.info("IntegrationFlows are started. Let's rock!");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.beanFactory = applicationContext;
    }
}