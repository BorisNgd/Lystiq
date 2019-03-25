package com.app.lystiq;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.utils.Constants;
import com.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 21/6/16.
 * <p>
 * This class is for Change User Password
 */

public class ChangePassword extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    // Widget Declaration
    public static EditText oldpassword, newpassword, confirmpassword;
    TextView save, show, title;
    ImageView back;


    // Declare Variables
    static final String TAG = "ChangePassword";
    public static boolean ishow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);

        oldpassword = (EditText) findViewById(R.id.oldPassword);
        newpassword = (EditText) findViewById(R.id.newPassword);
        confirmpassword = (EditText) findViewById(R.id.confirmPassword);
        title = (TextView) findViewById(R.id.title);
        save = (TextView) findViewById(R.id.save);
        show = (TextView) findViewById(R.id.show);
        back = (ImageView) findViewById(R.id.backbtn);

        oldpassword.setSelection(0);
        newpassword.setSelection(0);
        confirmpassword.setSelection(0);

        back.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.changepassword));

        save.setOnClickListener(this);
        back.setOnClickListener(this);
        show.setOnClickListener(this);
        newpassword.addTextChangedListener(this);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (newpassword.getText().length() == 0) {
            show.setVisibility(View.GONE);
        } else {
            show.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    /**
     * Function for send new password to server
     **/

    private void changePassword(final String oldPassword,final String newPassword){
        StringRequest req = new StringRequest(Request.Method.POST,Constants.API_CHANGE_PASSWORD, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject json = new JSONObject(result);
                    if (json.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        GetSet.setPassword(newPassword);
                        Constants.editor.putString(Constants.TAG_PASSWORD, GetSet.getPassword());
                        Constants.editor.commit();
                        Toast.makeText(ChangePassword.this, getString(R.string.password_changed_successfully), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        if (json.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Old Password Incorrect")) {
                            JoysaleApplication.dialog(ChangePassword.this, getString(R.string.alert), getString(R.string.old_password_incorrect));
                        } else if (json.getString(Constants.TAG_MESSAGE).equalsIgnoreCase("Old Password and new password are same, Please enter different one!")) {
                            JoysaleApplication.dialog(ChangePassword.this, getString(R.string.alert), getString(R.string.old_password_and_new_password_are_same));
                        } else {
                            JoysaleApplication.dialog(ChangePassword.this, getString(R.string.alert), json.getString(Constants.TAG_MESSAGE));
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
                map.put(Constants.TAG_OLD_PASSWORD, oldPassword);
                map.put(Constants.TAG_NEW_PASSWORD, newPassword);
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
            case R.id.show:
                if (!ishow) {
                    newpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    newpassword.setSelection(newpassword.length());
                    ishow = true;
                    show.setText(getString(R.string.show));
                } else {
                    newpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    newpassword.setSelection(newpassword.length());
                    ishow = false;
                    show.setText(getString(R.string.hide));
                }
                break;
            case R.id.save:
                try {
                    if ((oldpassword.getText().toString().trim()).equals("")) {
                        oldpassword.setError(getString(R.string.please_fill));
                        oldpassword.requestFocus();
                    } else if (!oldpassword.getText().toString().equals(GetSet.getPassword())) {
                        oldpassword.setError(getString(R.string.wrongpassword));
                    } else if ((newpassword.getText().toString().trim()).equals("")) {
                        newpassword.setError(getString(R.string.please_fill));
                        newpassword.requestFocus();
                    } else if (newpassword.getText().length() < 6) {
                        newpassword.setError(getString(R.string.passwordshould));
                        newpassword.requestFocus();
                    } else if ((oldpassword.getText().toString().trim())
                            .equals(newpassword.getText().toString().trim())) {
                        newpassword.setError(getString(R.string.youroldandnew));
                        newpassword.requestFocus();
                    } else if (!(newpassword.getText().toString().trim())
                            .equals(confirmpassword.getText().toString().trim())) {
                        confirmpassword.setError(getString(R.string.passwordmismatched));
                        confirmpassword.requestFocus();
                    } else {
                        changePassword(oldpassword.getText().toString().trim(), newpassword.getText().toString().trim());
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}
