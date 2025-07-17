package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.example.model.constants.CorsConstants;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(CorsConstants.Mapping.MAPPING)
                .allowedOrigins(CorsConstants.Origin.DEFAULT)
                .allowedMethods(CorsConstants.Methods.GET,
                        CorsConstants.Methods.POST,
                        CorsConstants.Methods.PUT,
                        CorsConstants.Methods.DELETE,
                        CorsConstants.Methods.OPTIONS)
                .allowedHeaders(CorsConstants.Headers.ALL)
                .allowCredentials(true); // <-- This is required for cookies!
    }
}
