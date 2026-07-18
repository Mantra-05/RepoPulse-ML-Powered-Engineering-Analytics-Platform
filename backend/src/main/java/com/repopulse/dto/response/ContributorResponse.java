package com.repopulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributorResponse {

    private Long id;
    private String login;
    private String avatarUrl;
    private String githubUrl;
    private Integer contributionsCount;
    private Integer totalAdditions;
    private Integer totalDeletions;
    private Integer pullRequestsOpened;
    private Integer pullRequestsMerged;
}
