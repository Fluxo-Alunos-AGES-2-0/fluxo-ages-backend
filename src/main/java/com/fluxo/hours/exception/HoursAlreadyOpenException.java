package com.fluxo.hours.exception;

public class HoursAlreadyOpenException extends RuntimeException {

    public HoursAlreadyOpenException(String message) {
        super(message);
    }
}
