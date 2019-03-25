package com.app.lystiq;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.external.ExpandableHeightListView;
import com.app.external.RangeSeekBar;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.hitasoft.materialslider.MaterialSeekBar;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft.
 * <p>
 * This class is for Filter Page.
 */

public class SearchAdvance extends AppCompatActivity implements OnClickListener, MaterialSeekBar.OnSeekBarChangeListener {
    /**
     * Declare Layout Elements
     **/
    public static RangeSeekBar priceBar;
    TextView title, last24Txt, last7Txt, last30Txt, allproductTxt, popularTxt, urgentTxt, highTxt, lowTxt, reset, apply, seektext, minPrice, maxPrice, locationName;
    ImageView backbtn, home, road, last24Next, last7Next, last30Next, allproductNext, popularNext, urgentNext, highNext, lowNext, lnext;
    RelativeLayout locationLay, mainLay, urgentLay;
    LinearLayout saveLay, priceSeekLay;
    ExpandableHeightListView category;
    AVLoadingIndicatorView progress;
    MaterialSeekBar materialSlider;

    CategoryAdapter categoryAdapter;
    InputMethodManager imm;
    public static ArrayList<String> categoryId = new ArrayList<String>(), categoryName = new ArrayList<String>();
    public static HashMap<String, String> subcategoryId = new HashMap<String, String>();
    ArrayList<HashMap<String, String>> categoryAry = new ArrayList<HashMap<String, String>>();
    ArrayList<ArrayList<HashMap<String, String>>> subcategAry = new ArrayList<ArrayList<HashMap<String, String>>>();

    /**
     * Declare Variables
     **/
    String TAG = "SelectCategory",distanceType;
    public static String postedWithin = "", sortBy = "1", distance = "0", postedTxt = "", sortTxt = "";
    public String tempPostedWithin = "", tempSortBy = "1";
    public static float distanceX;
    public static boolean applyFilter = false;
    public static int priceMinimum = 0, priceMaximum = 5000000, priceMin, priceMax,zeroMax = 1, storePriceMin = 0, storePriceMax = 0;
    public int tempPriceMin, tempPriceMax, primaryText, colorPrimary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_advance);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        materialSlider = (MaterialSeekBar) findViewById(R.id.materialSeekBar);
        seektext = (TextView) findViewById(R.id.seektext);
        title = (TextView) findViewById(R.id.title);
        home = (ImageView) findViewById(R.id.home);
        road = (ImageView) findViewById(R.id.road);
        locationLay = (RelativeLayout) findViewById(R.id.locationLay);
        locationName = (TextView) findViewById(R.id.locationName);
        category = (ExpandableHeightListView) findViewById(R.id.category);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        mainLay = (RelativeLayout) findViewById(R.id.mainLay);
        saveLay = (LinearLayout) findViewById(R.id.saveLay);
        last24Txt = (TextView) findViewById(R.id.last24Txt);
        last7Txt = (TextView) findViewById(R.id.last7Txt);
        last30Txt = (TextView) findViewById(R.id.last30Txt);
        allproductTxt = (TextView) findViewById(R.id.allproductTxt);
        popularTxt = (TextView) findViewById(R.id.popularTxt);
        urgentTxt = (TextView) findViewById(R.id.urgentTxt);
        highTxt = (TextView) findViewById(R.id.highTxt);
        lowTxt = (TextView) findViewById(R.id.lowTxt);
        reset = (TextView) findViewById(R.id.reset);
        apply = (TextView) findViewById(R.id.apply);
        last24Next = (ImageView) findViewById(R.id.last24Next);
        last7Next = (ImageView) findViewById(R.id.last7Next);
        last30Next = (ImageView) findViewById(R.id.last30Next);
        allproductNext = (ImageView) findViewById(R.id.allproductNext);
        popularNext = (ImageView) findViewById(R.id.popularNext);
        urgentNext = (ImageView) findViewById(R.id.urgentNext);
        highNext = (ImageView) findViewById(R.id.highNext);
        lowNext = (ImageView) findViewById(R.id.lowNext);
        urgentLay = (RelativeLayout) findViewById(R.id.urgentLay);
        lnext = (ImageView) findViewById(R.id.lnext);
        priceSeekLay = (LinearLayout) findViewById(R.id.priceSeekLay);
        minPrice = (TextView) findViewById(R.id.minPrice);
        maxPrice = (TextView) findViewById(R.id.maxPrice);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        saveLay.setVisibility(View.GONE);
        mainLay.setVisibility(View.GONE);

        title.setText(getString(R.string.filter));
        distanceType = JoysaleApplication.adminPref.getString(Constants.PREF_DISTANCE_TYPE, "km");

        backbtn.setOnClickListener(this);
        locationLay.setOnClickListener(this);
        last24Txt.setOnClickListener(this);
        last7Txt.setOnClickListener(this);
        last30Txt.setOnClickListener(this);
        allproductTxt.setOnClickListener(this);
        popularTxt.setOnClickListener(this);
        urgentTxt.setOnClickListener(this);
        highTxt.setOnClickListener(this);
        lowTxt.setOnClickListener(this);
        last24Next.setOnClickListener(this);
        last7Next.setOnClickListener(this);
        last30Next.setOnClickListener(this);
        allproductNext.setOnClickListener(this);
        popularNext.setOnClickListener(this);
        urgentNext.setOnClickListener(this);
        highNext.setOnClickListener(this);
        lowNext.setOnClickListener(this);
        reset.setOnClickListener(this);
        apply.setOnClickListener(this);
        category.setExpanded(true);

        Log.e("pricemax","- "+priceMax +" , pricemin - "+ priceMin +" , zeroMax - "+zeroMax);

        materialSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress == 0) {
                    home.setBackgroundResource(R.drawable.f_hme);
                    road.setBackgroundResource(R.drawable.f_road);
                    seektext.setVisibility(View.GONE);
                } else {
                    home.setBackgroundResource(R.drawable.f_hme_select);
                    road.setBackgroundResource(R.drawable.f_road_select);
                    seektext.setVisibility(View.VISIBLE);
                }
                seekText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                distance = String.valueOf(seekBar.getProgress());
            }
        });

        priceBar = new RangeSeekBar<Integer>(priceMinimum, priceMaximum, SearchAdvance.this);

        if(zeroMax == 0 && priceMin == 0 && priceMax ==0){
            priceBar.setSelectedMinValue(0);
            priceBar.setSelectedMaxValue(0);
            minPrice.setText("0");
            maxPrice.setText("0");
            tempPriceMin =0;
            tempPriceMax =0;
        }else if(priceMin != 0 || (priceMax !=5000000 && priceMax != 0)){
            priceBar.setSelectedMinValue(priceMin);
            priceBar.setSelectedMaxValue(priceMax);
            minPrice.setText(""+priceMin);
            maxPrice.setText(""+priceMax);
            tempPriceMin =priceMin;
            tempPriceMax =priceMax;
        }else {
            priceBar.setSelectedMinValue(priceMinimum);
            priceBar.setSelectedMaxValue(priceMaximum);
            tempPriceMin =priceMinimum;
            tempPriceMax =priceMaximum;
        }

        priceBar.setDefaultColor(getResources().getColor(R.color.colorPrimary));
        priceBar.setNotifyWhileDragging(true);

        priceBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                // handle changed range values
                minPrice.setText(""+minValue);
                maxPrice.setText(""+maxValue);
                if(minValue==0 && maxValue==5000000) {
                    tempPriceMin = 0;
                    tempPriceMax = 5000000;
                }else if(minValue==0 && maxValue==0){
                    tempPriceMin = minValue;
                    tempPriceMax = maxValue;
                }else {
                    tempPriceMin = minValue;
                    tempPriceMax = maxValue;
                }
            }

        });

        priceSeekLay.addView(priceBar);

        primaryText = getResources().getColor(R.color.primaryText);
        colorPrimary = getResources().getColor(R.color.colorPrimary);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (FragmentMainActivity.actv1.getText().toString() != null && !FragmentMainActivity.actv1.getText().toString().equals("")) {
//            locationName.setText(LocationActivity.location);
            locationName.setText(FragmentMainActivity.actv1.getText().toString());
        }else {
            locationName.setText(R.string.location_temp);
        }

        setSortBy(sortBy);
        setPostedWithin(postedWithin);

        materialSlider.setProgress(Integer.parseInt(distance));
        saveLay.setVisibility(View.GONE);
        mainLay.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        //To get category from Api
        getCategory();

        //To initialize and set the Adapter
        categoryAdapter = new CategoryAdapter(SearchAdvance.this, categoryAry, subcategAry);
        category.setAdapter(categoryAdapter);

        if(locationName.getText()!=null) {
            if (locationName.getText().toString().equals(getString(R.string.world_wide))) {
                materialSlider.setEnabled(false);
            } else {
                materialSlider.setEnabled(true);
            }
        }

        if (Constants.PROMOTION) {
            urgentLay.setVisibility(View.VISIBLE);
        } else {
            urgentLay.setVisibility(View.GONE);
        }

        if (JoysaleApplication.isRTL(SearchAdvance.this)) {
            lnext.setRotation(180);
        } else {
            lnext.setRotation(0);
        }

        ViewTreeObserver observer = materialSlider.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.v(TAG, "conditionBar.getRight()==" + materialSlider.getRight());
                if (materialSlider.getRight() != 0) {
                    seekText(Integer.parseInt(distance));
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        materialSlider.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        materialSlider.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            }
        });

    }

    private void setPostedWithin(String type) {
        last24Txt.setTextColor(primaryText);
        last7Txt.setTextColor(primaryText);
        last30Txt.setTextColor(primaryText);
        allproductTxt.setTextColor(primaryText);

        last24Next.setVisibility(View.GONE);
        last7Next.setVisibility(View.GONE);
        last30Next.setVisibility(View.GONE);
        allproductNext.setVisibility(View.GONE);

        last24Next.setColorFilter(primaryText);
        last7Next.setColorFilter(primaryText);
        last30Next.setColorFilter(primaryText);
        allproductNext.setColorFilter(primaryText);
        postedTxt = "";
        switch (type) {
            case "last24h":
                postedTxt = getString(R.string.last24h);
                tempPostedWithin = "last24h";
                last24Txt.setTextColor(colorPrimary);
                last24Next.setVisibility(View.VISIBLE);
                last24Next.setColorFilter(colorPrimary);
                break;
            case "last7d":
                postedTxt = getString(R.string.last7d);
                tempPostedWithin = "last7d";
                last7Txt.setTextColor(colorPrimary);
                last7Next.setVisibility(View.VISIBLE);
                last7Next.setColorFilter(colorPrimary);
                break;
            case "last30d":
                postedTxt = getString(R.string.last30d);
                tempPostedWithin = "last30d";
                last30Txt.setTextColor(colorPrimary);
                last30Next.setVisibility(View.VISIBLE);
                last30Next.setColorFilter(colorPrimary);
                break;
            case "all":
                postedTxt = getString(R.string.all);
                tempPostedWithin = "all";
                allproductTxt.setTextColor(colorPrimary);
                allproductNext.setVisibility(View.VISIBLE);
                allproductNext.setColorFilter(colorPrimary);
                break;
        }
    }

    /**
     * for change the seekbar progreess
     **/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        seekBar.setProgress(progress);
        if (progress == 0) {
            home.setBackgroundResource(R.drawable.f_hme);
            road.setBackgroundResource(R.drawable.f_road);
            seektext.setVisibility(View.GONE);
        } else {
            home.setBackgroundResource(R.drawable.f_hme_select);
            road.setBackgroundResource(R.drawable.f_road_select);
            seektext.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        distance = String.valueOf(seekBar.getProgress());
    }

    /**
     * Function for change the seekbar progress text
     **/
    private void seekText(int how_many) {
        String what_to_say = String.valueOf(how_many);
        seektext.setText(what_to_say + " " + distanceType);

        int extraPadding = JoysaleApplication.dpToPx(SearchAdvance.this, 15);
        int right = materialSlider.getRight() - extraPadding;
        int left = materialSlider.getLeft() + extraPadding;
        int seek_label_pos = (((right - left) * materialSlider.getProgress()) / materialSlider.getMax()) + left;
        Log.v(TAG, "xvalue=" + seek_label_pos);
        if (seek_label_pos == 0) {
            float xvalue = distanceX - seektext.getWidth() / 2;
            seektext.setX(xvalue);
        } else {
            float xvalue = seek_label_pos - seektext.getWidth() / 2;
            distanceX = seek_label_pos;
            seektext.setX(xvalue);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(SearchAdvance.this);
        if (categoryAdapter != null) {
            categoryAdapter.notifyDataSetChanged();
        }
    }

    /**
     * set selected sort by
     **/
    private void setSortBy(String type) {
        popularTxt.setTextColor(primaryText);
        urgentTxt.setTextColor(primaryText);
        highTxt.setTextColor(primaryText);
        lowTxt.setTextColor(primaryText);

        popularNext.setVisibility(View.GONE);
        urgentNext.setVisibility(View.GONE);
        highNext.setVisibility(View.GONE);
        lowNext.setVisibility(View.GONE);

        popularNext.setColorFilter(primaryText);
        urgentNext.setColorFilter(primaryText);
        highNext.setColorFilter(primaryText);
        lowNext.setColorFilter(primaryText);
        sortTxt = "";
        switch (type) {
            case "2":
                sortTxt = getString(R.string.popular);
                tempSortBy = "2";
                popularTxt.setTextColor(colorPrimary);
                popularNext.setVisibility(View.VISIBLE);
                popularNext.setColorFilter(colorPrimary);
                break;
            case "3":
                sortTxt = getString(R.string.hightlow);
                tempSortBy = "3";
                highTxt.setTextColor(colorPrimary);
                highNext.setVisibility(View.VISIBLE);
                highNext.setColorFilter(colorPrimary);
                break;
            case "4":
                sortTxt = getString(R.string.lowthigh);
                tempSortBy = "4";
                lowTxt.setTextColor(colorPrimary);
                lowNext.setVisibility(View.VISIBLE);
                lowNext.setColorFilter(colorPrimary);
                break;
            case "5":
                sortTxt = getString(R.string.urgent);
                tempSortBy = "5";
                urgentTxt.setTextColor(colorPrimary);
                urgentNext.setVisibility(View.VISIBLE);
                urgentNext.setColorFilter(colorPrimary);
                break;
        }
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(SearchAdvance.this);
        super.onPause();
    }

    /**
     * Function for get the category from admin
     **/

    private void getCategory() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CATEGORY, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                try {
                    JSONObject jobj = new JSONObject(json);
                    String response = jobj.getString(Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        JSONObject result = jobj
                                .optJSONObject(Constants.TAG_RESULT);
                        if (!(result == null)) {
                            JSONArray category = result.optJSONArray(Constants.TAG_CATEGORY);
                            if (category != null) {
                                for (int i = 0; i < category.length(); i++) {
                                    HashMap<String, String> map = new HashMap<String, String>();
                                    JSONObject temp = category.getJSONObject(i);
                                    map.put(Constants.TAG_CATEGORYNAME, DefensiveClass.optString(temp, Constants.TAG_CATEGORYNAME));
                                    map.put(Constants.TAG_CATEGORYID, DefensiveClass.optString(temp, Constants.TAG_CATEGORYID));
                                    map.put(Constants.TAG_CATEGORYIMG, DefensiveClass.optString(temp, Constants.TAG_CATEGORYIMG));
                                    categoryAry.add(map);

                                    ArrayList<HashMap<String, String>> tempAry = new ArrayList<HashMap<String, String>>();
                                    JSONArray subcategory = temp.optJSONArray(Constants.TAG_SUBCATEGORY);
                                    for (int j = 0; j < subcategory.length(); j++) {
                                        HashMap<String, String> smap = new HashMap<String, String>();
                                        JSONObject stemp = subcategory.getJSONObject(j);
                                        smap.put(Constants.TAG_SUBID, DefensiveClass.optString(stemp, Constants.TAG_SUBID));
                                        smap.put(Constants.TAG_SUBNAME, DefensiveClass.optString(stemp, Constants.TAG_SUBNAME));
                                        tempAry.add(smap);
                                    }

                                    HashMap<String, String> tmap = new HashMap<String, String>();
                                    tmap.put(Constants.TAG_SUBID, "all");
                                    tmap.put(Constants.TAG_SUBNAME, getString(R.string.all));
                                    tempAry.add(0, tmap);
                                    subcategAry.add(tempAry);
                                }
                            }
                        }
                        progress.setVisibility(View.GONE);
                        saveLay.setVisibility(View.VISIBLE);
                        mainLay.setVisibility(View.VISIBLE);
                        if (categoryAry.size() == 0) {
                            Toast.makeText(SearchAdvance.this, getString(R.string.category_problem), Toast.LENGTH_SHORT).show();
                        }
                        Log.v("categoryAry", "categoryAry=" + categoryAry);
                        Log.v("subcategAry", "subcategAry=" + subcategAry);
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
                map.put(Constants.LANG_TYPE, AppUtils.getCurrentLanguageCode(SearchAdvance.this));
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Adapter for  Category
     **/

    public class CategoryAdapter extends BaseAdapter {

        ArrayList<HashMap<String, String>> datas;
        ArrayList<ArrayList<HashMap<String, String>>> subcateg = new ArrayList<ArrayList<HashMap<String, String>>>();
        ViewHolder holder = null;
        Context mContext;

        public CategoryAdapter(Context ctx, ArrayList<HashMap<String, String>> data, ArrayList<ArrayList<HashMap<String, String>>> subcat) {
            mContext = ctx;
            datas = data;
            subcateg = subcat;
        }

        @Override
        public int getCount() {
            return datas.size();
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
                convertView = inflater.inflate(R.layout.filter_row_selection, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.tick = (ImageView) convertView.findViewById(R.id.tick);
                holder.next = (ImageView) convertView.findViewById(R.id.next);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                holder.name.setText(datas.get(position).get(Constants.TAG_CATEGORYNAME));
                holder.name.setTextColor(getResources().getColor(R.color.primaryText));
                holder.tick.setVisibility(View.INVISIBLE);
                holder.next.setVisibility(View.INVISIBLE);

                if (subcateg.get(position).size() == 1) {
                    if (categoryId.contains(datas.get(position).get(Constants.TAG_CATEGORYID))) {
                        holder.tick.setVisibility(View.VISIBLE);
                        holder.next.setVisibility(View.INVISIBLE);
                    } else {
                        holder.tick.setVisibility(View.INVISIBLE);
                        holder.next.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (categoryId.contains(datas.get(position).get(Constants.TAG_CATEGORYID))) {
                        holder.tick.setVisibility(View.VISIBLE);
                        holder.next.setVisibility(View.INVISIBLE);
                    } else {
                        holder.tick.setVisibility(View.INVISIBLE);
                        holder.next.setVisibility(View.VISIBLE);
                    }
                }

                if (JoysaleApplication.isRTL(mContext)) {
                    holder.next.setRotation(180);
                    holder.name.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                } else {
                    holder.next.setRotation(0);
                    holder.name.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                }

                holder.mainLay.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (subcateg.get(position).size() == 1) {
                            if (categoryId.contains(datas.get(position).get(Constants.TAG_CATEGORYID))) {
                                categoryId.remove(datas.get(position).get(Constants.TAG_CATEGORYID));
                                categoryName.remove(datas.get(position).get(Constants.TAG_CATEGORYNAME));
                            } else {
                                categoryId.add(datas.get(position).get(Constants.TAG_CATEGORYID));
                                categoryName.add(datas.get(position).get(Constants.TAG_CATEGORYNAME));
                            }
                            categoryAdapter.notifyDataSetChanged();
                        } else {
                            if (subcategoryId.get(datas.get(position).get(Constants.TAG_CATEGORYID)) == null) {
                                subcategoryId.put(datas.get(position).get(Constants.TAG_CATEGORYID), "");
                            }
                            Intent i = new Intent(SearchAdvance.this, SubCategory.class);
                            i.putExtra(Constants.FROM, "filter_icon");
                            i.putExtra(Constants.TAG_CATEGORYNAME, datas.get(position).get(Constants.TAG_CATEGORYNAME));
                            i.putExtra(Constants.CATEGORYID, datas.get(position).get(Constants.TAG_CATEGORYID));
                            i.putExtra(Constants.DATA, subcateg.get(position));
                            startActivity(i);
                        }
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
            ImageView tick, next;
            TextView name;
            RelativeLayout mainLay;
        }
    }

    /**
     * Function for OnClick Event
     **/
    static String loc_name="";
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.locationLay:
                showLocationDialog();
//                Intent i = new Intent(SearchAdvance.this, LocationActivity.class);
//                i.putExtra("from", "home");
//                startActivity(i);
                break;
            case R.id.last24Txt:
            case R.id.last24Next:
                tempPostedWithin = "last24h";
                setPostedWithin(tempPostedWithin);
                break;
            case R.id.last7Txt:
            case R.id.last7Next:
                tempPostedWithin = "last7d";
                setPostedWithin(tempPostedWithin);
                break;
            case R.id.last30Txt:
            case R.id.last30Next:
                tempPostedWithin = "last30d";
                setPostedWithin(tempPostedWithin);
                break;
            case R.id.allproductTxt:
            case R.id.allproductNext:
                tempPostedWithin = "all";
                setPostedWithin(tempPostedWithin);
                break;
            case R.id.popularTxt:
            case R.id.popularNext:
                tempSortBy = "2";
                setSortBy(tempSortBy);
                break;
            case R.id.urgentTxt:
            case R.id.urgentNext:
                tempSortBy = "5";
                setSortBy(tempSortBy);
                break;
            case R.id.highTxt:
            case R.id.highNext:
                tempSortBy = "3";
                setSortBy(tempSortBy);
                break;
            case R.id.lowTxt:
            case R.id.lowNext:
                tempSortBy = "4";
                setSortBy(tempSortBy);
                break;
            case R.id.reset:
                distance = "0";
                distanceX = 0;
                categoryId.clear();
                categoryName.clear();
                subcategoryId.clear();
                postedWithin = "";
                sortBy = "1";
                priceMax = 0;
                priceMin = 0;
                zeroMax = 1;
                priceBar.setSelectedMaxValue(priceMaximum);
                priceBar.setSelectedMinValue(priceMinimum);
                minPrice.setText("0");
                maxPrice.setText("5000");
                categoryAdapter.notifyDataSetChanged();
                setPostedWithin(postedWithin);
                setSortBy(sortBy);
                FragmentMainActivity.lat = 0.0;
                FragmentMainActivity.lon = 0.0;
                materialSlider.setProgress(Integer.parseInt(distance));
                FragmentMainActivity.filterAry.clear();
                if (FragmentMainActivity.filterAdapter != null)
                    FragmentMainActivity.filterAdapter.notifyDataSetChanged();
                applyFilter = true;
                finish();
                Intent l = new Intent(SearchAdvance.this, FragmentMainActivity.class);
                startActivity(l);
                break;
            case R.id.apply:
                FragmentMainActivity.filterAry.clear();
                if (FragmentMainActivity.filterAdapter != null)
                    FragmentMainActivity.filterAdapter.notifyDataSetChanged();

                if(tempPriceMin != 0 || tempPriceMax != 5000) {
                    storePriceMin = tempPriceMin;
                    storePriceMax = tempPriceMax;

                    priceMin = storePriceMin;
                    priceMax = storePriceMax;

                    if(tempPriceMin == 0 && tempPriceMax == 0)
                        zeroMax = 0;
                    else
                        zeroMax = 1;

                }else if(tempPriceMin == 0 && tempPriceMax ==5000){

                    zeroMax = 1;
                    priceMin = 0;
                    priceMax = 0;
                }

                loc_name = locationName.getText().toString();

                if (loc_name != null && !loc_name.equals("")) {
//            locationName.setText(LocationActivity.location);
                    FragmentMainActivity.actv1.setText(loc_name);
                }else {
                    FragmentMainActivity.actv1.setText(loc_name);
                }

                postedWithin = tempPostedWithin;
                sortBy = tempSortBy;
                applyFilter = true;
                finish();
                Intent k = new Intent(SearchAdvance.this, FragmentMainActivity.class);
                if(loc_name != null && !loc_name.equals(getString(R.string.world_wide))){
                    k.putExtra("loc_name",loc_name);
                }
                k.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(k);
                break;
        }
    }

    int prevPosition=0;
    private void showLocationDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.country_select_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        ListView locationList = (ListView) dialog.findViewById(R.id.countryLists);
        locationList.setAdapter(new ArrayAdapter<String>(SearchAdvance.this, android.R.layout.simple_list_item_1, FragmentMainActivity.autolocationListAry));
        if (!dialog.isShowing()) {
            dialog.show();
        }
        locationList.setSelection(prevPosition);

        locationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int positionn, long id) {
                String selected_loc = FragmentMainActivity.autolocationListAry.get(positionn);
                String selected_locID = FragmentMainActivity.locationListIDAry.get(positionn);
                /*Pass Country Id using setTag() and Country Name using setText()*/
                locationName.setTag(selected_locID);
                locationName.setText(selected_loc);
                locationName.setTextColor(getResources().getColor(R.color.primaryText));
                if(selected_loc != null && !selected_loc.equals(getString(R.string.world_wide))){
                    FragmentMainActivity.lat = Double.valueOf(FragmentMainActivity.locationAry.get(positionn-1).get(Constants.TAG_LOCATION_LAT));
                    FragmentMainActivity.lon = Double.valueOf(FragmentMainActivity.locationAry.get(positionn-1).get(Constants.TAG_LOCATION_LON));
                    materialSlider.setEnabled(true);
                }else{
                    FragmentMainActivity.lat = 0.0;
                    FragmentMainActivity.lon = 0.0;
                    materialSlider.setEnabled(false);
                }
                prevPosition = positionn;
                dialog.dismiss();
            }
        });
    }
}