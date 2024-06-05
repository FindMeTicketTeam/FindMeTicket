package com.booking.app.services.impl;

import com.booking.app.services.LanguageDetectorService;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for language detection.
 */
@Service
@RequiredArgsConstructor
public class LanguageDetectorServiceImpl implements LanguageDetectorService {

    @Value("${languages}")
    private final List<String> languageProfiles;

    @Override
    public Optional<String> detectLanguage(String letters) throws IOException {
        List<LanguageProfile> loadedProfiles = new LanguageProfileReader().read(languageProfiles);
        LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(loadedProfiles)
                .build();
        TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
        TextObject textObject = textObjectFactory.forText(letters);
        com.google.common.base.Optional<LdLocale> detectedLanguage = languageDetector.detect(textObject);
        return detectedLanguage.transform(LdLocale::getLanguage)
                .toJavaUtil();
    }

}
