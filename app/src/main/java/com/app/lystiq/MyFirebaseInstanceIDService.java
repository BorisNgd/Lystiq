package com.app.lystiq;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.app.helper.SharedPrefManager;
import com.app.lystiq.JoysaleApplication;
import com.app.lystiq.R;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by hitasoft on 03/11/16.
 * <p>
 * This class is to Get a FCM Device Id From FCM Server.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.v(TAG, "Refreshed token: " + refreshedToken);
        storeToken(refreshedToken);
    }

    private void storeToken(String token) {
        //saving the token on shared preferences
        SharedPrefManager.getInstance(getApplicationContext()).saveDeviceToken(token);

        //get the logined user details from preference

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
        Constants.REGISTER_ID = token;
        if (Constants.pref.getBoolean(Constants.PREF_ISLOGGED, false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(
                    Constants.pref.getString(Constants.TAG_USER_ID, null));
            addDeviceId();
        }
    }

    private void addDeviceId() {
        Log.v(TAG,"addDeviceId");
        final String token = SharedPrefManager.getInstance(this).getDeviceToken();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PUSH_REGISTER,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "addDeviceIdRes=" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            Log.v(TAG,"FCMRegResponse="+response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                String[] languages = getResources().getStringArray(R.array.languages);
                String[] langCode = getResources().getStringArray(R.array.languageCode);
                String selectedLang = Constants.pref.getString("language", Constants.LANGUAGE);
                int index = Arrays.asList(languages).indexOf(selectedLang);
                final String languageCode = Arrays.asList(langCode).get(index);

                Log.v(TAG, "languageCode=" + languageCode);

                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);
                map.put(Constants.TAG_FCM_USERID, GetSet.getUserId());
                map.put(Constants.TAG_DEVICE_TYPE, "1");
                map.put(Constants.TAG_DEVICE_MODE, "1");
                map.put(Constants.LANG_TYPE, languageCode);
                map.put(Constants.TAG_DEVICE_TOKEN, token);
                Log.v(TAG,"reisterfcmparams" + map);
                return map;
            }
        };

        JoysaleApplication.getInstance().addToRequestQueue(req, TAG);
    }

}