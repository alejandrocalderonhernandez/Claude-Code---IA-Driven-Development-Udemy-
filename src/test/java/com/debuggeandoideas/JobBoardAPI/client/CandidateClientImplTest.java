package com.debuggeandoideas.JobBoardAPI.client;

import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateResponse;
import com.debuggeandoideas.JobBoardAPI.exception.CandidateNotFoundException;
import com.debuggeandoideas.JobBoardAPI.exception.CandidateServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CandidateClientImpl candidateClient;

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    @BeforeEach
    void configurarBaseUrl() {
        ReflectionTestUtils.setField(candidateClient, "baseUrl", BASE_URL);
    }

    // ─── getCandidateById ────────────────────────────────────────────────────

    @Test
    void getCandidateById_retornaElCandidatoCuandoExisteEnElServicioExterno() {
        // Given
        var candidatoEsperado = new CandidateResponse(1L, "Leanne Graham", "Bret",
                "sincere@april.biz", "555-1234", "hildegard.org", null, null);
        when(restTemplate.getForObject(BASE_URL + "/users/{id}", CandidateResponse.class, 1L))
                .thenReturn(candidatoEsperado);

        // When
        var resultado = candidateClient.getCandidateById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getName()).isEqualTo("Leanne Graham");
    }

    @Test
    void getCandidateById_lanzaCandidateNotFoundExceptionCuandoElServicioRetorna404() {
        // Given
        when(restTemplate.getForObject(BASE_URL + "/users/{id}", CandidateResponse.class, 99L))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When / Then
        assertThatThrownBy(() -> candidateClient.getCandidateById(99L))
                .isInstanceOf(CandidateNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getCandidateById_lanzaCandidateServiceUnavailableExceptionCuandoElServicioRetornaError5xx() {
        // Given
        when(restTemplate.getForObject(BASE_URL + "/users/{id}", CandidateResponse.class, 1L))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When / Then
        assertThatThrownBy(() -> candidateClient.getCandidateById(1L))
                .isInstanceOf(CandidateServiceUnavailableException.class);
    }

    @Test
    void getCandidateById_lanzaCandidateServiceUnavailableExceptionCuandoNoHayConexion() {
        // Given
        when(restTemplate.getForObject(BASE_URL + "/users/{id}", CandidateResponse.class, 1L))
                .thenThrow(new ResourceAccessException("Connection refused"));

        // When / Then
        assertThatThrownBy(() -> candidateClient.getCandidateById(1L))
                .isInstanceOf(CandidateServiceUnavailableException.class)
                .hasMessageContaining("no disponible");
    }

    // ─── getAllCandidates ────────────────────────────────────────────────────

    @Test
    void getAllCandidates_retornaTodosLosCandidatosDelServicioExterno() {
        // Given
        var candidatos = List.of(
                new CandidateResponse(1L, "Leanne Graham", "Bret", "sincere@april.biz", null, null, null, null),
                new CandidateResponse(2L, "Ervin Howell", "Antonette", "shanna@melissa.tv", null, null, null, null)
        );
        when(restTemplate.exchange(eq(BASE_URL + "/users"), eq(HttpMethod.GET), isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(candidatos));

        // When
        var resultado = candidateClient.getAllCandidates();

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getName()).isEqualTo("Leanne Graham");
    }

    @Test
    void getAllCandidates_lanzaCandidateServiceUnavailableExceptionCuandoElServicioNoEstaDisponible() {
        // Given
        when(restTemplate.exchange(eq(BASE_URL + "/users"), eq(HttpMethod.GET), isNull(),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        // When / Then
        assertThatThrownBy(() -> candidateClient.getAllCandidates())
                .isInstanceOf(CandidateServiceUnavailableException.class)
                .hasMessageContaining("no disponible");
    }
}
