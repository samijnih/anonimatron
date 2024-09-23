package com.rolfje.anonimatron.anonymizer;

import com.github.javafaker.Faker;
import com.rolfje.anonimatron.synonyms.StringSynonym;
import com.rolfje.anonimatron.synonyms.Synonym;

import java.util.*;

public class IbanFromList implements Anonymizer {
    static Faker faker = new Faker();

    @Override
    public Synonym anonymize(Object from, int size, boolean shortlived) {
        return anonymize(from, size, shortlived, new HashMap<>());
    }

    @Override
    public Synonym anonymize(Object from, int size, boolean shortlived, Map<String, String> parameters) {
        if (parameters == null || !parameters.containsKey("countryCodes")) {
            throw new UnsupportedOperationException("missing parameter countryCodes");
        }

        Random r = new Random();
        List<String> countryCodes = Arrays.asList(parameters.get("countryCodes").split(","));

        return new StringSynonym(
            getType(),
            (String) from,
            faker.finance().iban(countryCodes.get(r.nextInt(countryCodes.size()))),
            shortlived
        );
    }

    @Override
    public String getType() {
        return "IBAN_FROM_LIST";
    }
}
