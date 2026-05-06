package com.debuggeandoideas.JobBoardAPI.controller;

import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateResponse;
import com.debuggeandoideas.JobBoardAPI.exception.CandidateNotFoundException;
import com.debuggeandoideas.JobBoardAPI.exception.CandidateServiceUnavailableException;
import com.debuggeandoideas.JobBoardAPI.service.CandidateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CandidateControllerTest {

    @Mock
    private CandidateService candidateService;

    @InjectMocks
    private CandidateController candidateController;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(candidateController).build();
    }

    // ─── GET /candidates ─────────────────────────────────────────────────────

    @Test
    void getAllCandidates_retornaOkConListaDeCandidatos() throws Exception {
        // Given
        var candidatos = List.of(
                new CandidateResponse(1L, "Leanne Graham", "Bret", "sincere@april.biz", null, null, null, null),
                new CandidateResponse(2L, "Ervin Howell", "Antonette", "shanna@melissa.tv", null, null, null, null)
        );
        when(candidateService.getAllCandidates()).thenReturn(candidatos);

        // When / Then
        mockMvc.perform(get("/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Leanne Graham"))
                .andExpect(jsonPath("$[1].username").value("Antonette"));
    }

    @Test
    void getAllCandidates_retornaBadGatewayCuandoElServicioExternoNoEstaDisponible() throws Exception {
        // Given
        when(candidateService.getAllCandidates())
                .thenThrow(new CandidateServiceUnavailableException("JSONPlaceholder no disponible", null));

        // When / Then
        mockMvc.perform(get("/candidates"))
                .andExpect(status().isBadGateway());
    }

    // ─── GET /candidates/{id} ────────────────────────────────────────────────

    @Test
    void getCandidateById_retornaOkConElCandidatoCuandoExiste() throws Exception {
        // Given
        var candidato = new CandidateResponse(1L, "Leanne Graham", "Bret",
                "sincere@april.biz", "555-1234", "hildegard.org", null, null);
        when(candidateService.getCandidateById(1L)).thenReturn(candidato);

        // When / Then
        mockMvc.perform(get("/candidates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Leanne Graham"))
                .andExpect(jsonPath("$.email").value("sincere@april.biz"));
    }

    @Test
    void getCandidateById_retornaNotFoundCuandoElCandidatoNoExiste() throws Exception {
        // Given
        when(candidateService.getCandidateById(99L))
                .thenThrow(new CandidateNotFoundException(99L));

        // When / Then
        mockMvc.perform(get("/candidates/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCandidateById_retornaBadGatewayCuandoElServicioExternoNoEstaDisponible() throws Exception {
        // Given
        when(candidateService.getCandidateById(1L))
                .thenThrow(new CandidateServiceUnavailableException("JSONPlaceholder no disponible", null));

        // When / Then
        mockMvc.perform(get("/candidates/1"))
                .andExpect(status().isBadGateway());
    }
}
