package com.app.external;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.app.lystiq.Language;
import com.app.lystiq.R;
import com.app.utils.Constants;

import java.util.Arrays;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by hitasoft on 9/1/17.
 */

public class TimeAgo {
    protected Context context;

    String[] languages, langCode;
    String languageCode,selectedLang,TAG = "TimeAgo";

    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;

    public TimeAgo(Context context) {
        this.context = context;
    }

    public String timeAgo(Date date) {
        return timeAgo(date.getTime());
    }

    public String timeAgo(long millis) {

        pref = context.getSharedPreferences("JoysalePref", MODE_PRIVATE);
        editor = pref.edit();

        languages = context.getResources().getStringArray(R.array.languages);
        langCode = context.getResources().getStringArray(R.array.languageCode);
        selectedLang= pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);

        int index = Arrays.asList(languages).indexOf(selectedLang);
        languageCode = Arrays.asList(langCode).get(index);
        Log.v(TAG, "languageCode=" + languageCode);

        long diff = new Date().getTime() - millis;
        Resources r = context.getResources();
        String prefix = r.getString(R.string.time_ago_prefix);
        String suffix = r.getString(R.string.time_ago_suffix);
        double seconds = Math.abs(diff) / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        double days = hours / 24;
        double weeks = days / 7;
        double years = days / 365;
        String words;
        if (seconds < 45) {
            words = r.getString(R.string.time_ago_seconds, Math.round(seconds));
        } else if (seconds < 90) {
            words = r.getString(R.string.time_ago_minute, 1);
        } else if (minutes < 45) {
            words = r.getString(R.string.time_ago_minutes, Math.round(minutes));
        } else if (minutes < 90) {
            words = r.getString(R.string.time_ago_hour, 1);
        } else if (hours < 24) {
            words = r.getString(R.string.time_ago_hours, Math.round(hours));
        } else if (hours < 42) {
            words = r.getString(R.string.time_ago_day, 1);
        } else if (days < 30) {
            words = r.getString(R.string.time_ago_days, Math.round(days));
        } else if (days < 45) {
            words = r.getString(R.string.time_ago_month, 1);
        } else if (days < 365) {
            words = r.getString(R.string.time_ago_months, Math.round(days / 30));
        } else if (years < 1.5) {
            words = r.getString(R.string.time_ago_year, 1);
        } else {
            words = r.getString(R.string.time_ago_years, Math.round(years));
        }
        StringBuilder sb = new StringBuilder();
        if (prefix != null && prefix.length() > 0) {
            sb.append(prefix).append(" ");
        }
        if(languageCode.equalsIgnoreCase("fr")){
            if (suffix != null && suffix.length() > 0 && !words.equals(r.getString(R.string.time_ago_seconds))) {
                sb.append(suffix);
            }
            sb.append(" ").append(words);
        }else{
            sb.append(words);
            if (suffix != null && suffix.length() > 0 && !words.equals(r.getString(R.string.time_ago_seconds))) {
                sb.append(" ").append(suffix);
            }
        }

        return sb.toString().trim();
    }
}
