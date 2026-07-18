package com.repopulse.service.impl;

import com.repopulse.dto.response.ContributorResponse;
import com.repopulse.exception.ResourceNotFoundException;
import com.repopulse.mapper.ContributorMapper;
import com.repopulse.repository.ContributorRepository;
import com.repopulse.service.ContributorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributorServiceImpl implements ContributorService {

    private final ContributorRepository contributorRepository;
    private final ContributorMapper contributorMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ContributorResponse> getContributorsByRepository(Long repositoryId) {
        return contributorRepository.findAllByRepositoryId(repositoryId)
                .stream()
                .map(contributorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ContributorResponse getContributorByLogin(Long repositoryId, String login) {
        return contributorRepository.findByLoginAndRepositoryId(login, repositoryId)
                .map(contributorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Contributor", "login", login));
    }
}
