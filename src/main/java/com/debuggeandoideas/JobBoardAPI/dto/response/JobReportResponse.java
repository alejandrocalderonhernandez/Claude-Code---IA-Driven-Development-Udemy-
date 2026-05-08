package com.debuggeandoideas.JobBoardAPI.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class JobReportResponse {

    @JsonProperty("job_id")
    private Long jobId;

    private String title;

    @JsonProperty("total_applications")
    private long totalApplications;

    @JsonProperty("by_status")
    private Map<String, Long> byStatus;
}
