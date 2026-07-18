package com.repopulse.repository;

import com.repopulse.entity.Contributor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributorRepository extends JpaRepository<Contributor, Long> {

    List<Contributor> findAllByRepositoryId(Long repositoryId);

    Optional<Contributor> findByLoginAndRepositoryId(String login, Long repositoryId);

    boolean existsByLoginAndRepositoryId(String login, Long repositoryId);

    long countByRepositoryId(Long repositoryId);
}
