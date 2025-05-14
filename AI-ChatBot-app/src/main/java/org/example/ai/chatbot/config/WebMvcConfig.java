package org.example.ai.chatbot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // Use your existing ThreadPoolExecutor for async web requests
        configurer.setTaskExecutor(new TaskExecutorAdapter(threadPoolExecutor));
        // Set a reasonable timeout for async requests (in milliseconds)
        configurer.setDefaultTimeout(30000);
    }
}