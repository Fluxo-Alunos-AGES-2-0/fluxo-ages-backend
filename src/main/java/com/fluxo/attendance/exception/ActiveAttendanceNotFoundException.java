package com.fluxo.attendance.exception;

public class ActiveAttendanceNotFoundException extends RuntimeException {

    public ActiveAttendanceNotFoundException(String message) {
        super(message);
    }
}