package com.debuggeandoideas.JobBoardAPI.service;

import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateApplicationsResponse;
import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateResponse;

import java.util.List;

public interface CandidateService {
    CandidateResponse getCandidateById(Long id);
    List<CandidateResponse> getAllCandidates();
    CandidateApplicationsResponse getCandidateApplications(Long candidateId);
}
