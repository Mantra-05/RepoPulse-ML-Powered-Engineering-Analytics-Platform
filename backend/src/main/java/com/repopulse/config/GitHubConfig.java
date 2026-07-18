package com.repopulse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * Configures a {@link RestTemplate} bean pre-wired for the GitHub REST API v3.
 *
 * <p>If a Personal Access Token is present in {@code github.token},
 * it is injected as a {@code Authorization: Bearer …} header so that the app
 * benefits from the higher rate limit (5 000 req/hr vs 60 req/hr unauthenticated).
 */
@Configuration
public class GitHubConfig {

    @Value("${github.token:}")
    private String githubToken;

    @Bean(name = "gitHubRestTemplate")
    public RestTemplate gitHubRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            if (githubToken != null && !githubToken.isBlank()) {
                request.getHeaders().setBearerAuth(githubToken);
            }
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
