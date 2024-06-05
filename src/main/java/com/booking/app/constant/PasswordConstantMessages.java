package com.booking.app.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordConstantMessages {
    public static final String MESSAGE_CODE_HAS_BEEN_SENT = "Code has been sent";
    public static final String MESSAGE_PASSWORD_HAS_BEEN_SUCCESSFULLY_RESET = "Password has been successfully reset";
    public static final String MESSAGE_NEW_PASSWORD_HAS_BEEN_CREATED = "New password has been created";
    public static final String MESSAGE_CURRENT_PASSWORD_IS_NOT_RIGHT = "The passed current password is not right.";
}