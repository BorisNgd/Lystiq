package com.app.lystiq;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.app.external.ImagePicker;
import com.app.helper.ImageCompression;
import com.app.helper.ImageStorage;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft on 11/6/16.
 * <p>
 * This class is for Edit User Profile
 */

public class EditProfile extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    /**
     * Declare Layout Elements
     **/
    public static ImageView logout, backBtn, userImage;
    LinearLayout logoutLay, parentLay, callLay;
    RelativeLayout changepassword, editphoto, languageLay, mobileLayout;
    ImageView mailverifiedIcon, mobilverifiedIcon, fbverifiedIcon, imagebtn, langbtn, passbtn;
    TextView title, mobilverified, mailverified, fbverified, linkfb, save, language, showphoneno, verify;
    EditText username, name, email;
    TextInputLayout passwrdLay;

    AVLoadingIndicatorView progress;
    Dialog dialog;
    SwitchCompat callSwitch;

    /**
     * Declare Variables
     **/
    final String TAG = "EditProfile";
    public static int APP_REQUEST_CODE = 99;
    int count;
    String fullname = "", facebookid = "", uploadedImage = "", viewUrl = "", confirmedPhone = "", showPhone = "";
    CallbackManager callbackManager;
    HashMap<String, String> profileMap = new HashMap<String, String>(), fbData = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        logout = (ImageView) findViewById(R.id.logout);
        editphoto = (RelativeLayout) findViewById(R.id.editphoto);
        changepassword = (RelativeLayout) findViewById(R.id.changePassword);
        logoutLay = (LinearLayout) findViewById(R.id.logoutLay);
        username = (EditText) findViewById(R.id.user_name);
        name = (EditText) findViewById(R.id.name);
        passwrdLay = (TextInputLayout) findViewById(R.id.passwordInput);
        email = (EditText) findViewById(R.id.emailid);
        userImage = (ImageView) findViewById(R.id.user_image);
        mobilverified = (TextView) findViewById(R.id.mobilverified);
        mailverified = (TextView) findViewById(R.id.mailverified);
        fbverified = (TextView) findViewById(R.id.fbverified);
        linkfb = (TextView) findViewById(R.id.linkfb);
        save = (TextView) findViewById(R.id.save);
        languageLay = (RelativeLayout) findViewById(R.id.languageLay);
        language = (TextView) findViewById(R.id.language);
        parentLay = (LinearLayout) findViewById(R.id.parentLay);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        showphoneno = (TextView) findViewById(R.id.phoneno);
        verify = (TextView) findViewById(R.id.verify);
        mailverifiedIcon = (ImageView) findViewById(R.id.mailverifiedIcon);
        mobilverifiedIcon = (ImageView) findViewById(R.id.mobilverifiedIcon);
        fbverifiedIcon = (ImageView) findViewById(R.id.fbverifiedIcon);
        callLay = (LinearLayout) findViewById(R.id.callLay);
        callSwitch = (SwitchCompat) findViewById(R.id.callSwitch);
        imagebtn = (ImageView) findViewById(R.id.imagebtn);
        langbtn = (ImageView) findViewById(R.id.langbtn);
        passbtn = (ImageView) findViewById(R.id.passbtn);
        mobileLayout = (RelativeLayout) findViewById(R.id.mobileLayout);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.edit_profile));
        passwrdLay.setHint(getResources().getString(R.string.changepassword).toUpperCase());

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        progress.setVisibility(View.VISIBLE);
        parentLay.setVisibility(View.GONE);
        save.setVisibility(View.GONE);

        getProfileInformation();

        loginToFacebook();

        backBtn.setOnClickListener(this);
        changepassword.setOnClickListener(this);
        editphoto.setOnClickListener(this);
        logoutLay.setOnClickListener(this);
        linkfb.setOnClickListener(this);
        save.setOnClickListener(this);
        verify.setOnClickListener(this);
        languageLay.setOnClickListener(this);
        mobileLayout.setOnClickListener(this);

        name.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(30)});

        if (JoysaleApplication.isRTL(EditProfile.this)) {
            imagebtn.setRotation(180);
            langbtn.setRotation(180);
            passbtn.setRotation(180);
            name.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            username.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            email.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            showphoneno.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            imagebtn.setRotation(0);
            langbtn.setRotation(0);
            passbtn.setRotation(0);
            name.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            username.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            email.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            showphoneno.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }

    }

    /**
     * set the information to elements
     **/

    private void setProfileInformation() {
        try {
            language.setText(Constants.pref.getString("language", "English"));
            if (profileMap.size() > 0) {
                username.setText(profileMap.get("user_name"));
                name.setText(profileMap.get(Constants.TAG_FULL_NAME));
                fullname = profileMap.get(Constants.TAG_FULL_NAME).toString();
                email.setText(profileMap.get(Constants.TAG_EMAIL));
               viewUrl = profileMap.get("user_img");
                Picasso.with(EditProfile.this).load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImage);
                //Picasso.with(EditProfile.this).load(viewUrl).placeholder(R.drawable.appicon);
                if (profileMap.get("facebook_ver").equals("true")) {
                    fbverified.setText(getString(R.string.verified));
                    fbverifiedIcon.setImageResource(R.drawable.tick_green);
                    linkfb.setEnabled(false);
                    linkfb.setVisibility(View.GONE);
                } else {
                    fbverified.setText(getString(R.string.unverified));
                    fbverifiedIcon.setImageResource(R.drawable.cancel);
                    fbverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                    linkfb.setVisibility(View.VISIBLE);
                    linkfb.setEnabled(true);
                }
                if (profileMap.get("mob_ver").equals("true")) {
                    verify.setText(getString(R.string.change));
                    mobilverified.setText(getString(R.string.verified));
                    mobilverifiedIcon.setImageResource(R.drawable.tick_green);
                    showphoneno.setVisibility(View.VISIBLE);
                    showphoneno.setText(profileMap.get("mobile_no"));
                    mobilverified.setEnabled(true);
                    mobilverifiedIcon.setImageResource(R.drawable.tick_green);
                    callLay.setVisibility(View.VISIBLE);
                } else {
                    verify.setText(getString(R.string.verify));
                    mobilverified.setText(getString(R.string.unverified));
                    mobilverifiedIcon.setImageResource(R.drawable.cancel);
                    mobilverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                    mobilverified.setEnabled(false);
                    callLay.setVisibility(View.GONE);
                }
                if (profileMap.get("email_ver").equals("true")) {
                    mailverified.setText(getString(R.string.verified));
                    mailverifiedIcon.setImageResource(R.drawable.tick_green);
                } else {
                    mailverified.setText(getString(R.string.unverified));
                    mailverifiedIcon.setImageResource(R.drawable.cancel);
                    mailverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                }

                if (profileMap.get("show_mobile_no").equals("true")) {
                    callSwitch.setChecked(true);
                } else {
                    callSwitch.setChecked(false);
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * dialog for confirm the user to signout
     **/

    public void signoutdialog() {
        final Dialog dialog = new Dialog(EditProfile.this, R.style.AlertDialog);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);
        TextView alertCancel = (TextView) dialog.findViewById(R.id.cancel_button);

        alertMsg.setText(getString(R.string.reallySignOut));
        alertOk.setText(getString(R.string.yes));
        alertCancel.setText(getString(R.string.no));

        alertCancel.setVisibility(View.VISIBLE);

        alertOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*JoysaleApplication aController = new JoysaleApplication();
                aController.unregister(EditProfile.this);*/
                userSignout();
                String selectedLang = Constants.pref.getString("language", Constants.LANGUAGE);
                Constants.editor.clear();
                Constants.editor.putString("language", selectedLang);
                Constants.editor.commit();
                GetSet.reset();
                FragmentMainActivity.homeItemList.clear();
                if (FragmentMainActivity.itemAdapter != null) {
                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                }
                FragmentMainActivity.currentPage = 0;
                MessageActivity.messagepageitems.clear();
                WelcomeActivity.fromSignout = true;
                finish();
                Intent p = new Intent(EditProfile.this, WelcomeActivity.class);
                startActivity(p);
            }
        });

        alertCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (dialog!=null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * Function for User Sign out
     **/

    private void userSignout() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PUSH_UNREGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                Log.v(TAG, "sendchatjson=" + res);
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
                map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);
                Log.v(TAG, "signoutParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mobilverified.setText(getString(R.string.change));
        mobilverified.setEnabled(true);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == APP_REQUEST_CODE) {
            AccessToken accessToken = AccountKit.getCurrentAccessToken();
            if (accessToken != null) {
                //Handle Returning User
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        PhoneNumber phoneNumber = account.getPhoneNumber();
                        if (phoneNumber != null) {
                            confirmedPhone = phoneNumber.toString();
                            mobilverified.setText(getString(R.string.verified));
                            mobilverifiedIcon.setImageResource(R.drawable.tick_green);
                            showphoneno.setVisibility(View.VISIBLE);
                            showphoneno.setText(confirmedPhone);
                            profileMap.put("mobile_no", confirmedPhone);
                            verify.setText(getResources().getString(R.string.change));
                            if (!TextUtils.isEmpty(fullname)) {
                                editUserProfile("otherdetails");
                            } else {
                                Toast.makeText(EditProfile.this, getString(R.string.please_enter_name), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            mobilverified.setText(getString(R.string.unverified));
                            mobilverifiedIcon.setImageResource(R.drawable.cancel);
                            mobilverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                        }
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });
            }

        } else if (resultCode == -1 && requestCode == 234) {
            Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
            ImageStorage imageStorage = new ImageStorage(EditProfile.this, this);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
            String imageStatus = imageStorage.saveToSdCard(bitmap, "profile", timestamp + ".jpg", timestamp);

            if (imageStatus.equals("success")) {
                File file = imageStorage.getImage("profile", timestamp + ".jpg");
                String filepath = file.getAbsolutePath();
                Log.i(TAG, "selectedImageFile: " + filepath);
                ImageCompression imageCompression = new ImageCompression(EditProfile.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        new UploadProfileImage().execute(imagePath);
                    }
                };
                imageCompression.execute(filepath);
            } else {
                Toast.makeText(EditProfile.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }


    /**
     * for fb confirmation
     **/
    public void loginToFacebook() {
        Log.v(TAG, "loginToFacebook");
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.v("loginToFacebook", "onSuccess");
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject profile,
                                                            GraphResponse response) {
                                        Log.v("json", "object" + profile);
                                        // Application code
                                        try {
                                            String email = "";
                                            if (profile.has(Constants.TAG_EMAIL)) {
                                                email = profile.getString(Constants.TAG_EMAIL);
                                            } else {
                                                email = "";
                                            }

                                            fbData.put("id", profile.getString("id"));
                                            fbData.put(Constants.TAG_EMAIL, email);
                                            fbData.put("first_name", profile.getString("first_name"));
                                            fbData.put("last_name", profile.getString("last_name"));
                                            fbData.put("profile_url", "https://www.facebook.com/app_scoped_user_id/" + profile.getString("id") + "/");

                                            facebookid = profile.getString("id");
                                            EditProfile.this.runOnUiThread(new Runnable() {

                                                @SuppressWarnings("unchecked")
                                                @Override
                                                public void run() {
                                                    if (dialog != null && dialog.isShowing()) {
                                                        dialog.dismiss();
                                                    }
                                                    editUserProfile("facebook");
                                                    //new EditProfile.Editprofile("facebook").execute();

                                                }
                                            });
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
                        Toast.makeText(EditProfile.this, "Facebook - Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.v(TAG, "loginToFacebook-onError=" + exception);
                        Toast.makeText(EditProfile.this, "Facebook - " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        if (exception instanceof FacebookAuthorizationException) {
                            if (com.facebook.AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(EditProfile.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(EditProfile.this);
        if (EditProfilePhoto.editPhoto) {
            EditProfilePhoto.editPhoto = false;
            new UploadProfileImage().execute(EditProfilePhoto.imgPath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("requestCode", "requestCode=" + requestCode);
        if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ImagePicker.pickImage(this, "Select your image:");
        }
    }

    /**
     * Function for get profile information of user
     **/

    private void getProfileInformation() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PROFILE, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                try {
                    JSONObject obj = new JSONObject(json);
                    String response = DefensiveClass.optString(obj, Constants.TAG_STATUS);
                    Log.v(TAG, "getProfileInformationRes=" + json);
                    if (response.equalsIgnoreCase("true")) {
                        JSONObject result = obj.optJSONObject("result");
                        if (!(result == null)) {
                            profileMap.put(Constants.TAG_USERID, DefensiveClass.optString(result, Constants.TAG_USERID));
                            profileMap.put(Constants.TAG_USERNAME, DefensiveClass.optString(result, Constants.TAG_USERNAME));
                            profileMap.put(Constants.TAG_FULL_NAME, DefensiveClass.optString(result, Constants.TAG_FULL_NAME));
                            profileMap.put(Constants.TAG_USERIMG, DefensiveClass.optString(result, Constants.TAG_USERIMG));
                            profileMap.put(Constants.TAG_EMAIL, DefensiveClass.optString(result, Constants.TAG_EMAIL));
                            profileMap.put(Constants.TAG_FACEBOOK_ID, DefensiveClass.optString(result, Constants.TAG_FACEBOOK_ID));
                            profileMap.put(Constants.TAG_MOBILE_NO, DefensiveClass.optString(result, Constants.TAG_MOBILE_NO));

                            JSONObject verification = result.optJSONObject(Constants.TAG_VERIFICATION);
                            profileMap.put(Constants.TAG_FB_VER, DefensiveClass.optString(verification, Constants.TAG_FACEBOOK));
                            profileMap.put(Constants.TAG_EMAIL_VER, DefensiveClass.optString(verification, Constants.TAG_EMAIL));
                            profileMap.put(Constants.TAG_MOB_VER, DefensiveClass.optString(verification, Constants.TAG_MOB_NO));
                            profileMap.put(Constants.TAG_SHOW_MOBILE_NO, DefensiveClass.optString(result, Constants.TAG_SHOW_MOBILE_NO));

                            Log.v(TAG, "userimage=" + DefensiveClass.optString(result, "user_img"));
                        }
                        Log.v(TAG, "profileMap=" + profileMap);
                    }
                    progress.setVisibility(View.GONE);
                    if (profileMap.size() == 0) {
                        Toast.makeText(EditProfile.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    } else {
                        save.setVisibility(View.VISIBLE);
                        parentLay.setVisibility(View.VISIBLE);
                        setProfileInformation();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
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
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                Log.v(TAG, "getProfileInformationParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * save the edited details to server
     **/

    private void editUserProfile(final String from) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_EDIT_PROFILE, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                Log.v(TAG, "editUserProfileRes=" + res);
                try {
                    JSONObject json = new JSONObject(res);
                    String status = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        if (!from.equals("facebook"))
                            Toast.makeText(EditProfile.this, getString(R.string.your_changes_saved), Toast.LENGTH_SHORT).show();
                        Profile.userName.setText(fullname);
                        Profile.userName2.setText(fullname);
                        Picasso.with(EditProfile.this).load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(Profile.mHeaderLogo);
                        Picasso.with(EditProfile.this).load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(Profile.userImg);
                        Profile.profileMap.put(Constants.TAG_FULL_NAME, fullname);
                        Profile.profileMap.put(Constants.TAG_USERIMG, viewUrl);
                        GetSet.setFullName(fullname);
                        GetSet.setImageUrl(viewUrl);
                        FragmentMainActivity.username.setText(GetSet.getFullName());
                        Picasso.with(EditProfile.this).load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(FragmentMainActivity.userImage);
                        Constants.editor.putString(Constants.TAG_PHOTO, viewUrl);
                        Constants.editor.putString(Constants.TAG_FULL_NAME, fullname);
                        Constants.editor.commit();

                        JSONObject result = json.optJSONObject(Constants.TAG_RESULT);
                        JSONObject verification = result.optJSONObject(Constants.TAG_VERIFICATION);
                        Profile.profileMap.put(Constants.TAG_FB_VER, DefensiveClass.optString(verification, Constants.TAG_FACEBOOK));
                        Profile.profileMap.put(Constants.TAG_EMAIL_VER, DefensiveClass.optString(verification, Constants.TAG_EMAIL));
                        Profile.profileMap.put(Constants.TAG_MOB_VER, DefensiveClass.optString(verification, Constants.TAG_MOB_NO));
                        if (DefensiveClass.optString(verification, Constants.TAG_FACEBOOK).equals("true")) {
                            Profile.fbVerify.setImageResource(R.drawable.fb_veri);
                        } else {
                            Profile.fbVerify.setImageResource(R.drawable.fb_unveri);
                        }
                        if (DefensiveClass.optString(verification, Constants.TAG_EMAIL).equals("true")) {
                            Profile.mailVerify.setImageResource(R.drawable.mail_veri);
                        } else {
                            Profile.mailVerify.setImageResource(R.drawable.mail_unveri);
                        }
                        if (DefensiveClass.optString(verification, Constants.TAG_MOB_NO).equals("true")) {
                            Profile.mobVerify.setImageResource(R.drawable.mob_veri);
                        } else {
                            Profile.mobVerify.setImageResource(R.drawable.mob_unveri);
                        }
                        if (!from.equals("facebook")) {
                            finish();
                        } else {
                            fbverified.setText(getString(R.string.verified));
                            fbverifiedIcon.setImageResource(R.drawable.tick_green);
                            linkfb.setEnabled(false);
                            linkfb.setVisibility(View.GONE);
                        }
                    }
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
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_FULL_NAME, fullname);
                map.put(Constants.TAG_USERIMG, uploadedImage);
                map.put(Constants.TAG_FACEBOOK_ID, facebookid);
                map.put(Constants.TAG_MOBILE_NO, confirmedPhone);
                map.put(Constants.TAG_SHOW_MOBILE_NO, showPhone);
                if (fbData.size() > 0) {
                    map.put(Constants.TAG_FB_EMAIL, fbData.get(Constants.TAG_EMAIL));
                    map.put(Constants.TAG_FB_FIRSTNAME, fbData.get(Constants.TAG_FIRST_NAME));
                    map.put(Constants.TAG_FB_LASTNAME, fbData.get(Constants.TAG_LAST_NAME));
                    map.put(Constants.TAG_FB_PHONE, "");
                    map.put(Constants.TAG_FB_PROFILEURL, fbData.get(Constants.TAG_PROFILE_URL));
                }
                Log.v(TAG, "editUserProfileParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     *Function to Verify Given Number using Account Kit
     **/

    public void verifyMobileNo(View v) {
        final Intent intent = new Intent(EditProfile.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN);
        configurationBuilder.setReadPhoneStateEnabled(true);
        configurationBuilder.setReceiveSMS(true);
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    /**
     * class for upload user image
     **/

    class UploadProfileImage extends AsyncTask<String, Integer, Integer> {
        JSONObject jsonobject = null;
        String Json = "";
        ProgressDialog pd;

        @Override
        protected Integer doInBackground(String... imgpath) {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            StringBuilder builder = new StringBuilder();
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****", status;
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            String urlString = Constants.API_UPLOAD_IMAGE;
            try {
                String exsistingFileName = imgpath[0];
                Log.v(" exsistingFileName", exsistingFileName);
                FileInputStream fileInputStream = new FileInputStream(new File(exsistingFileName));
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data;name=\"type\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("user");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + exsistingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                Log.e("MediaPlayer", "Headers are written");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                Log.v("buffer", "buffer" + buffer);

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    Log.v("bytesRead", "bytesRead" + bytesRead);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                Log.v("in", "" + in);
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);

                Log.e("MediaPlayer", "File is written");
                fileInputStream.close();
                Json = builder.toString();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
            }
            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;
                while ((str = inStream.readLine()) != null) {
                    Log.e("MediaPlayer", "Server Response" + str);
                }
                inStream.close();
            } catch (IOException ioex) {
                Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
            }
            try {
                jsonobject = new JSONObject(Json);
                Log.v("json", "json" + Json);
                status = jsonobject.getString(Constants.TAG_STATUS);
                if (status.equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    String msg = image.getString("Message");
                    uploadedImage = image.getString("Name");
//                    viewUrl = Constants.url + "user/resized/150/" + uploadedImage;
                    viewUrl = image.optString("View_url","");
                }

            } catch (JSONException e) {
                status = "false";
                e.printStackTrace();
            } catch (NullPointerException e) {
                status = "false";
                e.printStackTrace();
            } catch (Exception e) {
                status = "false";
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(EditProfile.this,R.style.AppCompatAlertDialogStyle);
            pd.setMessage(getString(R.string.loading));
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Drawable drawable = new ProgressBar(EditProfile.this).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(EditProfile.this, R.color.progressColor),
                        PorterDuff.Mode.SRC_IN);
                pd.setIndeterminateDrawable(drawable);
            }
            pd.show();
        }

        @Override
        protected void onPostExecute(Integer unused) {
            Log.v(TAG, "imageupload=" + uploadedImage);
            Picasso.with(EditProfile.this).load(viewUrl).into(userImage);
            pd.dismiss();
        }
    }

    /**
     * Onclick Event
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                fullname = name.getText().toString();
                if (callSwitch.isChecked()) {
                    showPhone = "true";
                } else {
                    showPhone = "false";
                }
                if (!TextUtils.isEmpty(fullname)) {
                    editUserProfile("otherdetails");
                } else {
                    Toast.makeText(this, getString(R.string.please_enter_name), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.editphoto:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    ImagePicker.pickImage(this, "Select your image:");
                }
                break;
            case R.id.changePassword:
                Intent j = new Intent(EditProfile.this, ChangePassword.class);
                startActivity(j);
                break;
            case R.id.logoutLay:
                signoutdialog();
                break;
            case R.id.backbtn:
                this.finish();
                break;
            case R.id.verify:
            case R.id.mobileLayout:
                verifyMobileNo(v);
                break;
            case R.id.linkfb:
                LoginManager.getInstance().logInWithReadPermissions(EditProfile.this, Arrays.asList("public_profile", "email"));
                break;
            case R.id.languageLay:
                Intent i = new Intent(EditProfile.this, Language.class);
                startActivity(i);
                break;
        }
    }

}