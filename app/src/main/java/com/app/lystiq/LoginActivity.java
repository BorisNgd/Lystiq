package com.app.lystiq;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft.
 * <p>
 * This class is for User Sign in and Forgot Password.
 */

public class LoginActivity extends AppCompatActivity implements OnClickListener {

    /**
     * Declare Layout Elements
     **/
    EditText email, password;
    TextView login, register, forgetPassword;
    ImageView backbtn;
    Display display;
    RelativeLayout main;
    ProgressDialog dialog;

    /**
     * Declare Variables
     **/
    final String TAG = "LoginActivity";
    String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        login = (TextView) findViewById(R.id.login);
        register = (TextView) findViewById(R.id.register);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        forgetPassword = (TextView) findViewById(R.id.forgetpassword);
        main = (RelativeLayout) findViewById(R.id.main);

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        JoysaleApplication.setupUI(LoginActivity.this, main);

        login.setOnClickListener(this);
        register.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        forgetPassword.setOnClickListener(this);

        display = this.getWindowManager().getDefaultDisplay();

        dialog = new ProgressDialog(LoginActivity.this,R.style.AppCompatAlertDialogStyle);
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
     * For register push notification
     **/

    private void addDeviceId(final String userId) {
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
                            Log.v("FCMRegResponse",response);
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
                map.put(Constants.TAG_FCM_USERID, userId);
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

    /**
     * Dialog for forgot password
     **/

    private void forgotPassword() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.forget_password);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView title = (TextView) dialog.findViewById(R.id.alert_title);
        final EditText msg = (EditText) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.alert_cancel);

        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!(msg.getText().toString().matches(emailPattern))
                        || (msg.getText().toString().trim().length() == 0)) {
                    msg.setError(getString(R.string.please_verify_mail));
                } else {
                    dialog.dismiss();
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    resetUserPassword(msg.getText().toString());
                }
            }
        });

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                dialog.dismiss();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(LoginActivity.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(LoginActivity.this);
    }

    /**
     * Function for Forgot Password
     **/
    private void resetUserPassword(final String userEmail) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_FORGET_PASSWORD, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                JSONObject jonj;
                try {
                    Log.v(TAG,"resetpasswordRes="+result);
                    jonj = new JSONObject(result);
                    if (jonj.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        if (jonj.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Reset password link has been mailed to you")) {
                            JoysaleApplication.dialog(LoginActivity.this, getString(R.string.success), getString(R.string.reset_password_link_mailed));
                        } else if (jonj.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("User not verified yet, activate the account from the email")) {
                            JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.user_not_verified_activate_account));
                        } else {
                            JoysaleApplication.dialog(LoginActivity.this, getString(R.string.success), jonj.getString(Constants.TAG_MESSAGE));
                        }
                    } else {
                        if (jonj.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("User not found")) {
                            JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.user_not_registered_yet));
                        } else {
                            JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), jonj.getString(Constants.TAG_MESSAGE));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
                }
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
                map.put(Constants.TAG_EMAIL, userEmail);
                Log.v(TAG,"resetpasswordparams="+map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for Login User
     **/

    private void loginUser() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                try {
                    dialog.dismiss();
                    Log.v(TAG,"loginresponse="+result);
                    JSONObject jonj = new JSONObject(result);
                    if (jonj.getString(Constants.TAG_STATUS).equalsIgnoreCase(
                            "true")) {
                        GetSet.setLogged(true);
                        GetSet.setEmail(email.getText().toString());
                        GetSet.setPassword(password.getText().toString());
                        GetSet.setUserId(jonj.getString(Constants.TAG_USERID));
                        GetSet.setUserName(jonj.getString(Constants.TAG_USERNAME));
                        GetSet.setFullName(jonj.getString(Constants.TAG_FULL_NAME));
                        GetSet.setImageUrl(jonj.getString(Constants.TAG_PHOTO));
                        GetSet.setRating(DefensiveClass.optInt(jonj, Constants.TAG_RATING));
                        GetSet.setRatingUserCount(DefensiveClass.optInt(jonj, Constants.TAG_RATING_USER_COUNT));

                        Constants.editor.putBoolean(Constants.PREF_ISLOGGED, true);
                        Constants.editor.putString(Constants.TAG_USER_ID, GetSet.getUserId());
                        Constants.editor.putString(Constants.TAG_USERNAME, GetSet.getUserName());
                        Constants.editor.putString(Constants.TAG_EMAIL, GetSet.getEmail());
                        Constants.editor.putString(Constants.TAG_PASSWORD, GetSet.getPassword());
                        Constants.editor.putString(Constants.TAG_PHOTO, GetSet.getImageUrl());
                        Constants.editor.putString(Constants.TAG_FULL_NAME, GetSet.getFullName());
                        Constants.editor.putString(Constants.TAG_RATING, GetSet.getRating());
                        Constants.editor.putString(Constants.TAG_RATING_USER_COUNT, GetSet.getRatingUserCount());
                        Constants.editor.commit();

                        addDeviceId(jonj.getString(Constants.TAG_USERID));

                        finish();
                        Intent i = new Intent(LoginActivity.this, FragmentMainActivity.class);
                        startActivity(i);

                    } else if (jonj.getString(Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                        JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), jonj.getString(Constants.TAG_MESSAGE));
                    } else {
                        dialog.dismiss();
                        if (jonj.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Please activate your account by the email sent to you")) {
                            JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.please_activate_your_account));
                        } else if (jonj.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Your account has been blocked by admin")) {
                            JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.your_account_blocked_by_admin));
                        } else if (jonj.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Please enter correct email and password")) {
                            JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.please_enter_correct_email_and_password));
                        } else if (jonj.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("User not registered yet")) {
                            JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), getString(R.string.user_not_registered_yet));
                        } else {
                            JoysaleApplication.dialog(LoginActivity.this, getString(R.string.alert), jonj.getString(Constants.TAG_MESSAGE));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), e.getMessage());
                }
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
                map.put(Constants.TAG_EMAIL, email.getText().toString());
                map.put(Constants.TAG_PASSWORD, password.getText().toString());
                Log.v(TAG,"loginparams="+map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for Onclick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                if (!JoysaleApplication.isNetworkAvailable(this)) {
                    JoysaleApplication.dialog(LoginActivity.this, getString(R.string.error), getString(R.string.network_error));
                } else if (email.getText().toString().trim().length() == 0) {
                    email.setError(getString(R.string.please_type_mail));
                } else if (!email.getText().toString().matches(emailPattern)) {
                    email.setError(getString(R.string.please_verify_mail));
                } else if (password.getText().toString().length() == 0) {
                    password.setError(getString(R.string.please_type_password));
                } else {
                    dialog.show();
                    loginUser();
                }
                break;
            case R.id.register:
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                break;
            case R.id.backbtn:
                finish();
                break;
            case R.id.forgetpassword:
                forgotPassword();
                break;
        }
    }

}