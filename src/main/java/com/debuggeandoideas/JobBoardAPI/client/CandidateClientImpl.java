package com.debuggeandoideas.JobBoardAPI.client;

import com.debuggeandoideas.JobBoardAPI.dto.response.CandidateResponse;
import com.debuggeandoideas.JobBoardAPI.exception.CandidateNotFoundException;
import com.debuggeandoideas.JobBoardAPI.exception.CandidateServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CandidateClientImpl implements CandidateClient {

    private final RestTemplate restTemplate;

    @Value("${candidate.service.base-url}")
    private String baseUrl;

    @Override
    public CandidateResponse getCandidateById(Long id) {
        // TO DO UNIT TEST
        try {
            return restTemplate.getForObject(baseUrl + "/users/{id}", CandidateResponse.class, id);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new CandidateNotFoundException(id);
            }
            throw new CandidateServiceUnavailableException("Error al consultar candidato con id " + id, e);
        } catch (RestClientException e) {
            throw new CandidateServiceUnavailableException("JSONPlaceholder no disponible", e);
        }
    }

    @Override
    public List<CandidateResponse> getAllCandidates() {
        // TO DO UNIT TEST
        try {
            return restTemplate.exchange(
                    baseUrl + "/users",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CandidateResponse>>() {}
            ).getBody();
        } catch (RestClientException e) {
            throw new CandidateServiceUnavailableException("JSONPlaceholder no disponible", e);
        }
    }
}
