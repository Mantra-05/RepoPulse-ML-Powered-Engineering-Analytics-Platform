package com.repopulse.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GitHub REST API v3 contributor list item.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubContributorDto {

    private Long id;
    private String login;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("html_url")
    private String htmlUrl;

    private Integer contributions;
}
