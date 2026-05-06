package com.debuggeandoideas.JobBoardAPI.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CandidateNotFoundException extends RuntimeException {
    public CandidateNotFoundException(Long id) {
        super("No existe un candidato con id " + id + ".");
    }
}
