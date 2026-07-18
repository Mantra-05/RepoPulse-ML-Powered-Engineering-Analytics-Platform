package com.repopulse.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Nested branch-reference object inside a GitHub PR ({@code base} / {@code head}).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubBranchRefDto {

    private String label;
    private String ref;
    private String sha;
}
