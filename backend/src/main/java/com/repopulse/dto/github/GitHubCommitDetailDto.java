package com.repopulse.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GitHub REST API v3 single commit detail (includes diff stats).
 * Fetched via {@code GET /repos/{owner}/{repo}/commits/{sha}}.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubCommitDetailDto {

    private String sha;
    private GitHubCommitDto.GitHubCommitDetailDto commit;
    private GitHubUserDto author;
    private GitHubStatsDto stats;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubStatsDto {
        private Integer additions;
        private Integer deletions;
        private Integer total;
    }
}
