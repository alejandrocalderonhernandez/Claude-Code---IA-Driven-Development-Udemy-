package com.debuggeandoideas.JobBoardAPI.service;

import com.debuggeandoideas.JobBoardAPI.dto.request.JobRequest;
import com.debuggeandoideas.JobBoardAPI.dto.request.JobSearchCriteria;
import com.debuggeandoideas.JobBoardAPI.dto.response.JobPageResponse;
import com.debuggeandoideas.JobBoardAPI.dto.response.JobReportResponse;
import com.debuggeandoideas.JobBoardAPI.dto.response.JobResponse;

import java.util.List;

public interface JobService {
    JobResponse createJob(JobRequest request);
    List<JobResponse> getAllJobs();
    JobResponse getJobById(Long id);
    JobResponse closeJob(Long id);
    JobPageResponse searchJobs(JobSearchCriteria criteria);
    JobReportResponse getJobReport(Long jobId);
}
