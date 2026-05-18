package com.debuggeandoideas.JobBoardAPI.controller;

import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateApplicationsResponse;
import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateResponse;
import com.debuggeandoideas.JobBoardAPI.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping
    public ResponseEntity<List<CandidateResponse>> getAllCandidates() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    // TODO UNIT TEST
    @GetMapping("/{id}/applications")
    public ResponseEntity<CandidateApplicationsResponse> getCandidateApplications(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(candidateService.getCandidateApplications(id));
    }
}
