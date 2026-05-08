package com.debuggeandoideas.JobBoardAPI.dto.request;

import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;

public record JobSearchCriteria(
        String title,
        String location,
        JobStatus status,
        int page,
        int size
) {}
