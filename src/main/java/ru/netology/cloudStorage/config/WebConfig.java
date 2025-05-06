package ru.netology.cloudStorage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cross.origin.host.name}")
    private String origins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configuring CORS with allowed origins: {}", origins);
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOrigins(origins)
                .allowedMethods("*")
                .allowedHeaders("*");
        log.info("CORS configuration applied.");
    }
}
