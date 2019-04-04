package com.app.lystiq;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.app.helper.SharedPrefManager;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by hitasoft on 21/7/16.
 * <p>
 * This class is for Select a App Language.
 */

public class Language extends AppCompatActivity implements View.OnClickListener {
    final String TAG = "Language";
    /**
     * Declare Layout Elements
     **/
    ListView listView;
    TextView categoryName, title;
    ImageView backbtn;
    ProgressDialog dialog;

    /**
     * Declare Variables
     **/
    String[] languages, langCode;
    String selectedLang = Constants.LANGUAGE;
    LanguageAdapter languageAdapter;
    public static String langCodeGlobal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sub_category);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        listView = (ListView) findViewById(R.id.listView);
        categoryName = (TextView) findViewById(R.id.categoryName);
        title = (TextView) findViewById(R.id.title);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        categoryName.setVisibility(View.GONE);

        title.setText(getString(R.string.language));

        listView.setDivider(null);
        listView.setDividerHeight(JoysaleApplication.dpToPx(this, 10));

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        backbtn.setOnClickListener(this);

        selectedLang = Constants.pref.getString("language", Constants.LANGUAGE);

        languages = getResources().getStringArray(R.array.languages);
        langCode = getResources().getStringArray(R.array.languageCode);
        languageAdapter = new LanguageAdapter(Language.this, languages);
        listView.setAdapter(languageAdapter);
        dialog = new ProgressDialog(Language.this,R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }
    }

    /**
     * function for change the selected language
     **/

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        dialog.show();
        getAdminDatas(lang);
    }

    /**
     * For register push notification
     **/

    private void addDeviceId() {
        Log.v(TAG, "addDeviceId");
        final String token = SharedPrefManager.getInstance(this).getDeviceToken();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PUSH_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "addDeviceIdres=" + res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                            Log.v("FCMRegResponse", response);
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
                VolleyLog.d(TAG, "Language-Error: " + error.getMessage());
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
                Log.v(TAG, "reisterfcmparams" + map);
                return map;
            }
        };

        JoysaleApplication.getInstance().addToRequestQueue(req, "Language");
    }

    /**
     * Function for get the admin default datas
     **/

    private void getAdminDatas(final String languageCode) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    Log.d(TAG, "adminDatasRes=" + String.valueOf(json));
                    if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        JSONObject result = json.optJSONObject(Constants.TAG_RESULT);
                        FragmentMainActivity.homeBanner = DefensiveClass.optString(result, Constants.TAG_BANNER);
                        JoysaleApplication.adminEditor.putString(Constants.PREF_DISTANCE_TYPE, DefensiveClass.optString(result, Constants.TAG_DISTANCE_TYPE));
                        JoysaleApplication.adminEditor.commit();
                        if (DefensiveClass.optString(result, Constants.TAG_BUYNOW).equalsIgnoreCase("enable")) {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_BUYNOW, true);
                            JoysaleApplication.adminEditor.commit();
                            Constants.BUYNOW = true;
                        } else {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_BUYNOW, false);
                            JoysaleApplication.adminEditor.commit();
                            Constants.BUYNOW = false;
                        }

                        if (DefensiveClass.optString(result, Constants.TAG_EXCHANGE).equalsIgnoreCase("enable")) {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EXCHANGE, true);
                            JoysaleApplication.adminEditor.commit();
                            Constants.EXCHANGE = true;
                        } else {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EXCHANGE, false);
                            JoysaleApplication.adminEditor.commit();
                            Constants.EXCHANGE = false;
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

                        if (DefensiveClass.optString(result, Constants.TAG_PROMOTION).equalsIgnoreCase("enable")) {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_PROMOTION, true);
                            JoysaleApplication.adminEditor.commit();
                            Constants.PROMOTION = true;
                        } else {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_PROMOTION, false);
                            JoysaleApplication.adminEditor.commit();
                            Constants.PROMOTION = false;
                        }

                        JSONArray bannerAry = result.getJSONArray(Constants.TAG_BANNER_DATA);
                        FragmentMainActivity.bannerAry.clear();
                        for (int i = 0; i < bannerAry.length(); i++) {
                            JSONObject temp = bannerAry.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<String, String>();

                            map.put(Constants.KEY_IMAGE, DefensiveClass.optString(temp, Constants.TAG_BANNER_IMAGE));
                            map.put(Constants.KEY_URL, DefensiveClass.optString(temp, Constants.TAG_BANNER_URL));

                            FragmentMainActivity.bannerAry.add(map);
                        }
                        JSONArray categoryAry = result.getJSONArray("category");
                        FragmentMainActivity.categoryAry.clear();
                        for (int i = 0; i < categoryAry.length(); i++) {
                            JSONObject temp = categoryAry.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<String, String>();

                            map.put(Constants.TAG_CATEGORYID, DefensiveClass.optString(temp, "category_id"));
                            map.put(Constants.TAG_CATEGORYNAME, DefensiveClass.optString(temp, Constants.TAG_CATEGORYNAME));
                            map.put(Constants.TAG_CATEGORYIMG, DefensiveClass.optString(temp, Constants.TAG_CATEGORYIMG));
                            FragmentMainActivity.categoryAry.add(map);
                        }
                        JSONArray templateAry = result.optJSONArray(Constants.TAG_CHAT_TEMPLATE);
                        ChatActivity.templatMsgAry.clear();
                        for (int i = 0; i < templateAry.length(); i++) {
                            JSONObject temp = templateAry.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<String, String>();

                            map.put(Constants.NAME, DefensiveClass.optString(temp, Constants.NAME));
                            ChatActivity.templatMsgAry.add(map);
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                FragmentMainActivity.filterAry.clear();
                SearchAdvance.applyFilter = false;
                SearchAdvance.distance = "0";
                SearchAdvance.categoryId.clear();
                SearchAdvance.categoryName.clear();
                SearchAdvance.subcategoryId.clear();
                SearchAdvance.postedWithin = "";
                SearchAdvance.sortBy = "1";
                SearchActivity.searchQuery = "";
                Intent refresh = new Intent(Language.this, FragmentMainActivity.class);
                startActivity(refresh);
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
                Log.v(TAG, "adminDatasParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
        }
    }

    /**
     * Adapter for set the language to list
     **/

    public class LanguageAdapter extends BaseAdapter {

        String[] lang;
        ViewHolder holder = null;
        private Context mContext;

        public LanguageAdapter(Context ctx, String[] data) {
            mContext = ctx;
            lang = data;
        }

        @Override
        public int getCount() {
            return lang.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.language_list_items, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.tick = (ImageView) convertView.findViewById(R.id.tick);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                if (JoysaleApplication.isRTL(mContext)) {
                    holder.name.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                } else {
                    holder.name.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                }

                holder.name.setText(lang[position]);
                if (lang[position].equals(selectedLang)) {
                    holder.tick.setVisibility(View.VISIBLE);
                    holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.tick.setVisibility(View.INVISIBLE);
                    holder.name.setTextColor(getResources().getColor(R.color.primaryText));
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedLang = lang[position];
                        languageAdapter.notifyDataSetChanged();

                        Constants.editor.putString("language", selectedLang);
                        Constants.editor.commit();

                        addDeviceId();
                        langCodeGlobal = langCode[position];
                        setLocale(langCode[position]);
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            ImageView tick;
            TextView name;
            RelativeLayout mainLay;
        }
    }
}
