package com.booking.app.services;

import com.booking.app.entity.ConfirmationCode;
import com.booking.app.exception.exception.InvalidConfirmationCodeException;

public interface ConfirmationCodeService {

    /**
     * Saves a confirmation code.
     *
     * @param confirmationCode The confirmation code to save.
     * @return The saved confirmation code.
     */
    ConfirmationCode save(ConfirmationCode confirmationCode);


    /**
     * Deletes a confirmation code.
     *
     * @param confirmationCode The confirmation code to delete.
     */
    void delete(ConfirmationCode confirmationCode);

    /**
     * Finds a confirmation code by its code.
     *
     * @param confirmationCode The code to search for.
     * @return The confirmation code if found, null otherwise.
     */
    ConfirmationCode findByCode(String confirmationCode);

    /**
     * Verifies a confirmation code.
     *
     * @param confirmationCode The confirmation code to verify.
     * @param givenCode        The code to compare with the confirmation code.
     * @return True if the given code matches the confirmation code and it's not expired, false otherwise.
     * @throws InvalidConfirmationCodeException If the code verification fails.
     */
    boolean verifyCode(ConfirmationCode confirmationCode, String givenCode) throws InvalidConfirmationCodeException;

    /**
     * Updates an existing confirmation code with new code and expiry time.
     *
     * @param newCode      The new confirmation code.
     * @param existingCode The existing confirmation code to be updated.
     */
    void updateConfirmationCode(ConfirmationCode newCode, ConfirmationCode existingCode);

}
