package com.streambus.commonmodule.utils;

import android.content.Context;
import android.text.TextUtils;

import com.streambus.basemodule.utils.PreferencesUtils;
import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.Constants;
import com.streambus.commonmodule.R;
import com.streambus.commonmodule.api.RequestApi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LanguageUtils {

    private static final String TAG = "LanguageUtils";
    public static final String PT = "Português";
    public static final String ES = "Español";
    public static final String EN = "English";
    public static final String NO_SUBTITLE = "No Subtitle";
    public static final String UNKNOWN_AUDIO = "Unknown Audio";

    public static String DEFAULT_SUBTITLE_LANGUAGE = PreferencesUtils.get(Constants.KEY_SETTING_SUBTITLE_VALUE, "pt");
    public static String DEFAULT_AUDIO_LANGUAGE = PreferencesUtils.get(Constants.KEY_SETTING_AUDIO_VALUE, "pt");

    public static final List<String> DEFAULT_MATCH = Arrays.asList("pt", "es", "en");

    public static final HashMap<String, List<String>> AUDIO_MATCH = new HashMap<>();
    static {
        AUDIO_MATCH.put("pt", Arrays.asList("pt", "es", "en"));
        AUDIO_MATCH.put("es", Arrays.asList("es", "pt", "en"));
        AUDIO_MATCH.put("en", Arrays.asList("en", "pt", "es"));
    }

    public static final HashMap<String, List<String>> SUBTITLE_MATCH = new HashMap<>();
    static {
        SUBTITLE_MATCH.put("Portuguese", Arrays.asList("Portuguese", "Spanish", "English"));
        SUBTITLE_MATCH.put("Spanish", Arrays.asList("Spanish", "Portuguese", "English"));
        SUBTITLE_MATCH.put("English", Arrays.asList("English", "Portuguese", "Spanish"));
    }

    public static List<String> getDefaultSubtitleMatch() {
        return SUBTITLE_MATCH.get("Portuguese");
    }

//    public static String getDisplayLanguage(String language) {
//        return new Locale(language).getDisplayLanguage();
//    }

    public static String getDisplayLanguage(Context context, String language) {
        String tranLanguage;
        SLog.d(TAG, "getDisplayLanguage language=" + language + "  Locale=" + RequestApi.LANGUAGE);
        if ("Portuguese".equalsIgnoreCase(language)){
            language = "pt";
        } else if ("Spanish".equalsIgnoreCase(language)) {
            language = "es";
        } else if ("English".equalsIgnoreCase(language)) {
            language = "en";
        }
        SLog.d(TAG, "getDisplayLanguage 222 language=" + language + "  Locale=" + RequestApi.LANGUAGE);
        if (RequestApi.LANGUAGE.startsWith("pt") || RequestApi.LANGUAGE.startsWith("es")) {
            tranLanguage =  new Locale(language).getDisplayLanguage(new Locale(RequestApi.LANGUAGE));
        } else {
            tranLanguage =  new Locale(language).getDisplayLanguage(new Locale("eng", "US"));
        }
        if (NO_SUBTITLE.equalsIgnoreCase(tranLanguage)) {
            tranLanguage = context.getString(R.string.language_no_subtitle);
        } else if (UNKNOWN_AUDIO.equalsIgnoreCase(tranLanguage)) {
            tranLanguage = context.getString(R.string.language_unknown_audio);
        }
        return tranLanguage;
    }

    private static Locale ENGLISH_LOCALE = new Locale("en");
    public static String getDisplayLanguageEnglish(String language) {
        return new Locale(language).getDisplayLanguage(ENGLISH_LOCALE);
    }

    //简写
    public static String getDefaultAudioLanguage() {
        if (!TextUtils.isEmpty(PreferencesUtils.get(Constants.KEY_SETTING_AUDIO_VALUE, ""))) {
            return DEFAULT_AUDIO_LANGUAGE;
        }
        if (DEFAULT_MATCH.contains(RequestApi.LANGUAGE)) {
            return RequestApi.LANGUAGE;
        }
        return DEFAULT_AUDIO_LANGUAGE;
    }

    //需要全称
    public static String getDefaultSubtitleLanguage() {
        if (!TextUtils.isEmpty(PreferencesUtils.get(Constants.KEY_SETTING_SUBTITLE_VALUE, ""))) {
            return getDisplayLanguageEnglish(DEFAULT_SUBTITLE_LANGUAGE);
        }
        if (DEFAULT_MATCH.contains(RequestApi.LANGUAGE)) {
            return getDisplayLanguageEnglish(RequestApi.LANGUAGE);
        }
        return getDisplayLanguageEnglish(DEFAULT_SUBTITLE_LANGUAGE);
    }

    public static boolean equalsLanguage(String language1, String language2) {
        if (language1.equalsIgnoreCase(language2)) {
            return true;
        }
        return getDisplayLanguageEnglish(language1).equalsIgnoreCase(getDisplayLanguageEnglish(language2));
    }
}
