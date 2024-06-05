package com.booking.app.enums;

import com.booking.app.exception.exception.InvalidSocialProviderException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToSocialProviderConverter implements Converter<String, SocialProvider> {
    @Override
    public SocialProvider convert(String source) {
        try {
            return SocialProvider.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidSocialProviderException();
        }
    }
}
