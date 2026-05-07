package com.debuggeandoideas.JobBoardAPI.exception;

public class DuplicateApplicationException extends RuntimeException {
    public DuplicateApplicationException(Long candidateId, Long jobId) {
        super("El candidato " + candidateId + " ya postuló a la oferta " + jobId + ".");
    }
}
