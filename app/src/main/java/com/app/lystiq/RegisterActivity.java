package com.app.lystiq;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft.
 * <p>
 * This class is for User Sign Up.
 */

public class RegisterActivity extends Activity implements OnClickListener {

    /**
     * Declare Layout Elements
     **/
    EditText email, password, userName, fullName, confirmpwd;
    TextView register, login, title;
    ImageView backbtn;
    RelativeLayout main;

    /**
     * Declare Variables
     **/
    static final String TAG = "RegisterActivity";
    String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    InputFilter filterWithoutSpace = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    InputFilter filterWithSpace = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        userName = (EditText) findViewById(R.id.userName);
        fullName = (EditText) findViewById(R.id.fullName);
        login = (TextView) findViewById(R.id.login);
        register = (TextView) findViewById(R.id.register);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        main = (RelativeLayout) findViewById(R.id.main);
        confirmpwd = (EditText) findViewById(R.id.confirmpwd);

        JoysaleApplication.setupUI(RegisterActivity.this, main);

        backbtn.setOnClickListener(this);
        login.setOnClickListener(this);
        register.setOnClickListener(this);

        email.addTextChangedListener(new MyTextWatcher(email));
        password.addTextChangedListener(new MyTextWatcher(password));
        userName.addTextChangedListener(new MyTextWatcher(userName));
        fullName.addTextChangedListener(new MyTextWatcher(fullName));

        fullName.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
        userName.setFilters(new InputFilter[]{JoysaleApplication.USERNAME_FILTER, new InputFilter.LengthFilter(30)});
//        userName.setFilters(new InputFilter[]{filterWithoutSpace, new InputFilter.LengthFilter(30)});
    }

    public void dialog(Context ctx, String title, final String content) {
        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        Display display = ((Activity) ctx).getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(title);
        alertMsg.setText(content);

        alertOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (content.equals(getString(R.string.directsignup_true_response))) {
                    RegisterActivity.this.finish();
                    Intent in = new Intent(RegisterActivity.this, LoginActivity.class);
                    in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(in);
                } else {
                    RegisterActivity.this.finish();
                }
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                RegisterActivity.this.finish();
            }
        });
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(RegisterActivity.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(RegisterActivity.this);
    }

    private void registerUser(final ProgressDialog dialog) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SIGNUP, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Log.v(TAG, "RegisterResponse=" + result);
                    JSONObject jonj = new JSONObject(result);
                    if (jonj.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        if (jonj.getString(Constants.TAG_MESSAGE).equals("account has been created, Amazing products are waiting for you, kindly login.")) {
                            dialog(RegisterActivity.this, getString(R.string.success), getString(R.string.directsignup_true_response));
                        } else {
                            dialog(RegisterActivity.this, getString(R.string.success), getString(R.string.signup_true_response));
                        }

                    } else {
                        email.setText("");
                        password.setText("");

                        if (jonj.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Sorry, unable to create user, please try again later")) {
                            JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.alert), getString(R.string.unable_to_create_user));
                        } else if (jonj.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Email already exists")) {
                            JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.alert), getString(R.string.email_already_exists));
                        } else if (jonj.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Username already exists")) {
                            JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.alert), getString(R.string.username_already_exists));
                        } else {
                            JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.alert), jonj.getString(Constants.TAG_MESSAGE));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.error), e.getMessage());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.error), e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.error), e.getMessage());
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
                map.put(Constants.TAG_USERNAME, userName.getText().toString().trim());
                map.put(Constants.TAG_FULL_NAME, fullName.getText().toString().trim());
                map.put(Constants.TAG_EMAIL, email.getText().toString().trim());
                map.put(Constants.TAG_PASSWORD, password.getText().toString().trim());
                Log.v(TAG, "RegisterParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    public static class MyTextWatcher implements TextWatcher {

        private EditText view;

        MyTextWatcher(EditText view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (view != null) {
                view.setError(null);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    /**
     * Function for OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                if (!JoysaleApplication.isNetworkAvailable(this)) {
                    JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.error), getString(R.string.network_error));
                } else if (fullName.getText().toString().trim().length() == 0) {
                    fullName.setError(getString(R.string.please_fill));
                } else if (userName.getText().toString().trim().length() == 0) {
                    userName.setError(getString(R.string.please_fill));
                } else if ((!email.getText().toString().matches(emailPattern))
                        || (email.getText().toString().trim().length() == 0)) {
                    email.setError(getString(R.string.please_verify_mail));
                } else if (password.getText().toString().trim().length() < 6) {
                    password.setError(getString(R.string.passwordshould));
                } else if (!(password.getText().toString().trim())
                        .equals(confirmpwd.getText().toString().trim())) {
                    password.setError(getString(R.string.passwordmismatched));
                } else {
                    ProgressDialog dialog = new ProgressDialog(RegisterActivity.this,R.style.AppCompatAlertDialogStyle);
                    dialog.setMessage(getString(R.string.pleasewait));
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                        Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
                        drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                                PorterDuff.Mode.SRC_IN);
                        dialog.setIndeterminateDrawable(drawable);
                    }
                    dialog.show();

                    //To call register Api
                    registerUser(dialog);

                }
                break;
            case R.id.backbtn:
                finish();
                break;
            case R.id.login:
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                break;
        }
    }
}
