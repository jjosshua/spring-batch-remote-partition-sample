package com.example.spring.batch.learn.samples.config.common;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class BatchIncrementer implements JobParametersIncrementer {

    @Override
    public JobParameters getNext(JobParameters parameters){
        return new JobParametersBuilder()
                .addLong("batchDate",new Date().getTime())
                .toJobParameters();
    }
}