package com.clara.ops.challenge.document_management_service_challenge.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Value("${upload.executor.pool.size:10}")
    private  int uploadPoolSize ;

    @Bean(name = "uploadExecutor")
    public ExecutorService uploadExecutor() {
        return Executors
                .newFixedThreadPool(uploadPoolSize); // Max 10 parallel uploads
    }
}
