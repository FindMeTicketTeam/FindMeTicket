package com.booking.app.enums;

import com.booking.app.exception.exception.UndefinedLanguageException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToSiteLanguageConverter implements Converter<String, ContentLanguage> {

    @Override
    public ContentLanguage convert(String source) {
        try {
            return ContentLanguage.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UndefinedLanguageException();
        }
    }

}
