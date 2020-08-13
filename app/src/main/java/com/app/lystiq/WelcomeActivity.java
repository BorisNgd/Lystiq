package com.app.lystiq;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryPerformance;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.app.helper.SharedPrefManager;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 24/5/16.
 * <p>
 * This class is for Welcome Screen after displays Splash Screen.It contains Social Login.
 */

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Declare Layout Elements
     **/
    TextView login, signup, skip, fbTxt, twtTxt, gplusTxt;
    ImageView fbBtn, twtBtn, gplusBtn;
    Display display;
    ProgressDialog dialog, mConnectionProgressDialog;

    /**
     * Declare Variables
     **/
    public static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn", PREF_KEY_OAUTH_TOKEN = "oauth_token", PREF_KEY_OAUTH_SECRET = "oauth_token_secret", TAG = "WelcomeActivity";
    private static final int RC_SIGN_IN = 9001;
    public static boolean fromSignout = false;
    boolean mSignInClicked;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    /**
     * Declare Social Login Elements
     **/
    CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcomelay);

        login = (TextView) findViewById(R.id.login);
        signup = (TextView) findViewById(R.id.signup);
        skip = (TextView) findViewById(R.id.skip);
        fbTxt = (TextView) findViewById(R.id.fbTxt);
        twtTxt = (TextView) findViewById(R.id.twtTxt);
        gplusTxt = (TextView) findViewById(R.id.gpTxt);
        fbBtn = (ImageView) findViewById(R.id.fbBtn);
        twtBtn = (ImageView) findViewById(R.id.twtBtn);
        gplusBtn = (ImageView) findViewById(R.id.gpBtn);

        login.setOnClickListener(this);
        signup.setOnClickListener(this);
        skip.setOnClickListener(this);
        fbTxt.setOnClickListener(this);
        twtTxt.setOnClickListener(this);
        gplusTxt.setOnClickListener(this);
        fbBtn.setOnClickListener(this);
        twtBtn.setOnClickListener(this);
        gplusBtn.setOnClickListener(this);

        //To init Facebook login
        loginToFacebook();

        // To init GPlus function
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mConnectionProgressDialog = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        mConnectionProgressDialog.setMessage("Signing in...");
        mConnectionProgressDialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            mConnectionProgressDialog.setIndeterminateDrawable(drawable);
        }

        display = this.getWindowManager().getDefaultDisplay();

        //To initialize dialog
        dialog = new ProgressDialog(WelcomeActivity.this,R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }
        new FlurryAgent.Builder()
                .withDataSaleOptOut(false) //CCPA - the default value is false
                .withCaptureUncaughtExceptions(true)
                .withIncludeBackgroundSessionsInMetrics(true)
                .withLogLevel(Log.VERBOSE)
                .withPerformanceMetrics(FlurryPerformance.ALL)
                .build(this, "2NPD46F39M37C4T37SCT");

    }

    /**
     * Function for login using facebook
     */


    public void loginToFacebook() {

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject profile,
                                            GraphResponse response) {
                                        final HashMap<String, String> fbdata = new HashMap<String, String>();
                                        Log.v(TAG, "Fbobject" + profile);
                                        // Application code
                                        try {
                                            if (profile.has(Constants.TAG_EMAIL)) {
                                                fbdata.put(Constants.TAG_TYPE, "facebook");
                                                fbdata.put(Constants.TAG_EMAIL, profile.getString(Constants.TAG_EMAIL));
                                                fbdata.put(Constants.TAG_ID, profile.getString(Constants.TAG_ID));
                                                fbdata.put(Constants.TAG_FIRST_NAME, profile.getString(Constants.TAG_FIRST_NAME));
                                                fbdata.put(Constants.TAG_LAST_NAME, profile.getString(Constants.TAG_LAST_NAME));
                                                fbdata.put(Constants.TAG_IMAGE_URL, "http://graph.facebook.com/" + profile.getString("id") + "/picture?type=large");
                                                Log.v(TAG, "fbdata=" + fbdata);
                                                WelcomeActivity.this.runOnUiThread(new Runnable() {

                                                    @SuppressWarnings("unchecked")
                                                    @Override
                                                    public void run() {
                                                        if (dialog != null && dialog.isShowing()) {
                                                            dialog.dismiss();
                                                        }
                                                        try {
                                                            dialog.show();
                                                        } catch (WindowManager.BadTokenException e) {
                                                            e.printStackTrace();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        sentDetails(fbdata);
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(WelcomeActivity.this, "Please check your Facebook permissions", Toast.LENGTH_SHORT).show();
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,first_name,last_name");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(WelcomeActivity.this, "Facebook - Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(WelcomeActivity.this, "Facebook - " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        if (exception instanceof FacebookAuthorizationException) {
                            if (com.facebook.AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                });
    }

    /**
     * Function for send a social login user details to server
     **/

    private void sentDetails(final HashMap<String, String> datas) {
        final StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SOCIAL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                try {
                    dialog.dismiss();
                    JSONObject results = new JSONObject(result);
                    Log.v(TAG, "sentdetails=" + result);
                    if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        GetSet.setLogged(true);
                        GetSet.setEmail(DefensiveClass.optString(results, Constants.TAG_EMAIL));
                        GetSet.setPassword("");
                        GetSet.setUserId(results.getString(Constants.TAG_USERID));
                        GetSet.setUserName(results.getString(Constants.TAG_USERNAME));
                        GetSet.setFullName(DefensiveClass.optString(results, Constants.TAG_FULL_NAME));
                        GetSet.setImageUrl(results.getString(Constants.TAG_PHOTO));
                        GetSet.setRating(DefensiveClass.optInt(results, Constants.TAG_RATING));
                        GetSet.setRatingUserCount(DefensiveClass.optInt(results, Constants.TAG_RATING_USER_COUNT));

                        Constants.editor.putBoolean(Constants.PREF_ISLOGGED, true);
                        Constants.editor.putString(Constants.TAG_USER_ID, GetSet.getUserId());
                        Constants.editor.putString(Constants.TAG_USERNAME, GetSet.getUserName());
                        Constants.editor.putString(Constants.TAG_EMAIL, GetSet.getEmail());
                        Constants.editor.putString(Constants.TAG_PASSWORD, "");
                        Constants.editor.putString(Constants.TAG_PHOTO, GetSet.getImageUrl());
                        Constants.editor.putString(Constants.TAG_FULL_NAME, GetSet.getFullName());
                        Constants.editor.putString(Constants.TAG_RATING, GetSet.getRating());
                        Constants.editor.putString(Constants.TAG_RATING_USER_COUNT, GetSet.getRatingUserCount());
                        Constants.editor.putString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
                        Constants.editor.commit();

                        Constants.editor.putBoolean(Constants.TWITTER_ISLOGGED, true);
                        Constants.editor.putString(Constants.TWITTER_USERID, GetSet.getUserId());
                        Constants.editor.putString(Constants.TWITTER_USERNAME, GetSet.getUserName());
                        Constants.editor.putString(Constants.TWITTER_EMAIL, GetSet.getEmail());
                        Constants.editor.putString(Constants.TWITTER_PASSWORD, "");
                        Constants.editor.putString(Constants.TWITTER_PHOTO, GetSet.getImageUrl());
                        Constants.editor.putString(Constants.TWITTER_FULLNAME, GetSet.getFullName());
                        Constants.editor.putString(Constants.TWITTER_RATING, GetSet.getRating());
                        Constants.editor.putBoolean(PREF_KEY_TWITTER_LOGIN, false);
                        Constants.editor.commit();

                        addDeviceId();
                        finish();

                        Intent i = new Intent(WelcomeActivity.this, FragmentMainActivity.class);
                        startActivity(i);

                    } else if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("false")) {
                        if (results.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Account not found")) {
                            getEmailForTwitter(datas);
                        } else {

                            if (results.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Email Already Exist")) {
                                JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), getString(R.string.email_already_exists));
                            } else if (results.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Your account has been blocked by admin")) {
                                JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), getString(R.string.your_account_blocked_by_admin));
                            } else {
                                JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), results.getString(Constants.TAG_MESSAGE));
                            }

                            Constants.editor.putBoolean(PREF_KEY_TWITTER_LOGIN, false);
                            Constants.editor.commit();

                        }

                    } else if (results.getString(Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                        JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), results.getString(Constants.TAG_MESSAGE));
                        Constants.editor.putBoolean(PREF_KEY_TWITTER_LOGIN, false);
                        Constants.editor.commit();
                    } else {
                        dialog.dismiss();
                        String msg = results.getString(Constants.TAG_MESSAGE);
                        JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.error), e.getMessage());
                    Constants.editor.putBoolean(PREF_KEY_TWITTER_LOGIN, false);
                    Constants.editor.commit();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.error), e.getMessage());
                    Constants.editor.putBoolean(PREF_KEY_TWITTER_LOGIN, false);
                    Constants.editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.error), e.getMessage());
                    Constants.editor.putBoolean(PREF_KEY_TWITTER_LOGIN, false);
                    Constants.editor.commit();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));
            }
        })

        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_TYPE, datas.get(Constants.TAG_TYPE));
                map.put(Constants.TAG_ID, datas.get(Constants.TAG_ID));
                map.put(Constants.TAG_FIRST_NAME, datas.get(Constants.TAG_FIRST_NAME));
                map.put(Constants.TAG_LAST_NAME, datas.get(Constants.TAG_LAST_NAME));
                map.put(Constants.TAG_EMAIL, datas.get(Constants.TAG_EMAIL));
                map.put(Constants.TAG_IMAGE_URL, datas.get(Constants.TAG_IMAGE_URL));
                Log.v(TAG, "sentdetailsparams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for register push notification in FCM Server
     **/

    private void addDeviceId() {
        Log.v(TAG, "addDeviceId");
        final String token = SharedPrefManager.getInstance(this).getDeviceToken();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PUSH_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.v(TAG, "addDeviceIdRes=" + res);
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
                Log.v(TAG, "addDeviceIdParams" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req, TAG);
    }

    /**
     * Funtions for login using Twitter
     **/

    private void getEmailForTwitter(final HashMap<String, String> twitterData) {
        final Dialog dialog = new Dialog(this, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.forget_password);
        dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView title = (TextView) dialog.findViewById(R.id.alert_title);
        final EditText msg = (EditText) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);

        title.setText(getString(R.string.twitter));
        ok.setText(getString(R.string.ok));

        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if ((!msg.getText().toString().matches(emailPattern))
                        || (msg.getText().toString().trim().length() == 0)) {
                    msg.setError(getString(R.string.please_verify_mail));
                } else {
                    Constants.editor.putString(PREF_KEY_OAUTH_TOKEN, twitterData.get("oauth_token"));
                    Constants.editor.putString(PREF_KEY_OAUTH_SECRET, twitterData.get("oauth_secret"));
                    Constants.editor.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
                    Constants.editor.commit(); // save changes
                    twitterData.put(Constants.TAG_EMAIL, msg.getText().toString());
                    try {
                        dialog.show();
                    } catch (WindowManager.BadTokenException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sentDetails(twitterData);
                }
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

    }

    /**
     * Funtion for login using Google Sign In
     **/

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.v(TAG, "handleSignInResult:" + completedTask.isSuccessful());
        if (completedTask.isSuccessful()) {
            // Signed in successfully, show authenticated UI.
            try {
                GoogleSignInAccount acct = completedTask.getResult(ApiException.class);

                String personPhoto = "";

                if (acct.getPhotoUrl() == null) {
                    personPhoto = "";

                } else {
                    personPhoto = acct.getPhotoUrl().toString();
                }

                HashMap<String, String> gplusData = new HashMap<String, String>();

                gplusData.put(Constants.TAG_TYPE, "google");
                gplusData.put(Constants.TAG_EMAIL, acct.getEmail());
                gplusData.put(Constants.TAG_ID, acct.getId());
                gplusData.put(Constants.TAG_FIRST_NAME, acct.getDisplayName());
                gplusData.put(Constants.TAG_LAST_NAME, "");
                gplusData.put(Constants.TAG_IMAGE_URL, personPhoto);

                try {
                    dialog.show();
                } catch (WindowManager.BadTokenException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sentDetails(gplusData);

                Log.v(TAG, "personName" + acct.getDisplayName());
                Log.v(TAG, "personPhoto" + personPhoto);
                Log.v(TAG, "email" + acct.getEmail());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.v(TAG, "onConnectionFailed");
        if (mSignInClicked) {
            mConnectionProgressDialog.dismiss();
            // The user has already clicked 'sign-in' so we attempt to resolve all
            // errors until the user is signed in, or they cancel.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onactivity");
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(WelcomeActivity.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(WelcomeActivity.this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fromSignout) {
            fromSignout = false;
            finish();
            Intent y = new Intent(WelcomeActivity.this, FragmentMainActivity.class);
            startActivity(y);
        } else {
            finish();
        }
    }

    /**
     * Funtion for OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                break;
            case R.id.signup:
                Intent e = new Intent(WelcomeActivity.this, RegisterActivity.class);
                e.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(e);
                break;
            case R.id.skip:
                if (fromSignout) {
                    fromSignout = false;
                    finish();
                    Intent y = new Intent(WelcomeActivity.this, FragmentMainActivity.class);
                    startActivity(y);
                } else {
                    finish();
                }
                break;
            case R.id.fbBtn:
            case R.id.fbTxt:
                LoginManager.getInstance().logOut();
                LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this, Arrays.asList("public_profile", "email"));
                break;
            case R.id.twtBtn:
                break;
            case R.id.twtTxt:
                break;
            case R.id.gpBtn:
            case R.id.gpTxt:
                mSignInClicked = true;
                googleLogout();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
        }
    }

    // To logout from gplus account
    private void googleLogout() {
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });
        }
    }
}
