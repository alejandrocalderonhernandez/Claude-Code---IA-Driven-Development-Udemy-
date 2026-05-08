package com.debuggeandoideas.JobBoardAPI.controller;

import com.debuggeandoideas.JobBoardAPI.dto.request.JobRequest;
import com.debuggeandoideas.JobBoardAPI.dto.request.JobSearchCriteria;
import com.debuggeandoideas.JobBoardAPI.dto.response.JobPageResponse;
import com.debuggeandoideas.JobBoardAPI.dto.response.JobResponse;
import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;
import com.debuggeandoideas.JobBoardAPI.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(request));
    }

    @GetMapping
    public ResponseEntity<JobPageResponse> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var criteria = new JobSearchCriteria(title, location, status, page, size);
        return ResponseEntity.ok(jobService.searchJobs(criteria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(
            @PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<JobResponse> closeJob(
            @PathVariable Long id) {
        return ResponseEntity.ok(jobService.closeJob(id));
    }
}
