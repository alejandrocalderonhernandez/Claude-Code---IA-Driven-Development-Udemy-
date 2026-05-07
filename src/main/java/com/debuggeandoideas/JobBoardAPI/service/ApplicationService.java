package com.debuggeandoideas.JobBoardAPI.service;

import com.debuggeandoideas.JobBoardAPI.dto.request.ApplicationRequest;
import com.debuggeandoideas.JobBoardAPI.dto.request.PatchApplicationStatusRequest;
import com.debuggeandoideas.JobBoardAPI.dto.response.ApplicationResponse;

public interface ApplicationService {
    ApplicationResponse applyToJob(ApplicationRequest request);
    ApplicationResponse updateStatus(Long id, PatchApplicationStatusRequest request);
}
