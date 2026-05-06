package com.debuggeandoideas.JobBoardAPI.client;

import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateResponse;

import java.util.List;

public interface CandidateClient {
    CandidateResponse getCandidateById(Long id);
    List<CandidateResponse> getAllCandidates();
}
