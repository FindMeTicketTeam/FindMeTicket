package com.booking.app.enums;

import lombok.Getter;

@Getter
public enum ContentLanguage {
    UA("ua"),
    ENG("eng");

    private final String language;

    ContentLanguage(String language) {
        this.language = language;
    }

    public static ContentLanguage fromCode(String code) {
        for (ContentLanguage language : ContentLanguage.values()) {
            if (language.getLanguage().equalsIgnoreCase(code)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Unknown language code: " + code);
    }
}
