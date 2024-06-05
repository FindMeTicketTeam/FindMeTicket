package com.booking.app.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrationConstantMessages {
    public static final String EMAIL_IS_ALREADY_TAKEN_MESSAGE = "We’re sorry. This email already taken.";
    public static final String USER_REGISTERED_SUCCESSFULLY_MESSAGE = "User registered successfully.";
    public static final String CODE_IS_NOT_RIGHT_MESSAGE = "Code is not right";
    public static final String USER_IS_VERIFIED_MESSAGE = "User is verified";
    public static final String CONFIRM_CODE_HAS_BEEN_SENT_ONE_MORE_TIME_MESSAGE = "Confirm token has been sent";
    public static final String THE_SPECIFIED_EMAIL_IS_NOT_REGISTERED_MESSAGE = "The specified email is not registered";
}

