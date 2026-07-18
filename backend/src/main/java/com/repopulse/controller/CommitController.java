package com.repopulse.controller;

import com.repopulse.dto.response.ApiResponse;
import com.repopulse.dto.response.CommitResponse;
import com.repopulse.service.CommitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for querying commits within a repository.
 */
@RestController
@RequestMapping("/api/v1/repositories/{repositoryId}/commits")
@RequiredArgsConstructor
public class CommitController {

    private final CommitService commitService;

    /** GET /api/v1/repositories/{repositoryId}/commits */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommitResponse>>> getAll(
            @PathVariable Long repositoryId) {
        return ResponseEntity.ok(ApiResponse.success(
                commitService.getCommitsByRepository(repositoryId)));
    }

    /** GET /api/v1/repositories/{repositoryId}/commits/{sha} */
    @GetMapping("/{sha}")
    public ResponseEntity<ApiResponse<CommitResponse>> getBySha(
            @PathVariable Long repositoryId,
            @PathVariable String sha) {
        return ResponseEntity.ok(ApiResponse.success(commitService.getCommitBySha(sha)));
    }
}
