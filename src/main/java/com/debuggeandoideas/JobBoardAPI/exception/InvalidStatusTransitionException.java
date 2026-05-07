package com.debuggeandoideas.JobBoardAPI.exception;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(Long id) {
        super("La postulación con id " + id + " ya tiene un estado final y no puede modificarse.");
    }
}
