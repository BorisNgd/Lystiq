package com.app.lystiq;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 9/7/16.
 * <p>
 * This class is for User Profile.
 */

public class Profile extends AppCompatActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    public static ImageView userImg, mHeaderLogo, fbVerify, mailVerify, mobVerify, followStatus;
    public static TextView userName, location, userName2, location2, ratingCount;
    CoordinatorLayout main;
    AppBarLayout appbar;
    LinearLayout verificationLay, statusLay;
    RelativeLayout userLay, reviewLay;
    Display display;
    RatingBar ratingBar;
    Toolbar toolbar;
    ImageView backbtn, settingbtn, optionbtn;
    CollapsingToolbarLayout collapsingToolbar;
    TabPagerAdapter tabPagerAdapter;
    ViewPager mViewPager;
    TabLayout mTabLayout;

    public static HashMap<String, String> profileMap = new HashMap<String, String>();
    public static ArrayList<String> followingId = new ArrayList<String>();

    /**
     * Declare Variables
     **/
    static final String TAG = "Promotead";
    int headerPosition;
    String userId = "", userImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_main_layout);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        settingbtn = (ImageView) findViewById(R.id.settingbtn);
        optionbtn = (ImageView) findViewById(R.id.optionbtn);
        userImg = (ImageView) findViewById(R.id.userImg);
        mHeaderLogo = (ImageView) findViewById(R.id.header_logo);
        userLay = (RelativeLayout) findViewById(R.id.userLay);
        userName = (TextView) findViewById(R.id.userName);
        location = (TextView) findViewById(R.id.location);
        userName2 = (TextView) findViewById(R.id.userName2);
        location2 = (TextView) findViewById(R.id.location2);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        followStatus = (ImageView) findViewById(R.id.followStatus);
        fbVerify = (ImageView) findViewById(R.id.fbverify);
        mailVerify = (ImageView) findViewById(R.id.mailverify);
        mobVerify = (ImageView) findViewById(R.id.mblverify);
        verificationLay = (LinearLayout) findViewById(R.id.verificationLay);
        main = (CoordinatorLayout) findViewById(R.id.main_content);
        reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingCount = (TextView) findViewById(R.id.ratingCount);
        statusLay = (LinearLayout) findViewById(R.id.statusLay);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.detail_tabs);

        setToolbar();

        userId = (String) getIntent().getExtras().get(Constants.TAG_USER_ID);

        //To set Adapter in View pager
        setTabPageAdapter();

        display = this.getWindowManager().getDefaultDisplay();

        followingId.clear();

        //To Getfollowerid from Api
        getFollowingId();

        verificationLay.setVisibility(View.INVISIBLE);

        if (userId.equals(GetSet.getUserId())) {
            settingbtn.setVisibility(View.VISIBLE);
            optionbtn.setVisibility(View.VISIBLE);
            statusLay.setVisibility(View.GONE);
            if (!Constants.BUYNOW && !Constants.EXCHANGE && !Constants.PROMOTION) {
                optionbtn.setVisibility(View.GONE);
            }
        } else {
            settingbtn.setVisibility(View.GONE);
            optionbtn.setVisibility(View.GONE);
            statusLay.setVisibility(View.GONE);
        }

        backbtn.setOnClickListener(this);
        optionbtn.setOnClickListener(this);
        settingbtn.setOnClickListener(this);
        statusLay.setOnClickListener(this);
        ratingBar.setOnClickListener(this);

        profileMap.clear();

        //To get ProfileInformation from Api
        getProfileInformation();

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);

        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float offset = JoysaleApplication.pxToDp(Profile.this, verticalOffset);
                if (offset > -25) {
                    //opened
                    if (headerPosition != 0 && mHeaderLogo.getVisibility() != View.VISIBLE) {
                        Log.i(TAG, "opened");

                        userLay.setVisibility(View.GONE);
                        userImg.setVisibility(View.GONE);
                        userName2.setVisibility(View.GONE);
                        location2.setVisibility(View.GONE);
                        mHeaderLogo.setVisibility(View.VISIBLE);

                        userLay.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkout));
                        mHeaderLogo.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkin));

                        userName2.setTextColor(getResources().getColor(R.color.white));
                        location2.setTextColor(getResources().getColor(R.color.white));

                        backbtn.setColorFilter(getResources().getColor(R.color.white));
                        optionbtn.setColorFilter(getResources().getColor(R.color.white));
                        settingbtn.setColorFilter(getResources().getColor(R.color.white));
                    }
                    headerPosition = 0;
                } else if (offset > -130) {
                    //semiclosed
                    if (headerPosition != 1 && mHeaderLogo.getVisibility() != View.GONE) {
                        Log.i(TAG, "semiclosed");

                        userLay.setVisibility(View.VISIBLE);
                        userImg.setVisibility(View.VISIBLE);
                        userName2.setVisibility(View.VISIBLE);
                        location2.setVisibility(View.VISIBLE);
                        mHeaderLogo.setVisibility(View.GONE);

                        userLay.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkin));
                        mHeaderLogo.startAnimation(AnimationUtils.loadAnimation(Profile.this, R.anim.blinkout));

                        userName2.setTextColor(getResources().getColor(R.color.white));
                        location2.setTextColor(getResources().getColor(R.color.white));

                        backbtn.setColorFilter(getResources().getColor(R.color.white));
                        optionbtn.setColorFilter(getResources().getColor(R.color.white));
                        settingbtn.setColorFilter(getResources().getColor(R.color.white));
                    }
                    headerPosition = 1;
                } else {
                    //closed
                    if (headerPosition != 2) {
                        Log.i(TAG, "closed");
                        userLay.setVisibility(View.VISIBLE);
                        userImg.setVisibility(View.VISIBLE);
                        userName2.setVisibility(View.VISIBLE);
                        location2.setVisibility(View.VISIBLE);
                        mHeaderLogo.setVisibility(View.GONE);

                        userName2.setTextColor(getResources().getColor(R.color.primaryText));
                        location2.setTextColor(getResources().getColor(R.color.secondaryText));

                        backbtn.setColorFilter(getResources().getColor(R.color.primaryText));
                        optionbtn.setColorFilter(getResources().getColor(R.color.primaryText));
                        settingbtn.setColorFilter(getResources().getColor(R.color.primaryText));
                    }
                    headerPosition = 2;
                }
            }
        });

        ratingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mViewPager.setCurrentItem(4);
                }
                return true;
            }
        });
    }

    /**
     * Function for set the user profile information
     **/
    private void setProfileInformation() {
        try {
            userName.setText(profileMap.get(Constants.TAG_FULL_NAME));
            userName2.setText(profileMap.get(Constants.TAG_FULL_NAME));
            location.setText(profileMap.get(Constants.TAG_USERNAME));
            location2.setText(profileMap.get(Constants.TAG_USERNAME));
            Picasso.with(Profile.this).load(profileMap.get(Constants.TAG_USERIMG)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(mHeaderLogo);
            Picasso.with(Profile.this).load(profileMap.get(Constants.TAG_USERIMG)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImg);

            if (userId.equalsIgnoreCase(GetSet.getUserId())) {
                Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                        MODE_PRIVATE);
                Constants.editor = Constants.pref.edit();
                Constants.editor.putString(Constants.TAG_PHOTO, profileMap.get(Constants.TAG_USERIMG));
                Constants.editor.putString(Constants.TAG_USERNAME, profileMap.get(Constants.TAG_USERNAME));
                Constants.editor.putString(Constants.TAG_FULL_NAME, profileMap.get(Constants.TAG_FULL_NAME));
                Constants.editor.commit();

                GetSet.setImageUrl(Constants.pref.getString(Constants.TAG_PHOTO, null));
                GetSet.setUserName(Constants.pref.getString(Constants.TAG_USERNAME, null));
                GetSet.setFullName(Constants.pref.getString(Constants.TAG_FULL_NAME, null));

                if (FragmentMainActivity.userImage != null && FragmentMainActivity.username != null) {
                    Picasso.with(Profile.this).load(GetSet.getImageUrl()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(FragmentMainActivity.userImage);
                    FragmentMainActivity.username.setText(GetSet.getFullName());
                }
            }

            if (Constants.BUYNOW) {
                reviewLay.setVisibility(View.VISIBLE);
                location.setVisibility(View.GONE);
                try {
                    ratingBar.setRating(Float.parseFloat(profileMap.get(Constants.TAG_RATING)));

                    if(profileMap.get(Constants.TAG_RATING).equals("") || profileMap.get(Constants.TAG_RATING).equals("0.0"))
                        ratingCount.setText("(0)");
                    else {
                        ratingCount.setText("(" + profileMap.get(Constants.TAG_RATING_USER_COUNT) + ")");
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                reviewLay.setVisibility(View.GONE);
                location.setVisibility(View.VISIBLE);
            }

            verificationLay.setVisibility(View.VISIBLE);
            if (profileMap.get(Constants.TAG_FB_VER).equals("true")) {
                fbVerify.setImageResource(R.drawable.fb_veri);
            } else {
                fbVerify.setImageResource(R.drawable.fb_unveri);
            }
            if (profileMap.get( Constants.TAG_EMAIL_VER).equals("true")) {
                mailVerify.setImageResource(R.drawable.mail_veri);
            } else {
                mailVerify.setImageResource(R.drawable.mail_unveri);
            }
            if (profileMap.get(Constants.TAG_MOB_VER).equals("true")) {
                mobVerify.setImageResource(R.drawable.mob_veri);
            } else {
                mobVerify.setImageResource(R.drawable.mob_unveri);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * function for set the toolbar as actionbar
     **/
    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    public void disabledialog(final String content) {
        final Dialog dialog = new Dialog(Profile.this, R.style.AlertDialog);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(getString(R.string.error));
        alertMsg.setText(content);

        alertOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * function for showing the popup window
     **/
    public void viewOptions(View v) {
        String[] values;
        if (Constants.BUYNOW && Constants.EXCHANGE && Constants.PROMOTION) {
            values = new String[]{getString(R.string.myexchange), getString(R.string.my_promotions), getString(R.string.myorders_sales),
                    getString(R.string.my_address)};
        } else if (Constants.BUYNOW && Constants.EXCHANGE) {
            values = new String[]{getString(R.string.myexchange), getString(R.string.myorders_sales),
                    getString(R.string.my_address)};
        } else if (Constants.BUYNOW && Constants.PROMOTION) {
            values = new String[]{getString(R.string.my_promotions), getString(R.string.myorders_sales),
                    getString(R.string.my_address)};
        } else if (Constants.EXCHANGE && Constants.PROMOTION) {
            values = new String[]{getString(R.string.myexchange), getString(R.string.my_promotions)};
        } else if (Constants.EXCHANGE) {
            values = new String[]{getString(R.string.myexchange)};
        } else if (Constants.PROMOTION) {
            values = new String[]{getString(R.string.my_promotions)};
        } else if (Constants.BUYNOW) {
            values = new String[]{getString(R.string.myorders_sales), getString(R.string.my_address)};
        } else {
            values = new String[]{};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.share_new, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.share, null);
        layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
        final PopupWindow popup = new PopupWindow(Profile.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(display.getWidth() * 60 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(main, Gravity.TOP | Gravity.RIGHT, 0, 20);

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        if (Constants.EXCHANGE) {
                            Intent i = new Intent(Profile.this, ExchangeActivity.class);
                            startActivity(i);
                        } else if (Constants.PROMOTION) {
                            Intent j = new Intent(Profile.this, MyPromotions.class);
                            startActivity(j);
                        } else {
                            /*Intent k = new Intent(Profile.this, MySalesnOrder.class);
                            startActivity(k);*/
                        }
                        popup.dismiss();
                        break;
                    case 1:
                        if (Constants.BUYNOW && Constants.EXCHANGE && Constants.PROMOTION) {
                            Intent j = new Intent(Profile.this, MyPromotions.class);
                            startActivity(j);
                        } else if ((Constants.BUYNOW && Constants.EXCHANGE) || (Constants.BUYNOW && Constants.PROMOTION)) {
                           /* Intent k = new Intent(Profile.this, MySalesnOrder.class);
                            startActivity(k);*/
                        } else if (Constants.EXCHANGE && Constants.PROMOTION) {
                            Intent j = new Intent(Profile.this, MyPromotions.class);
                            startActivity(j);
                        } else if (Constants.BUYNOW) {
                            /*Intent l = new Intent(Profile.this, Addresses.class);
                            l.putExtra(Constants.FROM, "profile");
                            startActivity(l);*/
                        }
                        popup.dismiss();
                        break;
                    // The below cases only for buy now module, Otherwise comment it.
                    case 2:
                        if (Constants.BUYNOW && Constants.EXCHANGE && Constants.PROMOTION) {
                            /*Intent k = new Intent(Profile.this, MySalesnOrder.class);
                            startActivity(k);*/
                        } else {
                           /* Intent l = new Intent(Profile.this, Addresses.class);
                            l.putExtra(Constants.FROM, "profile");
                            startActivity(l);*/
                        }
                        popup.dismiss();
                        break;
                    case 3:
                        /*Intent l = new Intent(Profile.this, Addresses.class);
                        l.putExtra(Constants.FROM, "profile");
                        startActivity(l);*/
                        popup.dismiss();
                        break;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(Profile.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(Profile.this);
        /*User clicks back btn then refresh userId*/
        setTabPageAdapter();
    }

    //To set Adapter in View Pager
    private void setTabPageAdapter() {
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(tabPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * Function for follow the user
     **/
    private void follow(final String followId) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_FOLLOW, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    statusLay.setOnClickListener(Profile.this);
                    JSONObject jobj = new JSONObject(response);
                    if (DefensiveClass.optString(jobj, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        followingId.add(userId);
                        followStatus.setImageResource(R.drawable.unfollow);
                        followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorPrimary));
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
                map.put(Constants.TAG_FOLLOW_ID, followId);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for get the user profile information
     **/
    private void getProfileInformation() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PROFILE, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                Log.i(TAG, "getProfileInformation: "+ json);
                try {
                    JSONObject obj = new JSONObject(json);
                    if (DefensiveClass.optString(obj, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        JSONObject result = obj.optJSONObject(Constants.TAG_RESULT);
                        if (result != null) {
                            profileMap.put(Constants.TAG_USERID, DefensiveClass.optString(result, Constants.TAG_USERID));
                            profileMap.put(Constants.TAG_USERNAME, DefensiveClass.optString(result, Constants.TAG_USERNAME));
                            profileMap.put(Constants.TAG_FULL_NAME, DefensiveClass.optString(result, Constants.TAG_FULL_NAME));
                            profileMap.put(Constants.TAG_USERIMG, DefensiveClass.optString(result, Constants.TAG_USERIMG));
                            profileMap.put(Constants.TAG_EMAIL, DefensiveClass.optString(result, Constants.TAG_EMAIL));
                            profileMap.put(Constants.TAG_RATING, DefensiveClass.optInt(result, Constants.TAG_RATING));
                            profileMap.put(Constants.TAG_RATING_USER_COUNT, DefensiveClass.optInt(result, Constants.TAG_RATING_USER_COUNT));
                            profileMap.put(Constants.TAG_FACEBOOK_ID, DefensiveClass.optString(result, Constants.TAG_FACEBOOK_ID));
                            profileMap.put(Constants.TAG_MOBILE_NO, DefensiveClass.optString(result, Constants.TAG_MOBILE_NO));

                            JSONObject verification = result.optJSONObject(Constants.TAG_VERIFICATION);

                            profileMap.put(Constants.TAG_FB_VER, DefensiveClass.optString(verification, Constants.TAG_FACEBOOK));
                            profileMap.put(Constants.TAG_EMAIL_VER, DefensiveClass.optString(verification, Constants.TAG_EMAIL));
                            profileMap.put(Constants.TAG_MOB_VER, DefensiveClass.optString(verification, Constants.TAG_MOB_NO));

                            Log.v(TAG, "userimage=" + DefensiveClass.optString(result, Constants.TAG_USERIMG));

                        }
                    }
                    Log.v(TAG, "profileMap=" + profileMap);
                    if (profileMap.size() != 0) {
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
                Log.e(TAG, "onErrorResponse: "+error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_USERID, userId);
                Log.i(TAG, "getProfileInformationParams: "+ map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }


    /**
     * Function for get the following users id
     **/
    private void getFollowingId() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_FOLLOWER_ID, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        JSONArray result = json.optJSONArray("result");
                        for (int i = 0; i < result.length(); i++) {
                            followingId.add(result.getString(i));
                        }
                    }

                    Log.v(TAG, "followingId=" + followingId);
                    if (!userId.equals(GetSet.getUserId())) {
                        statusLay.setVisibility(View.VISIBLE);
                        if (followingId.contains(userId)) {
                            followStatus.setImageResource(R.drawable.unfollow);
                            followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorPrimary));
                        } else {
                            followStatus.setImageResource(R.drawable.follow);
                            followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorSecondary));
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
                Log.e(TAG, "onErrorResponse: "+error.getMessage());
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                Log.v(TAG,"getFollowingId="+map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * class for set fragments to each page
     **/

    class TabPagerAdapter extends FragmentStatePagerAdapter {
        private String[] profileSubPages;

        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
            if (Constants.BUYNOW) {
                if (userId.equalsIgnoreCase(GetSet.getUserId())) {
                    profileSubPages = new String[]{getString(R.string.my_listing), getString(R.string.liked), getString(R.string.followers), getString(R.string.followings), getString(R.string.review)};
                } else {
                    profileSubPages = new String[]{getString(R.string.listing), getString(R.string.liked), getString(R.string.followers), getString(R.string.followings), getString(R.string.review)};
                }
            } else {
                if (userId.equalsIgnoreCase(GetSet.getUserId())) {
                    profileSubPages = new String[]{getString(R.string.my_listing), getString(R.string.liked), getString(R.string.followers), getString(R.string.followings)};
                } else {
                    profileSubPages = new String[]{getString(R.string.listing), getString(R.string.liked), getString(R.string.followers), getString(R.string.followings)};
                }
            }
        }

        @Override
        public Fragment getItem(int position) {
            /*if (Constants.BUYNOW) {
                if (position == 0) {
                    return MyListing.newInstance(position, userId);
                } else if (position == 1) {
                    return LikedItems.newInstance(position, userId);
                } else if (position == 2) {
                    return Followers.newInstance(position, userId);
                } else if (position == 3) {
                    return Followings.newInstance(position, userId);
                } else {
                    return Review.newInstance(position, userId);
                }
            } else {*/
                if (position == 0) {
                    return MyListing.newInstance(position, userId);
                } else if (position == 1) {
                    return LikedItems.newInstance(position, userId);
                } else if (position == 2) {
                    return Followers.newInstance(position, userId);
                } else {
                    return Followings.newInstance(position, userId);
                }
            //}
        }

        @Override
        public int getCount() {
            return profileSubPages.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return profileSubPages[position];
        }
    }

    /**
     * Function for unfollow the user
     **/
    private void unFollow(final String followId) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_UNFOLLOW, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    statusLay.setOnClickListener(Profile.this);
                    JSONObject jobj = new JSONObject(response);
                    if (DefensiveClass.optString(jobj, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        followingId.remove(userId);
                        followStatus.setImageResource(R.drawable.follow);
                        followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorSecondary));
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
                map.put(Constants.TAG_FOLLOW_ID, followId);
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
            case R.id.optionbtn:
                viewOptions(v);
                break;
            case R.id.settingbtn:
                Intent i = new Intent(Profile.this, EditProfile.class);
                startActivity(i);
                break;
            case R.id.statusLay:
                if (GetSet.isLogged()) {
                    if (followingId.contains(userId)) {
                        followingId.remove(userId);
                        followStatus.setImageResource(R.drawable.follow);
                        followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorSecondary));
                        unFollow(userId);
                    } else {
                        followingId.add(userId);
                        followStatus.setImageResource(R.drawable.unfollow);
                        followStatus.setColorFilter(ContextCompat.getColor(Profile.this, R.color.colorPrimary));
                        follow(userId);
                    }
                } else {
                    Intent k = new Intent(Profile.this, WelcomeActivity.class);
                    startActivity(k);
                }
                break;
        }
    }
}