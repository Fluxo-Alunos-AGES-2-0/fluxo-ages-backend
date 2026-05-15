package com.fluxo.report.exception;

public class ReportStorageException extends RuntimeException {

    public ReportStorageException(String message) {
        super(message);
    }

    public ReportStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
