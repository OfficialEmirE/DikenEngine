package me.ramazanenescik04.diken.language;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Represents the `Language` type within the DikenEngine `resource` package.
 */
public final class Language {
	private static final Language[] languages = new Language[256];
	public static Language ENGLISH = new Language(0, Locale.ENGLISH, "/lang/en-US.lang");
	public static Language TURKISH = new Language(1, Locale.forLanguageTag("tr"), "/lang/tr-TR.lang");
	
	// Language registry
	private int id;
	private Locale locale;
	private List<Translation> translations;
	
	private Language(int id, Locale locale, String translationsFilePath) {
		this.id = id;
				
	    if (languages[id] != null) {
	        throw new IllegalArgumentException("Language with ID " + id + " already exists.");
	    }
		
		this.locale = locale;
		this.translations = Translation.load(translationsFilePath);
	}
	
	private Language(int id, Locale locale, List<Translation> translations) {
		this.id = id;
				
	    if (languages[id] != null) {
	        throw new IllegalArgumentException("Language with ID " + id + " already exists.");
	    }
		
		this.locale = locale;
		this.translations = translations;
	}
	
	public int getId() {
		return this.id;
	}
	
	public Locale getLocale() {
		return this.locale;
	}
	
	public List<Translation> getTranslations() {
		return this.translations;
	}
	
	public void addAllTranslations(List<Translation> translations) {
		this.translations.addAll(translations);
	}
	
	public String getTranslation(String key) {
		for (var translation : this.translations) {
			if (translation.key().equals(key)) {
				return translation.value();
			}
		}
		
		return key; // Return the key itself if no translation is found
	}
	
	public static Language getLanguageById(int id) {
		if (id < 0 || id >= languages.length) {
			throw new IllegalArgumentException("Invalid language ID: " + id);
		}
		
		return languages[id];
	}
	
	public static Language getLanguageByLocale(Locale locale) {
		for (var language : languages) {
			if (language != null && language.locale.equals(locale)) {
				return language;
			}
		}
		
		return null; // Return null if no matching language is found
	}

	public static int[] getLanguageListId() {
		int[] languageIds = new int[languages.length];
		for (int i = 0; i < languages.length; i++) {
			languageIds[i] = languages[i] != null ? languages[i].id : -1;
		}
		return languageIds;
	}
	
	public static Integer[] getLanguageListIdBoxed() {
		Integer[] languageIds = new Integer[languages.length];
		for (int i = 0; i < languages.length; i++) {
			languageIds[i] = languages[i] != null ? languages[i].id : null;
		}
		return languageIds;
	}
	
	public static Language[] getLanguages() {
		return Arrays.copyOf(languages, languages.length);
	}
}
