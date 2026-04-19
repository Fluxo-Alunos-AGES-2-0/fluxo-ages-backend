package com.fluxo.hours.exception;

public class ActiveHoursNotFoundException extends RuntimeException {

    public ActiveHoursNotFoundException(String message) {
        super(message);
    }
}
