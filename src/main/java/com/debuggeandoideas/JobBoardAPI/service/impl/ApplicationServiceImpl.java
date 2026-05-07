package com.debuggeandoideas.JobBoardAPI.service.impl;

import com.debuggeandoideas.JobBoardAPI.client.CandidateClient;
import com.debuggeandoideas.JobBoardAPI.dto.request.ApplicationRequest;
import com.debuggeandoideas.JobBoardAPI.dto.response.ApplicationResponse;
import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;
import com.debuggeandoideas.JobBoardAPI.exception.DuplicateApplicationException;
import com.debuggeandoideas.JobBoardAPI.exception.JobClosedException;
import com.debuggeandoideas.JobBoardAPI.exception.JobNotFoundException;
import com.debuggeandoideas.JobBoardAPI.mapper.ApplicationMapper;
import com.debuggeandoideas.JobBoardAPI.repository.ApplicationRepository;
import com.debuggeandoideas.JobBoardAPI.repository.JobRepository;
import com.debuggeandoideas.JobBoardAPI.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final CandidateClient candidateClient;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;

    @Override
    @Transactional
    public ApplicationResponse applyToJob(ApplicationRequest request) {
        // RN-004: validar candidato en JSONPlaceholder — lanza CandidateNotFoundException si no existe
        candidateClient.getCandidateById(request.getCandidateId());

        // Validar que la oferta existe
        var job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new JobNotFoundException(request.getJobId()));

        // RN-002: la oferta debe estar abierta
        if (job.getStatus() != JobStatus.open) {
            throw new JobClosedException(request.getJobId());
        }

        // RN-001: el candidato no puede postular dos veces a la misma oferta
        if (applicationRepository.existsByCandidateIdAndJobId(request.getCandidateId(), request.getJobId())) {
            throw new DuplicateApplicationException(request.getCandidateId(), request.getJobId());
        }

        var entity = applicationMapper.toEntity(request);
        var saved = applicationRepository.save(entity);
        return applicationMapper.toResponse(saved);
    }
}
