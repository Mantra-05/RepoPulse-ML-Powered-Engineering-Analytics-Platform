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
public class RepositoryResponse {

    private Long id;
    private Long githubId;
    private String name;
    private String fullName;
    private String description;
    private String language;
    private boolean privateRepo;
    private String defaultBranch;
    private Integer starsCount;
    private Integer forksCount;
    private Integer openIssuesCount;
    private String githubUrl;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime createdAt;
}
