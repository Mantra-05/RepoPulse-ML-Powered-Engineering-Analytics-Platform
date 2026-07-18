package com.repopulse.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddRepositoryRequest {

    /**
     * Full GitHub repository name in "owner/repo" format.
     * Example: "octocat/Hello-World"
     */
    @NotBlank(message = "Repository full name is required (e.g. owner/repo)")
    private String fullName;
}
