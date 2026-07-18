package com.repopulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullRequestResponse {

    private Long id;
    private Integer githubPrNumber;
    private String title;
    private String body;
    private String state;
    private String authorLogin;
    private String baseBranch;
    private String headBranch;
    private Integer changedFiles;
    private Integer additions;
    private Integer deletions;
    private Integer reviewComments;
    private Integer comments;
    private Integer commitsCount;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private LocalDateTime mergedAt;
    private Double riskScore;
    private Double predictedReviewTimeHours;
}
