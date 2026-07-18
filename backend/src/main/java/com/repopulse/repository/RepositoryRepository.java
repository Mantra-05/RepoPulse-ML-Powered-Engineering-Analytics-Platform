package com.repopulse.repository;

import com.repopulse.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    List<Repository> findAllByOwnerId(Long ownerId);

    Optional<Repository> findByGithubId(Long githubId);

    Optional<Repository> findByFullName(String fullName);

    boolean existsByGithubId(Long githubId);
}
