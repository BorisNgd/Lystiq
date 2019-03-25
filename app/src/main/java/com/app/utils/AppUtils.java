package com.app.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.app.lystiq.R;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by hitasoft on 21/11/17.
 * <p>
 * This class is Util Methods for All Classes.
 */

public class AppUtils {

    public static final String IMG_SENT_PATH = "images/Sent/";
    public static final String IMG_PROFILE_PATH = "images/Profile/";
    public static final String IMG_HOME_PATH = "images/";
    public static final String IMG_THUMBNAIL_PATH = "images/.thumbnails/";

    /**
     *Method for get Current Language Code from Preference
     * */

    public static String getCurrentLanguageCode(Context context) {
        String[] languages = context.getResources().getStringArray(R.array.languages);
        String[] langCode = context.getResources().getStringArray(R.array.languageCode);
        String selectedLang = Constants.pref.getString("language", Constants.LANGUAGE);
        int index = Arrays.asList(languages).indexOf(selectedLang);
        final String languageCode = Arrays.asList(langCode).get(index);
        return languageCode;
    }

    /**
     *Method for Download Images from Given Url
     * */

    public static Bitmap downloadImage(String src) {
        Log.v("AppUtils","downloadImgUrl="+src);
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
            return myBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *Method for Call a Installed Google Maps Application and display a given lat and long
     * */

    public static void callMap(String latitude, String longitude, Context context) {
        String latandlong = latitude + "," + longitude;
        Uri gmmIntentUri = Uri.parse("geo:" + latandlong + "?q=" + latandlong);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            Toast.makeText(context, context.getString(R.string.map_required_message), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *Method for get Image Name from a Given Url
     * */

    public static String getImageName(String url) {
        String imgSplit = url;

        int endIndex = !imgSplit.equals("") ? imgSplit.lastIndexOf("/") : 0;

        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1, imgSplit.length());
        }
        return imgSplit;
    }

    /**
     *Method for get a valid url from a Given url
     * */

    public static String getValidUrl(String url) {
        URI uri = null;
        try {
            uri = new URI(url.replace(" ", "%20"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri != null ? uri.toString() : "";
    }
}
