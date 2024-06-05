package com.booking.app.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiMessagesConstants {
    public static final String USER_HAS_BEEN_DELETED_MESSSAGE = "User was deleted successfully";
    public static final String UNAUTHENTICATED_MESSAGE = "Full authentication is required to access this resource";
    public static final String INVALID_CLIENT_PROVIDER_ID_MESSAGE = "Client ID is not right";
    public static final String USER_HAS_BEEN_AUTHENTICATED_MESSAGE = "User has been authenticated";
    public static final String NO_SOCIAL_IDENTITY_PROVIDERS_MESSAGE = "Illegal Social Identity Providers was chosen";
    public static final String INVALID_REQUEST_BODY_FORMAT_OR_VALIDATION_ERROR_MESSAGE = "Invalid request body format OR validation error";
    public static final String INVALID_CONTENT_LANGUAGE_HEADER_MESSAGE = "Incomprehensible language passed into ContentLanguage header";
}
