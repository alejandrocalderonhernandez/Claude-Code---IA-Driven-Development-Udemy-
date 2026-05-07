package com.debuggeandoideas.JobBoardAPI.dto.response;

import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;
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
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String location;
    private JobStatus status;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
