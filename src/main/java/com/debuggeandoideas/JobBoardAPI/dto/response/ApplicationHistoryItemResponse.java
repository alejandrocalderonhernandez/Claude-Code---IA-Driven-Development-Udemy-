package com.debuggeandoideas.JobBoardAPI.dto.response;

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
public class ApplicationHistoryItemResponse {

    @JsonProperty("application_id")
    private Long applicationId;

    @JsonProperty("application_status")
    private String applicationStatus;

    @JsonProperty("applied_at")
    private OffsetDateTime appliedAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    @JsonProperty("job_id")
    private Long jobId;

    private String title;

    private String company;

    private String location;

    @JsonProperty("job_status")
    private String jobStatus;
}
