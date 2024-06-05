package com.booking.app.exception.exception;


import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserNotFoundException extends UsernameNotFoundException {
    public static final String THE_SPECIFIED_EMAIL_IS_NOT_REGISTERED_MESSAGE = "The specified email is not registered";

    public UserNotFoundException() {
        super(THE_SPECIFIED_EMAIL_IS_NOT_REGISTERED_MESSAGE);
    }
}
