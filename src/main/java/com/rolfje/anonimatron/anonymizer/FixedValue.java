package com.rolfje.anonimatron.anonymizer;

import com.github.javafaker.Faker;
import com.rolfje.anonimatron.synonyms.StringSynonym;
import com.rolfje.anonimatron.synonyms.Synonym;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FixedValue implements Anonymizer {
    @Override
    public Synonym anonymize(Object from, int size, boolean shortlived) {
        return anonymize(from, size, shortlived, new HashMap<>());
    }

    @Override
    public Synonym anonymize(Object from, int size, boolean shortlived, Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            throw new UnsupportedOperationException("expected parameter value");
        }

        return new StringSynonym(
            getType(),
            (String) from,
            parameters.get("value"),
            shortlived
        );
    }

    @Override
    public String getType() {
        return "FIXED_VALUE";
    }
}
