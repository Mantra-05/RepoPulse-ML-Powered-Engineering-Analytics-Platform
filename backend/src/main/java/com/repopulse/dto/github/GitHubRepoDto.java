package com.repopulse.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GitHub REST API v3 repository response.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRepoDto {

    private Long id;
    private String name;

    @JsonProperty("full_name")
    private String fullName;

    private String description;
    private String language;

    @JsonProperty("private")
    private boolean privateRepo;

    @JsonProperty("default_branch")
    private String defaultBranch;

    @JsonProperty("stargazers_count")
    private Integer stargazersCount;

    @JsonProperty("forks_count")
    private Integer forksCount;

    @JsonProperty("open_issues_count")
    private Integer openIssuesCount;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("clone_url")
    private String cloneUrl;
}
