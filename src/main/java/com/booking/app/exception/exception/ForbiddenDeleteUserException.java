package com.booking.app.exception.exception;


public class ForbiddenDeleteUserException extends RuntimeException {
    public ForbiddenDeleteUserException(String message) {
        super(message);
    }
}
