package com.debuggeandoideas.JobBoardAPI.dto.response;

import com.debuggeandoideas.JobBoardAPI.entity.ApplicationStatus;
import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;

import java.time.OffsetDateTime;

public record ApplicationHistoryDto(
        Long applicationId,
        ApplicationStatus applicationStatus,
        OffsetDateTime appliedAt,
        OffsetDateTime updatedAt,
        Long jobId,
        String title,
        String company,
        String location,
        JobStatus jobStatus
) {}
