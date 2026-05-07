package com.debuggeandoideas.JobBoardAPI.mapper;

import com.debuggeandoideas.JobBoardAPI.dto.request.ApplicationRequest;
import com.debuggeandoideas.JobBoardAPI.dto.response.ApplicationResponse;
import com.debuggeandoideas.JobBoardAPI.entity.ApplicationEntity;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationEntity toEntity(ApplicationRequest request) {
        return ApplicationEntity.builder()
                .candidateId(request.getCandidateId())
                .jobId(request.getJobId())
                .build();
    }

    public ApplicationResponse toResponse(ApplicationEntity entity) {
        return ApplicationResponse.builder()
                .id(entity.getId())
                .candidateId(entity.getCandidateId())
                .jobId(entity.getJobId())
                .status(entity.getStatus())
                .appliedAt(entity.getAppliedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
