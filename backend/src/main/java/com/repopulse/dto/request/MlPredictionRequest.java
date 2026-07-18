package com.repopulse.dto.request;

import lombok.AllArgsConstructor;
<br>import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sent to the FastAPI ML service {@code POST /predict}.
 * All fields map directly to the XGBoost model features.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MlPredictionRequest {

    /** Lines added in the PR. */
    private Integer linesAdded;

    /** Lines deleted in the PR. */
    private Integer linesDeleted;

    /** Number of files changed. */
    private Integer filesChanged;

    /** Number of commits in the PR. */
    private Integer commits;

    /** Total number of review + issue comments. */
    private Integer comments;

    /** Author's total commit count to this repo (proxy for experience). */
    private Integer authorExperience;

    /** How many days the PR has been open. */
    private Double daysOpen;
}
