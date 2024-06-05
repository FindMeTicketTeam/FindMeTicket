package com.booking.app.exception.exception;

import com.booking.app.constant.ApiMessagesConstants;

public class InvalidSocialProviderException extends RuntimeException {
    public InvalidSocialProviderException() {
        super(ApiMessagesConstants.NO_SOCIAL_IDENTITY_PROVIDERS_MESSAGE);
    }
}
