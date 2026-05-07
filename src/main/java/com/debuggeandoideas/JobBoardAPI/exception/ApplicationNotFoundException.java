package com.debuggeandoideas.JobBoardAPI.exception;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(Long id) {
        super("La postulación con id " + id + " no existe.");
    }
}
