/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.spring.batch.learn.samples.ui;

import com.example.spring.batch.learn.samples.config.common.BatchIncrementer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"mix", "master"})
@RequestMapping("endpoints")
public class EndpointController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private BatchIncrementer batchIncrementer;

    @Qualifier("simpleJob")
    @Autowired
    private Job simpleJob;

    @Qualifier("remotePartitionJob")
    @Autowired
    private Job remotePartitionJob;

    @GetMapping(value = "/simpleJob")
    @ResponseBody
    public JobExecution simpleJob() throws Exception {
        JobExecution result = jobLauncher.run(simpleJob, new JobParameters());
        return result;
    }

    @GetMapping(value = "/remotePartitionJob")
    @ResponseBody
    public String remotePartitionJob() throws Exception {
        JobExecution result = jobLauncher.run(remotePartitionJob, batchIncrementer.getNext(new JobParameters()));
        return result.toString();
    }
}
