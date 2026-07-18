package com.repopulse.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * GitHub REST API v3 pull-request list item.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPullRequestDto {

    private Long id;
    private Integer number;
    private String title;
    private String body;
    private String state;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("closed_at")
    private OffsetDateTime closedAt;

    @JsonProperty("merged_at")
    private OffsetDateTime mergedAt;

    private GitHubUserDto user;

    private GitHubBranchRefDto base;

    private GitHubBranchRefDto head;

    @JsonProperty("review_comments")
    private Integer reviewComments;

    private Integer comments;
    private Integer commits;
    private Integer additions;
    private Integer deletions;

    @JsonProperty("changed_files")
    private Integer changedFiles;
}
