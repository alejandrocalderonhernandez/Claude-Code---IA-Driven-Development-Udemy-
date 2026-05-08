package com.debuggeandoideas.JobBoardAPI.service.impl;

import com.debuggeandoideas.JobBoardAPI.dto.request.JobRequest;
import com.debuggeandoideas.JobBoardAPI.dto.request.JobSearchCriteria;
import com.debuggeandoideas.JobBoardAPI.dto.response.JobPageResponse;
import com.debuggeandoideas.JobBoardAPI.dto.response.JobResponse;
import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;
import com.debuggeandoideas.JobBoardAPI.exception.JobNotFoundException;
import com.debuggeandoideas.JobBoardAPI.mapper.JobMapper;
import com.debuggeandoideas.JobBoardAPI.repository.JobRepository;
import com.debuggeandoideas.JobBoardAPI.service.JobService;
import com.debuggeandoideas.JobBoardAPI.specification.JobSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public JobResponse createJob(JobRequest request) {
        var entity = jobMapper.toEntity(request);
        var saved = jobRepository.save(entity);
        return jobMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll()
                .stream()
                .map(jobMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {
        var entity = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
        return jobMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public JobResponse closeJob(Long id) {
        var entity = jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
        entity.setStatus(JobStatus.closed);
        var saved = jobRepository.save(entity);
        return jobMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public JobPageResponse searchJobs(JobSearchCriteria criteria) {
        var spec = Specification
                .where(JobSpecification.hasTitle(criteria.title()))
                .and(JobSpecification.hasLocation(criteria.location()))
                .and(JobSpecification.hasStatus(criteria.status()));

        var pageable = PageRequest.of(criteria.page(), criteria.size());
        var result = jobRepository.findAll(spec, pageable);

        var content = result.getContent()
                .stream()
                .map(jobMapper::toResponse)
                .toList();

        return new JobPageResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
