package com.repopulse.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.repopulse.dto.request.MlPredictionRequest;
import com.repopulse.dto.response.MlPredictionResponse;
import com.repopulse.dto.response.PredictionResponse;
import com.repopulse.entity.Prediction;
import com.repopulse.entity.Prediction.RiskLevel;
import com.repopulse.entity.PullRequest;
import com.repopulse.exception.ResourceNotFoundException;
import com.repopulse.repository.PredictionRepository;
import com.repopulse.repository.PullRequestRepository;
import com.repopulse.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionServiceImpl implements PredictionService {

    private final PullRequestRepository pullRequestRepository;
    private final PredictionRepository  predictionRepository;
    private final RestTemplate          restTemplate;
    private final ObjectMapper          objectMapper;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    private static final String ML_MODEL_VERSION = "xgb-v1.0";

    // ── Request prediction ────────────────────────────────────────────────────

    @Override
    @Transactional
    public PredictionResponse requestPrediction(Long pullRequestId) {
        PullRequest pr = pullRequestRepository.findById(pullRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("PullRequest", "id", pullRequestId));

        // Build the ML feature vector
        MlPredictionRequest mlRequest = buildMlRequest(pr);

        // Call FastAPI
        MlPredictionResponse mlResponse = callMlService(mlRequest);

        // Persist / update
        Prediction prediction = predictionRepository.findByPullRequestId(pullRequestId)
                .orElse(Prediction.builder()
                        .pullRequest(pr)
                        .repository(pr.getRepository())
                        .build());

        prediction.setRiskScore(mlResponse.getRiskScore());
        prediction.setRiskLevel(parseRiskLevel(mlResponse.getRiskLevel()));
        prediction.setPriority(mlResponse.getPriority());
        prediction.setEstimatedReviewTimeHours(mlResponse.getEstimatedReviewTime());
        prediction.setRepositoryHealthScore(mlResponse.getRepositoryHealth());
        prediction.setModelVersion(ML_MODEL_VERSION);

        try {
            prediction.setRawPayload(objectMapper.writeValueAsString(mlResponse));
        } catch (JsonProcessingException e) {
            log.warn("Could not serialise ML payload: {}", e.getMessage());
        }

        Prediction saved = predictionRepository.save(prediction);
        log.info("Prediction stored for prId={} riskLevel={}", pullRequestId, saved.getRiskLevel());
        return toResponse(saved);
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PredictionResponse> getPredictionsByRepository(Long repositoryId) {
        return predictionRepository.findAllByRepositoryId(repositoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PredictionResponse getPredictionByPullRequest(Long pullRequestId) {
        return predictionRepository.findByPullRequestId(pullRequestId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prediction", "pullRequestId", pullRequestId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MlPredictionRequest buildMlRequest(PullRequest pr) {
        double daysOpen = 0.0;
        if (pr.getOpenedAt() != null) {
            LocalDateTime end = pr.getMergedAt() != null ? pr.getMergedAt()
                    : pr.getClosedAt() != null ? pr.getClosedAt()
                    : LocalDateTime.now();
            daysOpen = ChronoUnit.HOURS.between(pr.getOpenedAt(), end) / 24.0;
        }

        int totalComments = (pr.getComments() != null ? pr.getComments() : 0)
                + (pr.getReviewComments() != null ? pr.getReviewComments() : 0);

        return MlPredictionRequest.builder()
                .linesAdded(pr.getAdditions() != null ? pr.getAdditions() : 0)
                .linesDeleted(pr.getDeletions() != null ? pr.getDeletions() : 0)
                .filesChanged(pr.getChangedFiles() != null ? pr.getChangedFiles() : 0)
                .commits(pr.getCommitsCount() != null ? pr.getCommitsCount() : 0)
                .comments(totalComments)
                .authorExperience(pr.getAuthorContributions() != null
                        ? pr.getAuthorContributions() : 0)
                .daysOpen(daysOpen)
                .build();
    }

    /**
     * Calls {@code POST /predict} on the FastAPI ML service.
     * Falls back to a heuristic prediction if the service is unavailable.
     */
    private MlPredictionResponse callMlService(MlPredictionRequest request) {
        try {
            String url = mlServiceUrl + "/predict";
            ResponseEntity<MlPredictionResponse> response =
                    restTemplate.postForEntity(url, request, MlPredictionResponse.class);
            log.debug("ML service response: {}", response.getBody());
            return response.getBody();
        } catch (RestClientException ex) {
            log.warn("ML service unavailable ({}), using heuristic fallback.", ex.getMessage());
            return heuristicFallback(request);
        }
    }

    /** Simple heuristic fallback when FastAPI is not running. */
    private MlPredictionResponse heuristicFallback(MlPredictionRequest req) {
        int totalLines  = req.getLinesAdded() + req.getLinesDeleted();
        double rawScore = Math.min(1.0, (totalLines / 500.0) * 0.5
                + (req.getFilesChanged() / 20.0) * 0.3
                + (req.getDaysOpen() / 14.0) * 0.2);

        String riskLevel = rawScore < 0.33 ? "LOW" : rawScore < 0.66 ? "MEDIUM" : "HIGH";
        String priority  = rawScore < 0.33 ? "P4_LOW"
                : rawScore < 0.55 ? "P3_MEDIUM"
                : rawScore < 0.77 ? "P2_HIGH" : "P1_CRITICAL";

        MlPredictionResponse resp = new MlPredictionResponse();
        resp.setRiskScore(rawScore);
        resp.setRiskLevel(riskLevel);
        resp.setPriority(priority);
        resp.setEstimatedReviewTime(req.getDaysOpen() * 24 * 0.6);
        resp.setRepositoryHealth(0.5);
        return resp;
    }

    private RiskLevel parseRiskLevel(String level) {
        try {
            return RiskLevel.valueOf(level.toUpperCase());
        } catch (Exception e) {
            return RiskLevel.MEDIUM;
        }
    }

    private PredictionResponse toResponse(Prediction p) {
        return PredictionResponse.builder()
                .id(p.getId())
                .pullRequestId(p.getPullRequest().getId())
                .repositoryId(p.getRepository().getId())
                .riskScore(p.getRiskScore())
                .riskLevel(p.getRiskLevel().name())
                .priority(p.getPriority())
                .estimatedReviewTimeHours(p.getEstimatedReviewTimeHours())
                .repositoryHealthScore(p.getRepositoryHealthScore())
                .modelVersion(p.getModelVersion())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
