package me.ramazanenescik04.diken.language;

import org.json.JSONException;
import org.json.JSONObject;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.log.ConsoleLog;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Basit çoklu dil (i18n) yönetim sınıfı.
 *
 * Dosya yapısı (classpath üzerinde):
 *   /lang/index.json   -> { "kod": "Görünen İsim", ... }
 *   /lang/{kod}.json    -> { "key": "çeviri", ... }
 */
public final class Lang {

    private static final String BASE_PATH = "/lang/";
    private static final String DEFAULT_LANG = "en";

    private static final Map<String, JSONObject> translationCache = new LinkedHashMap<>();
    private static Map<String, String> availableLanguages; // kod -> görünen isim, lazy

    private static String currentLang = DEFAULT_LANG;

    private Lang() {}

    // ------------------------------------------------------------
    // Aktif dil
    // ------------------------------------------------------------

    public static void setLanguage(String langCode) {
        currentLang = langCode;
        loadTranslations(langCode); // erken hata tespiti için hemen yükle
        
        ConsoleLog.sendLog("[Lang] Dil değiştirildi: " + langCode);
    }

    public static String getCurrentLanguage() {
        return currentLang;
    }

    // ------------------------------------------------------------
    // Çeviri okuma
    // ------------------------------------------------------------

    /** Key'e karşılık gelen metni döner. Bulunamazsa key'in kendisini döner. */
    public static String get(String key) {
        JSONObject translations = loadTranslations(currentLang);

        if (translations != null && translations.has(key)) {
            return translations.getString(key);
        }

        if (!currentLang.equals(DEFAULT_LANG)) {
            JSONObject fallback = loadTranslations(DEFAULT_LANG);
            if (fallback != null && fallback.has(key)) {
                return fallback.getString(key);
            }
        }

        return key;
    }

    /** String.format destekli versiyon. Örn: get("player_joined", "Enes") */
    public static String get(String key, Object... args) {
        String raw = get(key);
        try {
            return String.format(raw, args);
        } catch (java.util.IllegalFormatException e) {
            return raw;
        }
    }

    // ------------------------------------------------------------
    // Mevcut diller (index.json)
    // ------------------------------------------------------------

    /** Mevcut dilleri döner: kod -> görünen isim. Örn: {"tr": "Türkçe", "en": "English"} */
    public static Map<String, String> getAvailableLanguages() {
        if (availableLanguages == null) {
            availableLanguages = loadIndex();
        }
        return Collections.unmodifiableMap(availableLanguages);
    }

    /** Bir dil kodunun index.json'da tanımlı olup olmadığını kontrol eder. */
    public static boolean isLanguageAvailable(String langCode) {
        return getAvailableLanguages().containsKey(langCode);
    }

    // ------------------------------------------------------------
    // Dahili yükleme
    // ------------------------------------------------------------

    private static JSONObject loadTranslations(String langCode) {
        return translationCache.computeIfAbsent(langCode, Lang::readJsonResource0);
    }

    private static JSONObject readJsonResource0(String langCode) {
        return readJsonResource(BASE_PATH + langCode + ".json");
    }

    private static Map<String, String> loadIndex() {
        Map<String, String> result = new LinkedHashMap<>();
        JSONObject json = readJsonResource(BASE_PATH + "index.json");

        if (json == null) {
            DikenEngine.errorLog("[Lang] index.json bulunamadı, sadece varsayılan dil kullanılabilir.");
            return result;
        }

        for (String code : json.keySet()) {
            result.put(code, json.getString(code));
        }
        return result;
    }

    private static JSONObject readJsonResource(String path) {
        try (InputStream is = Lang.class.getResourceAsStream(path)) {
            if (is == null) {
            	DikenEngine.errorLog("[Lang] Bulunamadı: " + path);
                return null;
            }
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new JSONObject(content);
        } catch (IOException | JSONException e) {
        	DikenEngine.errorLog("[Lang] Okunamadı: " + path + " -> " + e.getMessage());
            return null;
        }
    }
}