package com.debuggeandoideas.JobBoardAPI.exception;

public class CandidateServiceUnavailableException extends RuntimeException {
    public CandidateServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
