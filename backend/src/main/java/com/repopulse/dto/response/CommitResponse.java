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
public class CommitResponse {

    private Long id;
    private String sha;
    private String message;
    private String authorLogin;
    private String authorName;
    private String authorEmail;
    private LocalDateTime committedAt;
    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;
}
