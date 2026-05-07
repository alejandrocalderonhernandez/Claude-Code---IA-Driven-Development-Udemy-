package com.debuggeandoideas.JobBoardAPI.service.impl;

import com.debuggeandoideas.JobBoardAPI.client.CandidateClient;
import com.debuggeandoideas.JobBoardAPI.dto.request.ApplicationRequest;
import com.debuggeandoideas.JobBoardAPI.dto.request.PatchApplicationStatusRequest;
import com.debuggeandoideas.JobBoardAPI.dto.response.ApplicationResponse;
import com.debuggeandoideas.JobBoardAPI.entity.ApplicationStatus;
import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;
import com.debuggeandoideas.JobBoardAPI.exception.ApplicationNotFoundException;
import com.debuggeandoideas.JobBoardAPI.exception.DuplicateApplicationException;
import com.debuggeandoideas.JobBoardAPI.exception.InvalidStatusTransitionException;
import com.debuggeandoideas.JobBoardAPI.exception.JobClosedException;
import com.debuggeandoideas.JobBoardAPI.exception.JobNotFoundException;
import com.debuggeandoideas.JobBoardAPI.mapper.ApplicationMapper;
import com.debuggeandoideas.JobBoardAPI.repository.ApplicationRepository;
import com.debuggeandoideas.JobBoardAPI.repository.JobRepository;
import com.debuggeandoideas.JobBoardAPI.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final CandidateClient candidateClient;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;

    @Override
    public ApplicationResponse applyToJob(ApplicationRequest request) {
        candidateClient.getCandidateById(request.getCandidateId());

        var job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new JobNotFoundException(request.getJobId()));

        if (job.getStatus() != JobStatus.open) {
            throw new JobClosedException(request.getJobId());
        }

        if (applicationRepository.existsByCandidateIdAndJobId(request.getCandidateId(), request.getJobId())) {
            throw new DuplicateApplicationException(request.getCandidateId(), request.getJobId());
        }

        var entity = applicationMapper.toEntity(request);
        try {
            var saved = applicationRepository.save(entity);
            return applicationMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateApplicationException(request.getCandidateId(), request.getJobId());
        }
    }

    @Override
    public ApplicationResponse updateStatus(Long id, PatchApplicationStatusRequest request) {
        var application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));

        if (application.getStatus() != ApplicationStatus.pending) {
            throw new InvalidStatusTransitionException(id);
        }

        application.setStatus(ApplicationStatus.valueOf(request.getStatus()));
        application.setUpdatedAt(OffsetDateTime.now());

        var saved = applicationRepository.save(application);
        return applicationMapper.toResponse(saved);
    }
}
