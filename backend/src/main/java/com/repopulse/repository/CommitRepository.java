package com.repopulse.repository;

import com.repopulse.entity.Commit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommitRepository extends JpaRepository<Commit, Long> {

    List<Commit> findAllByRepositoryId(Long repositoryId);

    Optional<Commit> findBySha(String sha);

    boolean existsBySha(String sha);

    long countByRepositoryId(Long repositoryId);

    List<Commit> findAllByRepositoryIdAndCommittedAtBetween(
            Long repositoryId,
            LocalDateTime from,
            LocalDateTime to
    );
}
