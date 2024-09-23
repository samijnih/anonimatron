package com.rolfje.anonimatron.anonymizer;

import com.github.javafaker.Faker;
import com.rolfje.anonimatron.synonyms.StringSynonym;
import com.rolfje.anonimatron.synonyms.Synonym;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class State implements Anonymizer {
    static Faker faker = new Faker(new Locale("fr"));

    @Override
    public Synonym anonymize(Object from, int size, boolean shortlived) {
        return anonymize(from, size, shortlived, new HashMap<>());
    }

    @Override
    public Synonym anonymize(Object from, int size, boolean shortlived, Map<String, String> parameters) {
        return new StringSynonym(
            getType(),
            (String) from,
            faker.address().state(),
            shortlived
        );
    }

    @Override
    public String getType() {
        return "STATE";
    }
}
