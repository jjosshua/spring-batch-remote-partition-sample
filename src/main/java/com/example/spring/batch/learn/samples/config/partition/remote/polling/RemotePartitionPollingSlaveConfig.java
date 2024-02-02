package com.example.spring.batch.learn.samples.config.partition.remote.polling;

import com.example.spring.batch.learn.samples.config.partition.remote.RemotePartitionConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.partition.BeanFactoryStepLocator;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Slf4j
@Configuration
@Profile({"slave", "mix"})
@ConditionalOnProperty(value = "spring-batch.custom.remote-partition.type", havingValue = "polling")
public class RemotePartitionPollingSlaveConfig {

    @Autowired
    private RemotePartitioningWorkerStepBuilderFactory workerStepBuilderFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public DirectChannel slaveInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public NullChannel slaveOutputChannel() {
        return new NullChannel();
    }

    @Bean
    public Step slaveStep() {
        TaskletStep slaveStep = workerStepBuilderFactory.get("slaveStep")
                .inputChannel(slaveInputChannel())
                .tasklet(tasklet(), transactionManager)
                .build();
        return slaveStep;
    }

    @Bean
    public Tasklet tasklet() {
        return (stepContribution, chunkContext) -> {
            Map<String, Object> stepExecutionContext = chunkContext.getStepContext().getStepExecutionContext();
            log.info("查看stepExecutionContext内容"+stepExecutionContext);
            Thread.sleep(8000L);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public AmqpInboundChannelAdapter slaveInboundChannelAdapter(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        listenerContainer.setQueueNames(RemotePartitionConstants.REQUEST_QUEUE_NAME);
        listenerContainer.setConcurrentConsumers(2);
        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(listenerContainer);
        adapter.setOutputChannel(slaveInputChannel());
        return adapter;
    }

}
