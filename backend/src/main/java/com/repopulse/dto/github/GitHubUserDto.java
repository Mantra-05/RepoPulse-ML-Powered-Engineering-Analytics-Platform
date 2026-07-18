package com.repopulse.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Nested GitHub user object embedded in pull requests and commits.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubUserDto {

    private Long id;
    private String login;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("html_url")
    private String htmlUrl;
}
