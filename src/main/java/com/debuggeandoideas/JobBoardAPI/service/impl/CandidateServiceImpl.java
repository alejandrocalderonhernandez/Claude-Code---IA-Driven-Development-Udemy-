package com.debuggeandoideas.JobBoardAPI.service.impl;

import com.debuggeandoideas.JobBoardAPI.client.CandidateClient;
import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateResponse;
import com.debuggeandoideas.JobBoardAPI.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateClient candidateClient;

    @Override
    public CandidateResponse getCandidateById(Long id) {
        // TO DO UNIT TEST
        return candidateClient.getCandidateById(id);
    }

    @Override
    public List<CandidateResponse> getAllCandidates() {
        // TO DO UNIT TEST
        return candidateClient.getAllCandidates();
    }
}
