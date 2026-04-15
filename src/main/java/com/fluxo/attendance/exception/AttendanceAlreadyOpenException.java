package com.fluxo.attendance.exception;

public class AttendanceAlreadyOpenException extends RuntimeException {

    public AttendanceAlreadyOpenException(String message) {
        super(message);
    }
}