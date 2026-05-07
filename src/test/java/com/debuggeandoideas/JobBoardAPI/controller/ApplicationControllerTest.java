package com.debuggeandoideas.JobBoardAPI.controller;

import com.debuggeandoideas.JobBoardAPI.dto.response.ApplicationResponse;
import com.debuggeandoideas.JobBoardAPI.entity.ApplicationStatus;
import com.debuggeandoideas.JobBoardAPI.exception.CandidateNotFoundException;
import com.debuggeandoideas.JobBoardAPI.exception.DuplicateApplicationException;
import com.debuggeandoideas.JobBoardAPI.exception.GlobalExceptionHandler;
import com.debuggeandoideas.JobBoardAPI.exception.JobClosedException;
import com.debuggeandoideas.JobBoardAPI.exception.JobNotFoundException;
import com.debuggeandoideas.JobBoardAPI.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationController applicationController;

    private MockMvc mockMvc;

    private static final String VALID_REQUEST_JSON = """
            {
              "candidate_id": 1,
              "job_id": 2
            }
            """;

    private static final String MISSING_CANDIDATE_ID_JSON = """
            {
              "job_id": 2
            }
            """;

    private static final String NEGATIVE_JOB_ID_JSON = """
            {
              "candidate_id": 1,
              "job_id": -1
            }
            """;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(applicationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ApplicationResponse buildResponse() {
        return ApplicationResponse.builder()
                .id(10L)
                .candidateId(1L)
                .jobId(2L)
                .status(ApplicationStatus.pending)
                .appliedAt(OffsetDateTime.parse("2025-07-15T10:00:00Z"))
                .updatedAt(OffsetDateTime.parse("2025-07-15T10:00:00Z"))
                .build();
    }

    // ─── POST /applications ──────────────────────────────────────────────────

    @Test
    void applyToJob_retornaCreatedConLaPostulacionCuandoElRequestEsValido() throws Exception {
        // Given
        var response = buildResponse();
        when(applicationService.applyToJob(any())).thenReturn(response);

        // When / Then
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.candidate_id").value(1))
                .andExpect(jsonPath("$.job_id").value(2))
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    void applyToJob_retornaUnprocessableEntityCuandoElCandidateIdEsNulo() throws Exception {
        // When / Then
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MISSING_CANDIDATE_ID_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void applyToJob_retornaUnprocessableEntityCuandoElJobIdEsNegativo() throws Exception {
        // When / Then
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NEGATIVE_JOB_ID_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void applyToJob_retornaNotFoundCuandoElCandidatoNoExisteEnJSONPlaceholder() throws Exception {
        // Given
        when(applicationService.applyToJob(any()))
                .thenThrow(new CandidateNotFoundException(1L));

        // When / Then
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CANDIDATE_NOT_FOUND"));
    }

    @Test
    void applyToJob_retornaNotFoundCuandoLaOfertaNoExiste() throws Exception {
        // Given
        when(applicationService.applyToJob(any()))
                .thenThrow(new JobNotFoundException(2L));

        // When / Then
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_NOT_FOUND"));
    }

    @Test
    void applyToJob_retornaConflictCuandoLaOfertaEstaCerrada() throws Exception {
        // Given
        when(applicationService.applyToJob(any()))
                .thenThrow(new JobClosedException(2L));

        // When / Then
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("JOB_CLOSED"));
    }

    @Test
    void applyToJob_retornaUnprocessableEntityCuandoElCandidatoYaPostulo() throws Exception {
        // Given
        when(applicationService.applyToJob(any()))
                .thenThrow(new DuplicateApplicationException(1L, 2L));

        // When / Then
        mockMvc.perform(post("/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("DUPLICATE_APPLICATION"));
    }
}
