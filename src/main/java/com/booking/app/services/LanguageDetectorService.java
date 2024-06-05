package com.booking.app.services;

import java.io.IOException;
import java.util.Optional;

public interface LanguageDetectorService {

    /**
     * Detects the language of the given text.
     *
     * @param letters the text for which the language needs to be detected
     * @return an Optional containing the detected language, or an empty Optional if detection fails
     * @throws IOException if an I/O error occurs while reading the language profiles
     */
    Optional<String> detectLanguage(String letters) throws IOException;

}
