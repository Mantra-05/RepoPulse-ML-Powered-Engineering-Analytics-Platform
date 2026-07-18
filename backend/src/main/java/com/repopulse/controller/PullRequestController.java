package com.repopulse.controller;

import com.repopulse.dto.response.ApiResponse;
import com.repopulse.dto.response.PullRequestResponse;
import com.repopulse.service.PullRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for querying pull requests within a repository.
 */
@RestController
@RequestMapping("/api/v1/repositories/{repositoryId}/pull-requests")
@RequiredArgsConstructor
public class PullRequestController {

    private final PullRequestService pullRequestService;

    /** GET /api/v1/repositories/{repositoryId}/pull-requests */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PullRequestResponse>>> getAll(
            @PathVariable Long repositoryId,
            @RequestParam(required = false) String state) {

        List<PullRequestResponse> prs = (state != null)
                ? pullRequestService.getPullRequestsByState(repositoryId, state)
                : pullRequestService.getPullRequestsByRepository(repositoryId);

        return ResponseEntity.ok(ApiResponse.success(prs));
    }

    /** GET /api/v1/repositories/{repositoryId}/pull-requests/{prId} */
    @GetMapping("/{prId}")
    public ResponseEntity<ApiResponse<PullRequestResponse>> getById(
            @PathVariable Long repositoryId,
            @PathVariable Long prId) {

        PullRequestResponse pr = pullRequestService.getPullRequestById(prId);
        return ResponseEntity.ok(ApiResponse.success(pr));
    }
}
