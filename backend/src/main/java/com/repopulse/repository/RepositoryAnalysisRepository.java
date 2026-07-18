package com.repopulse.repository;

import com.repopulse.entity.RepositoryAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositoryAnalysisRepository extends JpaRepository<RepositoryAnalysis, Long> {

    Optional<RepositoryAnalysis> findByRepositoryId(Long repositoryId);

    boolean existsByRepositoryId(Long repositoryId);
}
