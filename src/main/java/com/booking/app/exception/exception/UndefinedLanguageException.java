package com.booking.app.exception.exception;


import org.springframework.http.HttpHeaders;


public class UndefinedLanguageException extends RuntimeException {
    public static final String UKRAINIAN_LANGUAGE = "ua";
    public static final String ENGLISH_LANGUAGE = "eng";
    private static final String MESSAGE = "Incomprehensible language passed into " +
            HttpHeaders.CONTENT_LANGUAGE +
            String.format("%s or %s is required", UKRAINIAN_LANGUAGE, ENGLISH_LANGUAGE);

    public UndefinedLanguageException() {
        super(MESSAGE);
    }
}
