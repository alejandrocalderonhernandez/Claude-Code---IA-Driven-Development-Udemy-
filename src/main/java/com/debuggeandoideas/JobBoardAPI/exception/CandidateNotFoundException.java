package com.debuggeandoideas.JobBoardAPI.exception;

public class CandidateNotFoundException extends RuntimeException {
    public CandidateNotFoundException(Long id) {
        super("No existe un candidato con id " + id + ".");
    }
}
