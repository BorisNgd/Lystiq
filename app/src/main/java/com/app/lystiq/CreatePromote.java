package com.app.lystiq;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft  on 24/6/16.
 * <p>
 * This class is for Create a Promotion
 */

public class CreatePromote extends AppCompatActivity implements View.OnClickListener{

    /**
     * Declare Layout Elements
     **/
    public static TabLayout slidingTabLayout;
    ViewPager mViewPager;
    TextView title;
    ImageView backBtn;

    /**
     * Declare Variables
     **/
    static final String TAG = "CreatePromote";
    static ArrayList<HashMap<String, String>> promoteItems = new ArrayList<>();
    static String itemId = "", currencySymbol = "", currencyCode = "", urgent = "",clientToken = "";
    int mNumFragments = 2;
    ViewPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promote);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        slidingTabLayout = (TabLayout) findViewById(R.id.slideTab);
        title = (TextView) findViewById(R.id.title);

        promoteItems = new ArrayList<>();
        itemId = getIntent().getExtras().getString("itemId");

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.create_promotion));
        setupAdapter();

        if (PromoteUrgent.progress != null)
            PromoteUrgent.progress.setVisibility(View.VISIBLE);
        if (PromoteUrgent.main != null)
            PromoteUrgent.main.setVisibility(View.GONE);
        if (Promotead.progress != null)
            Promotead.progress.setVisibility(View.VISIBLE);
        if (Promotead.main != null)
            Promotead.main.setVisibility(View.GONE);
        if (PromoteUrgent.pay != null)
            PromoteUrgent.pay.setVisibility(View.GONE);
        if (Promotead.payPromote != null)
            Promotead.payPromote.setVisibility(View.GONE);

        loadPromotion();

        backBtn.setOnClickListener(this);

    }

    /**
     * set viewpager and sliding tab
     **/
    public void setupAdapter() {
        CharSequence titles[] = {getString(R.string.urgent), getString(R.string.advertisement)};

        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, mNumFragments);
        Log.v(TAG, "Urgent" + mAdapter);
        mViewPager.setAdapter(mAdapter);
        slidingTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(CreatePromote.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(CreatePromote.this);
    }


    public static class ViewPagerAdapter extends FragmentStatePagerAdapter {
        CharSequence titles[];
        int numbOfTabs;

        public ViewPagerAdapter(FragmentManager fm, CharSequence titles[], int noOfTabs) {
            super(fm);
            this.titles = titles;
            this.numbOfTabs = noOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            Log.v(TAG, "Urgentget");
            if (position == 0) {
                return new PromoteUrgent();
            } else if (position == 1) {
                return new Promotead();
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return numbOfTabs;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }

    /**
     * Function for get promotion datas form admin
     **/

    private void loadPromotion(){
        StringRequest req = new StringRequest(Request.Method.POST,Constants.API_GET_PROMOTION, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                try {
                    JSONObject jObj = new JSONObject(json);
                    String response = DefensiveClass.optString(jObj, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        JSONObject result = jObj.getJSONObject(Constants.TAG_RESULT);
                        urgent = DefensiveClass.optString(result, Constants.TAG_URGENT);
                        currencySymbol = DefensiveClass.optString(result, Constants.TAG_CURRENCY_SYM).trim();
                        currencyCode = DefensiveClass.optString(result, Constants.TAG_CURRENCY_CODE);
                        JSONArray otherPromo = result.optJSONArray("other_promotions");
                        for (int i = 0; i < otherPromo.length(); i++) {
                            HashMap<String, String> map = new HashMap<>();
                            JSONObject promo = otherPromo.getJSONObject(i);

                            map.put(Constants.TAG_ID, DefensiveClass.optString(promo, Constants.TAG_ID));
                            map.put(Constants.TAG_NAME, DefensiveClass.optString(promo, Constants.TAG_NAME));
                            map.put(Constants.TAG_PRICE, DefensiveClass.optString(promo, Constants.TAG_PRICE));
                            map.put(Constants.TAG_DAYS, DefensiveClass.optString(promo, Constants.TAG_DAYS));

                            promoteItems.add(map);
                        }
                    }

                    Log.v(TAG, "promoteItems=" + promoteItems);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (PromoteUrgent.progress != null)
                    PromoteUrgent.progress.setVisibility(View.GONE);
                if (PromoteUrgent.main != null)
                    PromoteUrgent.main.setVisibility(View.VISIBLE);
                if (Promotead.progress != null)
                    Promotead.progress.setVisibility(View.GONE);
                if (Promotead.main != null)
                    Promotead.main.setVisibility(View.VISIBLE);
                if (PromoteUrgent.pay != null)
                    PromoteUrgent.pay.setVisibility(View.VISIBLE);
                if (Promotead.payPromote != null)
                    Promotead.payPromote.setVisibility(View.VISIBLE);
                if (!CreatePromote.urgent.equals("")) {
                    String price = CreatePromote.currencySymbol + " " + String.format("%.2f", Float.parseFloat(CreatePromote.urgent));
                    PromoteUrgent.adText.setText(Html.fromHtml(getString(R.string.urgent_des) + " <font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + price + "</font>"));
                }
                Promotead.adapter.notifyDataSetChanged();
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
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * On Click Event
     **/

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.backbtn){
            finish();
        }
    }
}
