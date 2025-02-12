package com.rolfje.anonimatron.anonymizer;

import com.rolfje.anonimatron.configuration.Column;
import com.rolfje.anonimatron.synonyms.NullSynonym;
import com.rolfje.anonimatron.synonyms.Synonym;
import org.apache.log4j.Logger;

import java.sql.Date;
import java.util.*;

public class AnonymizerService {
	private static final Logger LOG = Logger.getLogger(AnonymizerService.class);

	private Map<String, Anonymizer> customAnonymizers = new HashMap<>();
	private Map<String, String> defaultTypeMapping = new HashMap<>();

	private SynonymCache synonymCache;

	private Set<String> seenTypes = new HashSet<>();

	public AnonymizerService() throws Exception {
		this.synonymCache = new SynonymCache();

		// Custom anonymizers which produce more life-like data
		registerAnonymizer(new Bic());
		registerAnonymizer(new CharacterStringAnonymizer());
		registerAnonymizer(new CharacterStringPrefetchAnonymizer());
		registerAnonymizer(new City());
		registerAnonymizer(new CompanyName());
		registerAnonymizer(new CompanySuffix());
		registerAnonymizer(new CountryCodeAnonymizer());
		registerAnonymizer(new DateAnonymizer());
		registerAnonymizer(new DigitStringAnonymizer());
		registerAnonymizer(new DutchBankAccountAnononymizer());
		registerAnonymizer(new DutchBSNAnononymizer());
		registerAnonymizer(new DutchZipCodeAnonymizer());
		registerAnonymizer(new ElvenNameGenerator());
		registerAnonymizer(new EmailAddressAnonymizer());
		registerAnonymizer(new FirstName());
		registerAnonymizer(new FixedValue());
		registerAnonymizer(new FullAddress());
		registerAnonymizer(new FullName());
		registerAnonymizer(new IbanAnonymizer());
		registerAnonymizer(new IbanFromList());
		registerAnonymizer(new IPAddressV4Anonymizer());
		registerAnonymizer(new LastName());
		registerAnonymizer(new NifCode());
		registerAnonymizer(new Numerify());
		registerAnonymizer(new PersonJob());
		registerAnonymizer(new PersonPrefix());
		registerAnonymizer(new PhoneNumber());
		registerAnonymizer(new PostalCode());
		registerAnonymizer(new RomanNameGenerator());
		registerAnonymizer(new SecondaryAddress());
		registerAnonymizer(new State());
		registerAnonymizer(new StreetAddress());
		registerAnonymizer(new StringAnonymizer());
		registerAnonymizer(new UUIDAnonymizer());

		// Default anonymizers for plain Java objects. If we really don't
		// know or care how the data looks like.
		defaultTypeMapping.put(String.class.getName(), new StringAnonymizer().getType());
		defaultTypeMapping.put(Date.class.getName(), new DateAnonymizer().getType());
	}

	public AnonymizerService(SynonymCache synonymCache) throws Exception {
		this();
		this.synonymCache = synonymCache;
	}

	public void registerAnonymizers(List<String> anonymizers) {
		if (anonymizers == null) {
			return;
		}

		for (String anonymizer : anonymizers) {
			try {
				@SuppressWarnings("rawtypes")
				Class anonymizerClass = Class.forName(anonymizer);
				registerAnonymizer((Anonymizer)anonymizerClass.newInstance());
			} catch (Exception e) {
				LOG.fatal(
					"Could not instantiate class "
							+ anonymizer
							+ ". Please make sure that the class is on the classpath, "
							+ "and it has a default public constructor.", e);
			}
		}

		LOG.info(anonymizers.size()+" anonymizers registered.");
	}

	public Set<String> getCustomAnonymizerTypes() {
		return Collections.unmodifiableSet(customAnonymizers.keySet());
	}

	public Set<String> getDefaultAnonymizerTypes() {
		return Collections.unmodifiableSet(defaultTypeMapping.keySet());
	}

	public Synonym anonymize(Column column, Object from) {
		if (from == null) {
			return new NullSynonym(column.getType());
		}

		// Find for regular type
		Synonym synonym = getSynonym(column, from);

		if (synonym == null) {
            Anonymizer anonymizer = getAnonymizer(column.getType());
            synonym = anonymizer.anonymize(from, column.getSize(), column.isShortLived(), column.getParameters());

			synonymCache.put(synonym);
		}
		return synonym;
	}

	private Synonym getSynonym(Column c, Object from) {
		if (c.isShortLived()){
			return null;
		}

		Synonym synonym = synonymCache.get(c.getType(), from);
		if (synonym == null) {
			// Fallback for default type
			synonym = synonymCache.get(defaultTypeMapping.get(c.getType()), from);
		}
		return synonym;
	}

	private void registerAnonymizer(Anonymizer anonymizer) {
		if (customAnonymizers.containsKey(anonymizer.getType())) {
			// Do not allow overriding Anonymizers
			throw new UnsupportedOperationException(
					"Could not register anonymizer with type "
							+ anonymizer.getType()
							+ " and class "
							+ anonymizer.getClass().getName()
							+ " because there is already an anonymizer registered for type "
							+ anonymizer.getType());
		}

		customAnonymizers.put(anonymizer.getType(), anonymizer);
	}



	private Anonymizer getAnonymizer(String type) {
		if (type == null) {
			throw new UnsupportedOperationException(
					"Can not anonymyze without knowing the column type.");
		}

		Anonymizer anonymizer = customAnonymizers.get(type);
		if (anonymizer == null) {

			if (!seenTypes.contains(type)) {
				// Log this unknown type
				LOG.warn("Unknown type '" + type
						+ "', trying fallback to default anonymizer for this type.");
				seenTypes.add(type);
			}

			// Fall back to default if we don't know how to handle this
			anonymizer = customAnonymizers.get(defaultTypeMapping.get(type));
		}

		if (anonymizer == null) {
			// Fall back did not work, give up.
			throw new UnsupportedOperationException(
					"Do not know how to anonymize type '" + type
							+ "'.");
		}
		return anonymizer;
	}

	public boolean prepare(String type, Object databaseColumnValue) {
		Anonymizer anonymizer = getAnonymizer(type);
		if (anonymizer != null && anonymizer instanceof Prefetcher){
			((Prefetcher)anonymizer).prefetch(databaseColumnValue);
			return true;
		}

		return false;
	}

	public SynonymCache getSynonymCache() {
		return synonymCache;
	}
}
