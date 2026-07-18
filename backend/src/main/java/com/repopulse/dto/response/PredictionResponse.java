package com.repopulse.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Prediction result returned to API clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PredictionResponse {

    private Long id;
    private Long pullRequestId;
    private Long repositoryId;

    /** XGBoost composite risk score (0.0 – 1.0). */
    private Double riskScore;

    /** Three-class label: LOW / MEDIUM / HIGH. */
    private String riskLevel;

    /** Priority label: P1_CRITICAL / P2_HIGH / P3_MEDIUM / P4_LOW. */
    private String priority;

    /** Estimated review time in hours. */
    private Double estimatedReviewTimeHours;

    /** Repository health score at prediction time (0.0 – 1.0). */
    private Double repositoryHealthScore;

    private String modelVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
