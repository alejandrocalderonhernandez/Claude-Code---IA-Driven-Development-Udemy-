package com.debuggeandoideas.JobBoardAPI.service.impl;

import com.debuggeandoideas.JobBoardAPI.client.CandidateClient;
import com.debuggeandoideas.JobBoardAPI.dto.response.ApplicationHistoryItemResponse;
import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateApplicationsResponse;
import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateResponse;
import com.debuggeandoideas.JobBoardAPI.repository.ApplicationRepository;
import com.debuggeandoideas.JobBoardAPI.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateClient candidateClient;
    private final ApplicationRepository applicationRepository;

    @Override
    public CandidateResponse getCandidateById(Long id) {
        return candidateClient.getCandidateById(id);
    }

    @Override
    public List<CandidateResponse> getAllCandidates() {
        return candidateClient.getAllCandidates();
    }

    // TODO UNIT TEST
    @Override
    public CandidateApplicationsResponse getCandidateApplications(Long candidateId) {
        // RN-004: validar candidato externamente ANTES de consultar la DB
        var candidate = candidateClient.getCandidateById(candidateId);

        var items = applicationRepository
                .findApplicationHistoryByCandidateId(candidateId)
                .stream()
                .map(p -> ApplicationHistoryItemResponse.builder()
                        .applicationId(p.applicationId())
                        .applicationStatus(p.applicationStatus().name())
                        .appliedAt(p.appliedAt())
                        .updatedAt(p.updatedAt())
                        .jobId(p.jobId())
                        .title(p.title())
                        .company(p.company())
                        .location(p.location())
                        .jobStatus(p.jobStatus().name())
                        .build())
                .toList();

        return CandidateApplicationsResponse.builder()
                .candidateId(candidate.getId())
                .candidateName(candidate.getName())
                .candidateEmail(candidate.getEmail())
                .applications(items)
                .build();
    }
}
