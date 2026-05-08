package com.debuggeandoideas.JobBoardAPI.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class JobPageResponse {
    private List<JobResponse> content;
    private int page;
    private int size;
    private long total;
    private int totalPages;
}
