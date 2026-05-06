package com.debuggeandoideas.JobBoardAPI.service;

import com.debuggeandoideas.JobBoardAPI.client.CandidateClient;
import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateResponse;
import com.debuggeandoideas.JobBoardAPI.exception.CandidateNotFoundException;
import com.debuggeandoideas.JobBoardAPI.exception.CandidateServiceUnavailableException;
import com.debuggeandoideas.JobBoardAPI.service.impl.CandidateServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateServiceImplTest {

    @Mock
    private CandidateClient candidateClient;

    @InjectMocks
    private CandidateServiceImpl candidateService;

    // ─── getCandidateById ────────────────────────────────────────────────────

    @Test
    void getCandidateById_delegaAlClienteYRetornaElCandidato() {
        // Given
        var candidatoEsperado = new CandidateResponse(1L, "Leanne Graham", "Bret",
                "sincere@april.biz", "555-1234", "hildegard.org", null, null);
        when(candidateClient.getCandidateById(1L)).thenReturn(candidatoEsperado);

        // When
        var resultado = candidateService.getCandidateById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getEmail()).isEqualTo("sincere@april.biz");
        verify(candidateClient).getCandidateById(1L);
    }

    @Test
    void getCandidateById_propagaCandidateNotFoundExceptionCuandoElCandidatoNoExiste() {
        // Given
        when(candidateClient.getCandidateById(99L))
                .thenThrow(new CandidateNotFoundException(99L));

        // When / Then
        assertThatThrownBy(() -> candidateService.getCandidateById(99L))
                .isInstanceOf(CandidateNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getCandidateById_propagaCandidateServiceUnavailableExceptionCuandoElServicioFalla() {
        // Given
        when(candidateClient.getCandidateById(1L))
                .thenThrow(new CandidateServiceUnavailableException("JSONPlaceholder no disponible", null));

        // When / Then
        assertThatThrownBy(() -> candidateService.getCandidateById(1L))
                .isInstanceOf(CandidateServiceUnavailableException.class);
    }

    // ─── getAllCandidates ────────────────────────────────────────────────────

    @Test
    void getAllCandidates_delegaAlClienteYRetornaTodosLosCandidatos() {
        // Given
        var candidatos = List.of(
                new CandidateResponse(1L, "Leanne Graham", "Bret", "sincere@april.biz", null, null, null, null),
                new CandidateResponse(2L, "Ervin Howell", "Antonette", "shanna@melissa.tv", null, null, null, null)
        );
        when(candidateClient.getAllCandidates()).thenReturn(candidatos);

        // When
        var resultado = candidateService.getAllCandidates();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(1).getUsername()).isEqualTo("Antonette");
        verify(candidateClient).getAllCandidates();
    }

    @Test
    void getAllCandidates_propagaCandidateServiceUnavailableExceptionCuandoElServicioFalla() {
        // Given
        when(candidateClient.getAllCandidates())
                .thenThrow(new CandidateServiceUnavailableException("JSONPlaceholder no disponible", null));

        // When / Then
        assertThatThrownBy(() -> candidateService.getAllCandidates())
                .isInstanceOf(CandidateServiceUnavailableException.class);
    }
}
