package com.repopulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * General-purpose {@link RestTemplate} bean used by internal service-to-service
 * calls (e.g. Spring Boot → FastAPI ML service).
 *
 * <p>The GitHub-specific template is defined in {@link GitHubConfig}.
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
