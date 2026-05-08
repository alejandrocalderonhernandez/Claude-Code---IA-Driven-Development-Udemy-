package com.debuggeandoideas.JobBoardAPI.controller;

import com.debuggeandoideas.JobBoardAPI.dto.response.JobPageResponse;
import com.debuggeandoideas.JobBoardAPI.dto.response.JobReportResponse;
import com.debuggeandoideas.JobBoardAPI.dto.response.JobResponse;
import com.debuggeandoideas.JobBoardAPI.entity.JobStatus;
import com.debuggeandoideas.JobBoardAPI.exception.GlobalExceptionHandler;
import com.debuggeandoideas.JobBoardAPI.exception.JobNotFoundException;
import com.debuggeandoideas.JobBoardAPI.service.JobService;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    private MockMvc mockMvc;

    // JSON string literals — avoids importing Jackson 3 (tools.jackson.*) directly in tests
    private static final String VALID_JOB_JSON = """
            {
              "title": "Backend Engineer",
              "description": "Diseñar e implementar APIs REST.",
              "company": "Acme Corp",
              "location": "Remoto"
            }
            """;

    private static final String EMPTY_TITLE_JSON = """
            {
              "title": "",
              "description": "Diseñar e implementar APIs REST.",
              "company": "Acme Corp",
              "location": "Remoto"
            }
            """;

    private static final String NULL_DESCRIPTION_JSON = """
            {
              "title": "Backend Engineer",
              "company": "Acme Corp",
              "location": "Remoto"
            }
            """;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private JobResponse buildResponse(Long id, JobStatus status) {
        return JobResponse.builder()
                .id(id)
                .title("Backend Engineer")
                .description("Diseñar e implementar APIs REST.")
                .company("Acme Corp")
                .location("Remoto")
                .status(status)
                .createdAt(OffsetDateTime.parse("2025-07-15T14:30:00Z"))
                .build();
    }

    // ─── POST /jobs ──────────────────────────────────────────────────────────

    @Test
    void createJob_retornaCreatedConElBodyCorrectoCuandoElRequestEsValido() throws Exception {
        // Given
        var response = buildResponse(1L, JobStatus.open);
        when(jobService.createJob(any())).thenReturn(response);

        // When / Then
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JOB_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Backend Engineer"))
                .andExpect(jsonPath("$.company").value("Acme Corp"))
                .andExpect(jsonPath("$.status").value("open"));
    }

    @Test
    void createJob_retornaUnprocessableEntityCuandoElTituloEstaVacio() throws Exception {
        // When / Then
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EMPTY_TITLE_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void createJob_retornaUnprocessableEntityCuandoLaDescripcionEsNula() throws Exception {
        // When / Then
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NULL_DESCRIPTION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // ─── GET /jobs ───────────────────────────────────────────────────────────

    @Test
    void searchJobs_retornaOkConPaginaDeOfertasCuandoNoHayFiltros() throws Exception {
        // Given
        var jobs = List.of(buildResponse(1L, JobStatus.open), buildResponse(2L, JobStatus.closed));
        var pageResponse = new JobPageResponse(jobs, 0, 20, 2L, 1);
        when(jobService.searchJobs(any())).thenReturn(pageResponse);

        // When / Then
        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].status").value("closed"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void searchJobs_retornaOkConContentVacioCuandoNoHayResultados() throws Exception {
        // Given
        var pageResponse = new JobPageResponse(List.of(), 0, 20, 0L, 0);
        when(jobService.searchJobs(any())).thenReturn(pageResponse);

        // When / Then
        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void searchJobs_retornaOkCuandoSeFiltranOfertasPorTituloYStatus() throws Exception {
        // Given
        var jobs = List.of(buildResponse(1L, JobStatus.open));
        var pageResponse = new JobPageResponse(jobs, 0, 20, 1L, 1);
        when(jobService.searchJobs(any())).thenReturn(pageResponse);

        // When / Then
        mockMvc.perform(get("/jobs")
                        .param("title", "backend")
                        .param("status", "open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("open"))
                .andExpect(jsonPath("$.total").value(1));
    }

    // ─── GET /jobs/{id} ──────────────────────────────────────────────────────

    @Test
    void getJobById_retornaOkConLaOfertaCuandoExiste() throws Exception {
        // Given
        var response = buildResponse(1L, JobStatus.open);
        when(jobService.getJobById(1L)).thenReturn(response);

        // When / Then
        mockMvc.perform(get("/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Backend Engineer"))
                .andExpect(jsonPath("$.location").value("Remoto"));
    }

    @Test
    void getJobById_retornaNotFoundCuandoLaOfertaNoExiste() throws Exception {
        // Given
        when(jobService.getJobById(99L)).thenThrow(new JobNotFoundException(99L));

        // When / Then
        mockMvc.perform(get("/jobs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── PATCH /jobs/{id}/close ───────────────────────────────────────────────

    @Test
    void closeJob_retornaOkConLaOfertaConStatusClosed() throws Exception {
        // Given
        var response = buildResponse(1L, JobStatus.closed);
        when(jobService.closeJob(1L)).thenReturn(response);

        // When / Then
        mockMvc.perform(patch("/jobs/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("closed"));
    }

    @Test
    void closeJob_retornaNotFoundCuandoLaOfertaNoExiste() throws Exception {
        // Given
        when(jobService.closeJob(99L)).thenThrow(new JobNotFoundException(99L));

        // When / Then
        mockMvc.perform(patch("/jobs/99/close"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─── GET /jobs/{id}/report ───────────────────────────────────────────────

    @Test
    void getJobReport_retornaOkConElReporteCompleto() throws Exception {
        // Given
        var byStatus = Map.of("pending", 2L, "accepted", 1L, "rejected", 2L);
        var report = new JobReportResponse(1L, "Backend Engineer", 5L, byStatus);
        when(jobService.getJobReport(1L)).thenReturn(report);

        // When / Then
        mockMvc.perform(get("/jobs/1/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.job_id").value(1))
                .andExpect(jsonPath("$.title").value("Backend Engineer"))
                .andExpect(jsonPath("$.total_applications").value(5))
                .andExpect(jsonPath("$.by_status.pending").value(2))
                .andExpect(jsonPath("$.by_status.accepted").value(1))
                .andExpect(jsonPath("$.by_status.rejected").value(2));
    }

    @Test
    void getJobReport_retornaNotFoundCuandoLaOfertaNoExiste() throws Exception {
        // Given
        when(jobService.getJobReport(99L)).thenThrow(new JobNotFoundException(99L));

        // When / Then
        mockMvc.perform(get("/jobs/99/report"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_NOT_FOUND"));
    }
}
