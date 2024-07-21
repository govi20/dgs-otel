package com.sample.dgs_otel.executor;

import io.opentelemetry.context.Context;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean("dgsAsyncTaskExecutor")
    public ExecutorService dgsAsyncTaskExecutor() {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        return Context.taskWrapping(executorService);
    }

    @Bean
    public AsyncTaskExecutor asyncTaskExecutor(@Qualifier("dgsAsyncTaskExecutor") ExecutorService dgsAsyncExecutor) {
        return new TaskExecutorAdapter(dgsAsyncExecutor);
    }

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer(
            @Qualifier("dgsAsyncTaskExecutor") ExecutorService dgsAsyncExecutor) {
        return protocolHandler -> protocolHandler.setExecutor(dgsAsyncExecutor);

    }
}
