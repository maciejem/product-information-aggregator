package com.example.aggregator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public Executor upstreamCallExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
