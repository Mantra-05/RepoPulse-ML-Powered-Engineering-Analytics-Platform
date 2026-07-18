package com.repopulse.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * GitHub REST API v3 commit list item.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubCommitDto {

    private String sha;
    private GitHubCommitDetailDto commit;
    private GitHubUserDto author;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubCommitDetailDto {
        private String message;
        private GitHubCommitAuthorDto author;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GitHubCommitAuthorDto {
        private String name;
        private String email;

        @JsonProperty("date")
        private OffsetDateTime date;
    }
}
