package com.app.lystiq;
/****************
 *
 * @author 'Hitasoft Technologies'
 *
 * Description:
 * This class is used for displaying splash screen
 *
 * Revision History:
 * Version 1.0 - Initial Version
 *
 *****************/

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.helper.NetworkReceiver;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SplashActivity extends Activity implements NetworkReceiver.ConnectivityReceiverListener {

    /**
     * Declare variables
     **/
    String TAG = "SplashActivity",languageCode,selectedLang;
    private static int SPLASH_TIME_OUT = 0;
    String[] languages, langCode;

    public static SharedPreferences pref;
    public static Editor editor;
    private NetworkReceiver networkReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.splash_screen);
        pref = getApplicationContext().getSharedPreferences("JoysalePref", MODE_PRIVATE);
        editor = pref.edit();

        languages = getResources().getStringArray(R.array.languages);
        langCode = getResources().getStringArray(R.array.languageCode);
        selectedLang= pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);

        int index = Arrays.asList(languages).indexOf(selectedLang);
        languageCode = Arrays.asList(langCode).get(index);
        Log.v(TAG, "languageCode=" + languageCode);

        setLocale(languageCode);

        JoysaleApplication.adminPref = getApplicationContext().getSharedPreferences("JoysaleAdminPref",
                MODE_PRIVATE);
        JoysaleApplication.adminEditor = JoysaleApplication.adminPref.edit();
        networkReceiver = new NetworkReceiver();
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        JoysaleApplication.getInstance().setConnectivityListener(this);

    }

    /**
     * Function for get the admin default datas
     **/

    private void getAdminDatas() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    Log.v(TAG, "Get Admin Response=" + String.valueOf(json));

                    if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        JSONObject result = json.optJSONObject(Constants.TAG_RESULT);

                        FragmentMainActivity.homeBanner = DefensiveClass.optString(result, Constants.TAG_BANNER);
                        JoysaleApplication.adminEditor.putString(Constants.PREF_DISTANCE_TYPE, DefensiveClass.optString(result, Constants.TAG_DISTANCE_TYPE));
                        JoysaleApplication.adminEditor.commit();
                        /*if (DefensiveClass.optString(result, Constants.TAG_BUYNOW).equalsIgnoreCase("enable")) {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_BUYNOW, true);
                            JoysaleApplication.adminEditor.commit();
                            Constants.BUYNOW = true;
                        } else {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_BUYNOW, false);
                            JoysaleApplication.adminEditor.commit();
                            Constants.BUYNOW = false;
                        }*/

                        if (DefensiveClass.optString(result, Constants.TAG_EXCHANGE).equalsIgnoreCase("enable")) {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EXCHANGE, true);
                            JoysaleApplication.adminEditor.commit();
                            Constants.EXCHANGE = true;
                        } else {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EXCHANGE, false);
                            JoysaleApplication.adminEditor.commit();
                            Constants.EXCHANGE = false;
                        }

                        if (DefensiveClass.optString(result, Constants.TAG_PROMOTION).equalsIgnoreCase("enable")) {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_PROMOTION, true);
                            JoysaleApplication.adminEditor.commit();
                            Constants.PROMOTION = true;
                        } else {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_PROMOTION, false);
                            JoysaleApplication.adminEditor.commit();
                            Constants.PROMOTION = false;
                        }

                        if (DefensiveClass.optString(result, Constants.TAG_GIVING_AWAY).equalsIgnoreCase("enable")) {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_GIVINGAWAY, true);
                            JoysaleApplication.adminEditor.commit();
                            Constants.GIVINGAWAY = true;
                        } else {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_GIVINGAWAY, false);
                            JoysaleApplication.adminEditor.commit();
                            Constants.GIVINGAWAY = false;
                        }

                        JSONArray bannerAry = result.optJSONArray(Constants.TAG_BANNER_DATA);
                        if (bannerAry != null) {
                            for (int i = 0; i < bannerAry.length(); i++) {
                                JSONObject temp = bannerAry.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(Constants.KEY_IMAGE, DefensiveClass.optString(temp, Constants.TAG_BANNER_IMAGE));
                                map.put(Constants.KEY_URL, DefensiveClass.optString(temp, Constants.TAG_BANNER_URL));
                                FragmentMainActivity.bannerAry.add(map);
                            }
                        }

                        JSONArray categoryAry = result.optJSONArray("category");
                        if (categoryAry != null) {
                            if (FragmentMainActivity.categoryAry != null) {
                                FragmentMainActivity.categoryAry.clear();
                            }
                            for (int i = 0; i < categoryAry.length(); i++) {
                                JSONObject temp = categoryAry.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(Constants.TAG_CATEGORYID, DefensiveClass.optString(temp, Constants.TAG_CATEGORYID));
                                map.put(Constants.TAG_CATEGORYNAME, DefensiveClass.optString(temp, Constants.TAG_CATEGORYNAME));
                                map.put(Constants.TAG_CATEGORYIMG, DefensiveClass.optString(temp, Constants.TAG_CATEGORYIMG).replace("resized/40", "resized/150"));
                                map.put(Constants.LANG_TYPE,AppUtils.getCurrentLanguageCode(SplashActivity.this));
                                FragmentMainActivity.categoryAry.add(map);
                            }
                        }

                        JSONArray locationAry = result.optJSONArray("locations");
                        if (categoryAry != null) {
                            if (FragmentMainActivity.locationAry != null) {
                                FragmentMainActivity.locationAry.clear();
                            }
                            for (int i = 0; i < locationAry.length(); i++) {
                                JSONObject temp = locationAry.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(Constants.TAG_LOCATION_ID, DefensiveClass.optString(temp, Constants.TAG_LOCATION_ID));
                                map.put(Constants.TAG_LOCATION_REGION, DefensiveClass.optString(temp, Constants.TAG_LOCATION_REGION));
                                map.put(Constants.TAG_LOCATION_CITY, DefensiveClass.optString(temp, Constants.TAG_LOCATION_CITY));
                                map.put(Constants.TAG_LOCATION_COUNTRY,DefensiveClass.optString(temp, Constants.TAG_LOCATION_COUNTRY));
                                map.put(Constants.TAG_LOCATION_LAT, DefensiveClass.optString(temp, Constants.TAG_LOCATION_LAT));
                                map.put(Constants.TAG_LOCATION_LON,DefensiveClass.optString(temp, Constants.TAG_LOCATION_LON));
                                FragmentMainActivity.locationAry.add(map);
                            }
                        }

                        JSONArray templateAry = result.optJSONArray(Constants.TAG_CHAT_TEMPLATE);
                        if (templateAry != null) {
                            for (int i = 0; i < templateAry.length(); i++) {
                                JSONObject temp = templateAry.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(Constants.NAME, DefensiveClass.optString(temp, Constants.NAME));
                                ChatActivity.templatMsgAry.add(map);
                            }
                        }


                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        Intent i = new Intent(SplashActivity.this,
                                FragmentMainActivity.class);
                        startActivity(i);
                        finish();

                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                }, SPLASH_TIME_OUT);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.LANG_TYPE, languageCode);
                Log.v(TAG, "Get Admin Params=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public void onBackPressed() {

    }

    /**
     * Funtion for Set a Locale from Language
     **/

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(isConnected) {
            getAdminDatas();
        } else {
            JoysaleApplication.networkError(SplashActivity.this);
        }
    }
}
