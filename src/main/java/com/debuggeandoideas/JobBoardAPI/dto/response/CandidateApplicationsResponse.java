package com.debuggeandoideas.JobBoardAPI.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateApplicationsResponse {

    @JsonProperty("candidate_id")
    private Long candidateId;

    @JsonProperty("candidate_name")
    private String candidateName;

    @JsonProperty("candidate_email")
    private String candidateEmail;

    private List<ApplicationHistoryItemResponse> applications;
}
