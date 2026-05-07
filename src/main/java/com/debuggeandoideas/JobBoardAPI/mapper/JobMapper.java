package com.debuggeandoideas.JobBoardAPI.mapper;

import com.debuggeandoideas.JobBoardAPI.dto.request.JobRequest;
import com.debuggeandoideas.JobBoardAPI.dto.response.JobResponse;
import com.debuggeandoideas.JobBoardAPI.entity.JobEntity;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public JobEntity toEntity(JobRequest request) {
        return JobEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .company(request.getCompany())
                .location(request.getLocation())
                .build();
    }

    public JobResponse toResponse(JobEntity entity) {
        return JobResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .company(entity.getCompany())
                .location(entity.getLocation())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
