package com.repopulse.service.impl;

import com.repopulse.dto.response.CommitResponse;
import com.repopulse.exception.ResourceNotFoundException;
import com.repopulse.mapper.CommitMapper;
import com.repopulse.repository.CommitRepository;
import com.repopulse.service.CommitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommitServiceImpl implements CommitService {

    private final CommitRepository commitRepository;
    private final CommitMapper commitMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CommitResponse> getCommitsByRepository(Long repositoryId) {
        return commitRepository.findAllByRepositoryId(repositoryId)
                .stream()
                .map(commitMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommitResponse getCommitBySha(String sha) {
        return commitRepository.findBySha(sha)
                .map(commitMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Commit", "sha", sha));
    }
}
