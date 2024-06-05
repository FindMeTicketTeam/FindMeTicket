package com.booking.app.util;

import com.booking.app.exception.exception.UndefinedLanguageException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class HtmlTemplateUtils {

    /**
     * Return HTML user confirmation template name based on language
     *
     * @param language Language ua/eng
     * @return HTML template name
     */
    public static String getConfirmationTemplate(String language) {
        return switch (language) {
            case ("ua") -> "confirmMailUa";
            case ("eng") -> "confirmMailEng";
            default -> throw new UndefinedLanguageException();
        };
    }

    /**
     * Return HTML reset password template name based on language
     *
     * @param language Language ua/eng
     * @return HTML template name
     */
    public static String getResetPasswordTemplate(String language) {
        return switch (language) {
            case ("ua") -> "resetPasswordUa";
            case ("eng") -> "resetPasswordEng";
            default -> throw new UndefinedLanguageException();
        };
    }

}
