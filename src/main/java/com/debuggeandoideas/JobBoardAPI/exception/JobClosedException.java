package com.debuggeandoideas.JobBoardAPI.exception;

public class JobClosedException extends RuntimeException {
    public JobClosedException(Long jobId) {
        super("La oferta con id " + jobId + " está cerrada y no acepta postulaciones.");
    }
}
