package com.booking.app.services;

import com.booking.app.exception.exception.UserNotFoundException;


public interface MailSenderService {

    /**
     * Resends a confirmation email with a new confirmation token to the user.
     *
     * @param language the language preference for the email content
     * @param email    the email address of the user to resend the confirmation to
     * @throws UserNotFoundException if no user with the given email is found
     */
    void sendVerificationCode(String email, String language) throws UserNotFoundException;

    /**
     * Sends a reset code to the given email address.
     * If the user is found and their account is enabled, a new confirmation code is created and emailed.
     *
     * @param email    the email address to send the reset code to
     * @param language the language preference for the email content
     */
    void sendResetCode(String email, String language);

}
