package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.client.RestTemplate;


@Configuration
@ComponentScan(basePackages = {"org.example.config", "org.example.controller", "org.example.api", "org.example.pojo", "org.example.dao", "org.example.model", "org.example.dto", "org.example.flow", "org.example.exception", "org.springdoc"})
@Import({ DbConfig.class, ControllerConfig.class, SecurityConfig.class})
@EnableScheduling
public class SpringConfig {
    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
