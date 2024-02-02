package com.example.spring.batch.learn.samples.config.partition.remote.aggregate;

import com.example.spring.batch.learn.samples.config.common.BatchIncrementer;
import com.example.spring.batch.learn.samples.config.partition.remote.RemotePartitionConstants;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.partition.RemotePartitioningManagerStepBuilderFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;

import java.util.HashMap;


@Configuration
@Profile({"master", "mix"})
@ConditionalOnProperty(value = "spring-batch.custom.remote-partition.type", havingValue = "aggregate")
public class RemotePartitionAggregateMasterConfig {

    @Autowired
    private BatchIncrementer batchIncrementer;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private RemotePartitioningManagerStepBuilderFactory managerStepBuilderFactory;

    @Bean
    public DirectChannel masterOutputChannel() {
        return new DirectChannel();
    }

    @Bean
    public DirectChannel masterInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public Job remotePartitionJob() {
        return new JobBuilder(RemotePartitionConstants.REMOTE_PARTITION_NAME, jobRepository)
                .start(masterStep())
                .incrementer(batchIncrementer)
                .build();
    }

    @Bean
    public Step masterStep() {
        var managerStepBuilder = this.managerStepBuilderFactory.get(RemotePartitionConstants.MASTER_STEP_NAME)
                .pollInterval(5000L)
                .partitioner(RemotePartitionConstants.SLAVE_STEP_NAME, mockPartitioner())
                .gridSize(10)
                .outputChannel(masterOutputChannel())
                .inputChannel(masterInputChannel());
        return managerStepBuilder.build();
    }

    @Bean
    public Partitioner mockPartitioner() {
        return gridSize -> {
            var map = new HashMap<String, ExecutionContext>();
            for (int i = 1; i <= gridSize; ++i) {
                ExecutionContext context = new ExecutionContext();
                context.put("partition-"+ i, i);
                map.put("partition-"+ i, context);
            }
            return map;
        };
    }

    @Bean
    @ServiceActivator(inputChannel = "masterOutputChannel")
    public AmqpOutboundEndpoint masterOutboundChannelAdapter(AmqpTemplate template) {
        var masterOutboundChannelAdapter = new AmqpOutboundEndpoint(template);
        masterOutboundChannelAdapter.setRoutingKey(RemotePartitionConstants.REQUEST_QUEUE_NAME);
        return masterOutboundChannelAdapter;
    }

    @Bean
    public AmqpInboundChannelAdapter masterInboundChannelAdapter(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        listenerContainer.setQueueNames(RemotePartitionConstants.REPlY_QUEUE_NAME);
        listenerContainer.setConcurrentConsumers(2);
        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(listenerContainer);
        adapter.setOutputChannel(masterInputChannel());
        return adapter;
    }
}
