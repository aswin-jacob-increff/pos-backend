package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /files/invoice/** to the invoice folder in src/main/resources/invoice/
        registry.addResourceHandler("/files/invoice/**")
                .addResourceLocations("file:src/main/resources/invoice/");
    }
}
