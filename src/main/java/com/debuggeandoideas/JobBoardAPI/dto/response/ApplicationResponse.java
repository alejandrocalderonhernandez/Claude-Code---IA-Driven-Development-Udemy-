package com.debuggeandoideas.JobBoardAPI.dto.response;

import com.debuggeandoideas.JobBoardAPI.entity.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;

    @JsonProperty("candidate_id")
    private Long candidateId;

    @JsonProperty("job_id")
    private Long jobId;

    private ApplicationStatus status;

    @JsonProperty("applied_at")
    private OffsetDateTime appliedAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}
