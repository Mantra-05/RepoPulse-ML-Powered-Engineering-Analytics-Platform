package com.repopulse.service.impl;

import com.repopulse.dto.response.PullRequestResponse;
import com.repopulse.entity.PullRequest.PrState;
import com.repopulse.exception.ResourceNotFoundException;
import com.repopulse.mapper.PullRequestMapper;
import com.repopulse.repository.PullRequestRepository;
import com.repopulse.service.PullRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PullRequestServiceImpl implements PullRequestService {

    private final PullRequestRepository pullRequestRepository;
    private final PullRequestMapper pullRequestMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestResponse> getPullRequestsByRepository(Long repositoryId) {
        return pullRequestRepository.findAllByRepositoryId(repositoryId)
                .stream()
                .map(pullRequestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PullRequestResponse getPullRequestById(Long pullRequestId) {
        return pullRequestRepository.findById(pullRequestId)
                .map(pullRequestMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("PullRequest", "id", pullRequestId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestResponse> getPullRequestsByState(Long repositoryId, String state) {
        PrState prState = PrState.valueOf(state.toUpperCase());
        return pullRequestRepository.findAllByRepositoryIdAndState(repositoryId, prState)
                .stream()
                .map(pullRequestMapper::toResponse)
                .toList();
    }
}
