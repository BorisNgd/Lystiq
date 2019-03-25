package com.app.lystiq;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.vision.text.Line;
import com.app.external.AutoScrollViewPager;
import com.app.external.BadgeView;
import com.app.external.FloatingActionButton;
import com.app.external.GridRecyclerOnScrollListener;
import com.app.external.HorizontalListView;
import com.app.external.RecyclerItemClickListener;
import com.app.external.TimeAgo;
import com.app.helper.ItemAdapter;
import com.app.helper.Model;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.ItemsParsing;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.LinePageIndicator;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.app.utils.Constants.BUYNOW;
import static com.app.utils.Constants.ITEM_LIMIT;

/**
 * Created by hitasoft.
 * <p>
 * This class is for User Home Page.
 */
public class FragmentMainActivity extends AppCompatActivity implements OnClickListener, SwipeRefreshLayout.OnRefreshListener, TextWatcher {

    /**
     * Declare Layout Elements
     **/
    RecyclerView recyclerView;
    public static ListView listView;
    static TextView username, locationTxt;
    EditText locationTxt1;
    static ImageView userImage;
    TextView login, userid, ratingCount;
    ImageView titleImage, menu_btn, filter_btn, search_btn, notifybtn,downArrow,crossIcon;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    LinearLayout profheader, proflogin, nullLay, left_drawer,locationListLay,iconLay;
    RelativeLayout locationLay, headerLay, reviewLay;
    Toolbar toolbar;
    SwipeRefreshLayout swipeLayout;
    AVLoadingIndicatorView progress;
    HorizontalListView filterList;
    View filterView;
    Display display;
    BadgeView notifyBadge;
    RatingBar ratingBar;
    FloatingActionButton btnAddStuff;
    Dialog inviteDialog = null;

    public static ItemAdapter adapter;
    public static FilterAdapter filterAdapter;
    public static ItemViewAdapter itemAdapter;
    public static GridRecyclerOnScrollListener mScrollListener;
    NpaGridLayoutManager itemManager;

    List<Address> addresses;
    Location mylocation;
    GoogleApiClient googleApiClient;

    ArrayAdapter locationAdapter;
    static Double lat;
    static Double lon;
    int prevPosition;

    /**
     * Declare Variables
     **/
    final String TAG = "FragmentMainActivity";
    final static int REQUEST_CHECK_SETTINGS_GPS = 0x1, REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2, REQUEST_CAMERA_PERMISSION = 0x3;
    public static int screenWidth, screenHeight, screenHalf, currentPage = 0, bannerHeight;
    public static String chatCount = "", homeBanner = "";
    public static ArrayList<HashMap<String, String>> filterAry = new ArrayList<HashMap<String, String>>(), homeItemList = new ArrayList<HashMap<String, String>>(), bannerAry = new ArrayList<HashMap<String, String>>(), categoryAry = new ArrayList<HashMap<String, String>>();
    public static ArrayList<HashMap<String, String>> locationAry = new ArrayList<HashMap<String, String>>();
    int mDrawerPosition = 0;
    boolean pulldown = false, mDrawerItemClicked = false;
    Boolean categoryFlag = true, filterFlag = true;

    static ArrayList<String> locationListAry = new ArrayList<String>();
    static ArrayList<String> locationListIDAry = new ArrayList<String>();

    static ArrayList<String> autolocationListAry = new ArrayList<String>();
    static ArrayList<String> partial_autolocationListAry = new ArrayList<String>();

    int downArrowCount = 0;

    ListView locationList;
    static AutoCompleteTextView actv;
    static EditText actv1;
    ArrayAdapter<String> autoCompeleteAdapter;
    InputMethodManager imm;
    ArrayAdapter<String> locationListAdapter;
    RelativeLayout locationLay1,autoLocationLay;
    CoordinatorLayout rootview;
    RelativeLayout top;
    Toolbar toolBar;
    TextView locationTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_frame);

        // For Getting Side Menu itemList
        Model.LoadModel(FragmentMainActivity.this);

        final String[] ids = new String[Model.items.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Integer.toString(i + 1);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Elements initialisation
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        top = (RelativeLayout) findViewById(R.id.top);
        menu_btn = (ImageView) findViewById(R.id.menubtn);
        filter_btn = (ImageView) findViewById(R.id.homefilterbtn);
        search_btn = (ImageView) findViewById(R.id.searchbtn);
        listView = (ListView) findViewById(R.id.list);
        profheader = (LinearLayout) findViewById(R.id.profile_header);
        proflogin = (LinearLayout) findViewById(R.id.profile_login);
        headerLay = (RelativeLayout) findViewById(R.id.header);
        login = (TextView) findViewById(R.id.login);
        username = (TextView) findViewById(R.id.userName);
        userid = (TextView) findViewById(R.id.userId);
        userImage = (ImageView) findViewById(R.id.userImage);
        titleImage = (ImageView) findViewById(R.id.titleimg);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        btnAddStuff = (FloatingActionButton) findViewById(R.id.btnAddStuff);
//        locationLay = (RelativeLayout) findViewById(R.id.locationLay);
//        locationTxt = (TextView) findViewById(R.id.locationTxt);
        filterList = (HorizontalListView) findViewById(R.id.filterList);
        filterView = (View) findViewById(R.id.filterView);
        notifybtn = (ImageView) findViewById(R.id.notifybtn);
        reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingCount = (TextView) findViewById(R.id.ratingCount);
        left_drawer = (LinearLayout) findViewById(R.id.left_drawer);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        downArrow = (ImageView) findViewById(R.id.downIcon1);
        locationLay1 = (RelativeLayout) findViewById(R.id.locationLay1);
        autoLocationLay = (RelativeLayout) findViewById(R.id.autoLocationLay);
        iconLay = (LinearLayout) findViewById(R.id.iconLay);


        locationListLay = (LinearLayout) findViewById(R.id.locationListLay);
        locationList = (ListView) findViewById(R.id.locationList);
        locationList.setTextFilterEnabled(true);

        titleImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
            }
        });

        locationListLay.setVisibility(GONE);

        top.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
            }
        });



//        drawer.setOnClickListener(FragmentMainActivity.this);
//        rootview.setOnClickListener(FragmentMainActivity.this);
//        rootLayout.setOnClickListener(FragmentMainActivity.this);

        notifyBadge = new BadgeView(FragmentMainActivity.this, notifybtn);

        swipeLayout.setProgressViewOffset(false, 0, JoysaleApplication.dpToPx(FragmentMainActivity.this, 70));

        // Elements Visibility
        filter_btn.setVisibility(View.VISIBLE);
        search_btn.setVisibility(View.VISIBLE);
        titleImage.setVisibility(View.VISIBLE);
        nullLay.setVisibility(GONE);
        progress.setVisibility(GONE);

        crossIcon = (ImageView) findViewById(R.id.cross_icon);
        crossIcon.setOnClickListener(this);
        crossIcon.setVisibility(GONE);


//        partial_autolocationListAry.add("sdagdfgd");
//        partial_autolocationListAry.add("sdagdfgd");
//        partial_autolocationListAry.add("sdagdfgd");
//        partial_autolocationListAry.add("sdagdfgd");
//        partial_autolocationListAry.add("sdagdfgd");partial_autolocationListAry.add("sdagdfgd");
//        partial_autolocationListAry.add("sdagdfgd");partial_autolocationListAry.add("sdagdfgd");
//        partial_autolocationListAry.add("sdagdfgd");partial_autolocationListAry.add("sdagdfgd");



        autoCompeleteAdapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, partial_autolocationListAry);
        //Getting the instance of AutoCompleteTextView
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        locationTextView = (TextView) findViewById(R.id.LocationTextView);
        locationTextView.setOnClickListener(this);
        actv1 = (EditText) findViewById(R.id.autoCompleteTextView1);
        actv1.setClickable(true);

        if(getIntent().getExtras().containsKey("loc_name")){
            locationTextView.setText(getIntent().getExtras().getString("loc_name"));
            crossIcon.setVisibility(GONE);
        }

        actv.setThreshold(1);//will start working from first character
//        actv.setAdapter(autoCompeleteAdapter);//setting the adapter data into the AutoCompleteTextView
//        actv.addTextChangedListener(this);
        actv.setEnabled(false);
        actv1.addTextChangedListener(this);
        actv1.setEnabled(false);
        actv1.setOnClickListener(this);




        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Typeface font = Typeface.createFromAsset(getAssets(), "font_regular.ttf");
        actv.setTypeface(font);
        imm.hideSoftInputFromWindow(actv1.getWindowToken(), 0);
        actv.clearFocus();
        actv1.clearFocus();

        downArrow.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(View.GONE);
                    actv1.setEnabled(false);
                    downArrow.setScaleY(-1f);
                    imm.hideSoftInputFromWindow(actv1.getWindowToken(),0);
                    crossIcon.setVisibility(GONE);
                }else {
                    locationListLay.setVisibility(View.VISIBLE);
                    actv1.setEnabled(true);
                    imm.showSoftInput(actv1, 1);
                    downArrow.setScaleY(1f);
                }
            }
        });

        locationLay1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(View.GONE);
                    actv.setEnabled(false);
                    actv1.setEnabled(false);
                    downArrow.setScaleY(-1f);
                    imm.hideSoftInputFromWindow(actv1.getWindowToken(),0);
                }else {
                    locationListLay.setVisibility(View.VISIBLE);
                    actv.setEnabled(true);
                    actv1.setEnabled(true);
                    downArrow.setScaleY(1f);
                    imm.showSoftInput(actv1, 0);
                }
            }
        });

        iconLay.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(View.GONE);
                    actv.setEnabled(false);
                    actv1.setEnabled(false);
                    downArrow.setScaleY(-1f);
                    imm.hideSoftInputFromWindow(actv1.getWindowToken(),0);
                }else {
                    locationListLay.setVisibility(View.VISIBLE);
                    actv.setEnabled(true);
                    actv1.setEnabled(true);
                    downArrow.setScaleY(1f);
                    imm.showSoftInput(actv1, 0);
                }
            }
        });

        autoLocationLay.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(View.GONE);
                    actv.setEnabled(false);
                    actv1.setEnabled(false);
                    downArrow.setScaleY(-1f);
                    imm.hideSoftInputFromWindow(actv1.getWindowToken(),0);
                }else {
                    locationListLay.setVisibility(View.VISIBLE);
                    actv.setEnabled(true);
                    actv1.setEnabled(true);
                    downArrow.setScaleY(1f);
                    imm.showSoftInput(actv1, 0);
                }
            }
        });

//        actv1.setOnClickListener(new View.OnClickListener(){
//
//            @Override
//            public void onClick(View view) {
//                if(locationListLay.getVisibility() == View.VISIBLE){
//                    locationListLay.setVisibility(View.GONE);
//                    actv.setEnabled(false);
//                    actv1.setEnabled(false);
//                    downArrow.setScaleY(-1f);
//                    imm.hideSoftInputFromWindow(actv1.getWindowToken(),0);
//                }else {
//                    locationListLay.setVisibility(View.VISIBLE);
//                    actv.setEnabled(true);
//                    actv1.setEnabled(true);
//                    downArrow.setScaleY(1f);
//                    imm.showSoftInput(actv1, 0);
//                }
//            }
//        });

//        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                String selected_loc = actv.getText().toString();
//                int position  = locationListAry.indexOf(selected_loc);
//                String selected_locID = locationListIDAry.get(position);
////                actv.setTag(selected_locID);
////                actv.setText(selected_loc);
//                if(selected_loc != null && !selected_loc.equals(getString(R.string.world_wide))){
//                    lat = Double.valueOf(locationAry.get(position-1).get(Constants.TAG_LOCATION_LAT));
//                    lon = Double.valueOf(locationAry.get(position-1).get(Constants.TAG_LOCATION_LON));
//                }else{
//                    lat = 0.0;
//                    lon = 0.0;
//                }
////                lat = Double.valueOf(locationAry.get(position).get(Constants.TAG_LOCATION_LAT));
////                lon = Double.valueOf(locationAry.get(position).get(Constants.TAG_LOCATION_LON));
//                loadHomeItemList(0);
//                locationListLay.setVisibility(GONE);
//                downArrow.setScaleY(-1f);
//                downArrowCount = 0;
//                prevPosition = i;
//                pulldown = true;
//                initializeHomeUI();
//            }
//        });


        locationList.setSelection(prevPosition);

        locationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.v("FFF","FFF="+adapterView.getItemIdAtPosition(i));
                String selected_loc = String.valueOf(adapterView.getItemAtPosition(i));
                String selected_locID = locationListIDAry.get(i);
//                actv.setTag(selected_locID);
//                actv.setText(selected_loc);
                locationTextView.setText(selected_loc);
                if(selected_loc != null && !selected_loc.equals(getString(R.string.world_wide))){
                    int position  = autolocationListAry.indexOf(selected_loc);
                    Log.v("FFF","FFF1="+position);
                    lat = Double.valueOf(locationAry.get(position-1).get(Constants.TAG_LOCATION_LAT));
                    lon = Double.valueOf(locationAry.get(position-1).get(Constants.TAG_LOCATION_LON));
                }else{
                    lat = 0.0;
                    lon = 0.0;
//                    actv1.setEnabled(false);
                }

                loadHomeItemList(0);
                locationListLay.setVisibility(GONE);
                downArrow.setScaleY(-1f);
                downArrowCount = 0;
                prevPosition = i;
                pulldown = true;
//                actv.dismissDropDown();
                initializeHomeUI();
                imm.hideSoftInputFromWindow(actv1.getWindowToken(),0);
                crossIcon.setVisibility(View.GONE);
            }
        });
        Log.v(TAG,"locationAry="+locationAry);


        locationExtraction("");


//        locationListAdapter.notifyDataSetChanged();



        // Adapter for side menu
        adapter = new ItemAdapter(FragmentMainActivity.this, R.layout.menu_list_item, ids);
        listView.setAdapter(adapter);

        toggle = new ActionBarDrawerToggle(this, drawer, null, R.string.open, R.string.close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.v(TAG, "Drawer Opened");
                if (GetSet.isLogged()) {
                    getCountDetails();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.v(TAG, "Drawer Closed");
                if (mDrawerItemClicked) {
                    mDrawerItemClicked = false;
                    openActivity(Model.GetbyId(Integer.parseInt(ids[mDrawerPosition])).name);
                }
            }
        };

        Constants.filpref = getApplicationContext().getSharedPreferences("FilterPref", MODE_PRIVATE);
        Constants.fileditor = Constants.filpref.edit();

        LocationActivity.location = Constants.filpref.getString(Constants.TAG_LOCATION, getString(R.string.world_wide));
//        LocationActivity.lat = Double.parseDouble(Constants.filpref.getString(Constants.TAG_LAT, "0"));
//        LocationActivity.lon = Double.parseDouble(Constants.filpref.getString(Constants.TAG_LON, "0"));
        LocationActivity.locationRemoved = Constants.filpref.getBoolean("locationRemoved", false);

        if (SearchAdvance.categoryId.size() > 0 || !SearchAdvance.distance.equals("0") || !SearchAdvance.sortBy.equals("1")
                || !SearchAdvance.postedWithin.equals("") || !SearchActivity.searchQuery.equals("")
                || (SearchAdvance.priceMin == 0 || SearchAdvance.priceMax == 0) || (SearchAdvance.zeroMax != 0)) {
            filterAry.clear();

            // To get FilterArray
            getFilterAry();

            Log.v(TAG, "filterAry=" + filterAry);

            //Initialize the adapter
            setFilterAdapter();

            if (filterList.getVisibility() == View.VISIBLE && filterAry.size() == 0) {
                filterList.setVisibility(View.GONE);
                filterView.setVisibility(View.GONE);
            } else {
                filterList.setVisibility(View.VISIBLE);
                filterView.setVisibility(View.VISIBLE);
            }

        } else {
            filterAry.clear();
            filterList.setVisibility(GONE);
            filterView.setVisibility(GONE);
        }

        drawer.setDrawerListener(toggle);
        drawer.post(new Runnable() {
            @Override
            public void run() {
                toggle.syncState();
            }
        });

        Display display = this.getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        screenHalf = (display.getWidth() * 50 / 100) - JoysaleApplication.dpToPx(this, 30);

        //To set Grid Layout manager
        recyclerView.setHasFixedSize(true);
        itemManager = new NpaGridLayoutManager(FragmentMainActivity.this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        //To initialize the adapter
        itemAdapter = new ItemViewAdapter(FragmentMainActivity.this, homeItemList);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
                return false;
            }
        });

        itemManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (itemAdapter.getItemViewType(position) == 0) ? 2 : 1;
            }

        });

        mScrollListener = new GridRecyclerOnScrollListener(itemManager) {
            @Override
            public void onLoadMore(int current_page) {
                if (!swipeLayout.isRefreshing()) {
                    initializeHomeUI();
                    loadHomeItemList(current_page * ITEM_LIMIT);
                    Log.v(TAG, "On offset" + (ITEM_LIMIT * current_page));
                }
            }
        };

        recyclerView.addOnScrollListener(mScrollListener);

        display = this.getWindowManager().getDefaultDisplay();

        float scale = (float) display.getWidth() / Constants.HOME_BANNER_WIDTH;
        bannerHeight = (int) Math.round(Constants.HOME_BANNER_HEIGHT * scale);

        // Elements Listener
        listView.setOnItemClickListener(new DrawerItemClickListener());
        login.setOnClickListener(this);
        filter_btn.setOnClickListener(this);
        search_btn.setOnClickListener(this);
        menu_btn.setOnClickListener(this);
        swipeLayout.setOnRefreshListener(this);
//        locationLay.setOnClickListener(this);
        login.setOnClickListener(this);
        btnAddStuff.setOnClickListener(this);
        headerLay.setOnClickListener(this);
        notifybtn.setOnClickListener(this);

        // For Set Login & Logout State
        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
        if (Constants.pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(Constants.pref.getString(Constants.TAG_USER_ID, null));
            GetSet.setUserName(Constants.pref.getString(Constants.TAG_USERNAME, null));
            GetSet.setEmail(Constants.pref.getString(Constants.TAG_EMAIL, null));
            GetSet.setPassword(Constants.pref.getString(Constants.TAG_PASSWORD, null));
            GetSet.setFullName(Constants.pref.getString(Constants.TAG_FULL_NAME, null));
            GetSet.setImageUrl(Constants.pref.getString(Constants.TAG_PHOTO, null));
            GetSet.setRating(Constants.pref.getString(Constants.TAG_RATING, "0"));
            GetSet.setRatingUserCount(Constants.pref.getString(Constants.TAG_RATING_USER_COUNT, "0"));
            profheader.setVisibility(View.VISIBLE);
            proflogin.setVisibility(GONE);
        } else {
            profheader.setVisibility(GONE);
            proflogin.setVisibility(View.VISIBLE);
        }

        setNavigationUI();

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));

        //To set Location
//        setLocationTxt();

        //To get Home data from Api
        loadData();
//        attachKeyboardListeners();
    }

    public void locationExtraction(String cond){
        locationListIDAry.add(0,"");
        if(cond != null && cond.equalsIgnoreCase("refresh")){
            autolocationListAry.clear();
            locationListIDAry.clear();
            locationListIDAry.add(0,"");
            if(!(autolocationListAry.size()>0)){
                if(locationAry.size()>0){
                    for (int i = 0; i < locationAry.size(); i++) {
                        HashMap<String, String> temp = locationAry.get(i);
                        String location_id = temp.get(Constants.TAG_LOCATION_ID);
                        String location = temp.get(Constants.TAG_LOCATION_REGION)+", "+temp.get(Constants.TAG_LOCATION_CITY)+", "+temp.get(Constants.TAG_LOCATION_COUNTRY);
                        autolocationListAry.add(location);
                        locationListIDAry.add(location_id);
                    }
                }
            }

            partial_autolocationListAry =autolocationListAry;
            locationListAdapter = new ArrayAdapter<String>(FragmentMainActivity.this, android.R.layout.simple_list_item_1, partial_autolocationListAry);
            locationList.setAdapter(locationListAdapter);
        }else{
            if(!(autolocationListAry.size()>0)){
                if(locationAry.size()>0){
                    for (int i = 0; i < locationAry.size(); i++) {
                        HashMap<String, String> temp = locationAry.get(i);
                        String location_id = temp.get(Constants.TAG_LOCATION_ID);
                        String location = temp.get(Constants.TAG_LOCATION_REGION)+", "+temp.get(Constants.TAG_LOCATION_CITY)+", "+temp.get(Constants.TAG_LOCATION_COUNTRY);
                        autolocationListAry.add(location);
                    }
                }
            }

            partial_autolocationListAry =autolocationListAry;
            locationListAdapter = new ArrayAdapter<String>(FragmentMainActivity.this, android.R.layout.simple_list_item_1, partial_autolocationListAry);
            locationList.setAdapter(locationListAdapter);
        }
        if(!locationListAdapter.isEmpty() && !locationListAdapter.getItem(0).equalsIgnoreCase(getString(R.string.world_wide))){
//            if(cond != null && !cond.equalsIgnoreCase("refresh")){
                locationListAdapter.insert(getString(R.string.world_wide),0);
//            }
        }
        if(locationListAdapter.getCount()>6){
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationListLay.getLayoutParams();
            int hght = displayMetrics.heightPixels;
            layoutParams.height=JoysaleApplication.dpToPx(FragmentMainActivity.this, 250);
            locationListLay.setLayoutParams(layoutParams);
        }else{
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationListLay.getLayoutParams();
            int hght = displayMetrics.heightPixels;
            layoutParams.height= RelativeLayout.LayoutParams.WRAP_CONTENT;
            locationListLay.setLayoutParams(layoutParams);
        }

        if(cond != null && !cond.equalsIgnoreCase("refresh")){
            if(locationAry.size()>0){
                for (int i = 0; i < locationAry.size(); i++) {
                    HashMap<String, String> temp = locationAry.get(i);
                    String location_id = temp.get(Constants.TAG_LOCATION_ID);
                    String location = temp.get(Constants.TAG_LOCATION_REGION)+", "+temp.get(Constants.TAG_LOCATION_CITY)+", "+temp.get(Constants.TAG_LOCATION_COUNTRY);
                    locationListAry.add(location);
                    locationListIDAry.add(location_id);
                }
            }
        }
    }

    /**
     * Set a Navigation View UI
     **/

    private void setNavigationUI() {
        if (GetSet.isLogged()) {
            profheader.setVisibility(View.VISIBLE);
            proflogin.setVisibility(GONE);
            username.setText(GetSet.getFullName());
            userid.setText(GetSet.getUserName());
            if (GetSet.getImageUrl() != null && !GetSet.getImageUrl().equals("")) {
                Log.v(TAG, "getImageurl=" + GetSet.getImageUrl());
                Picasso.with(FragmentMainActivity.this).load(GetSet.getImageUrl()).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImage);
            }
        } else {
            profheader.setVisibility(GONE);
            proflogin.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "buynowmodule=" + BUYNOW);
        if (BUYNOW) {
            reviewLay.setVisibility(View.VISIBLE);
            userid.setVisibility(GONE);
            try {
                ratingBar.setRating(Float.parseFloat(GetSet.getRating()));
                if (GetSet.getRatingUserCount().equals("0")) {
                    ratingCount.setText("(0)");
                } else {
                    ratingCount.setText("(" + GetSet.getRatingUserCount() + ")");
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            reviewLay.setVisibility(GONE);
            userid.setVisibility(View.VISIBLE);
        }
    }

    // Load Home page

    private void loadData() {
        if (homeItemList.size() == 0) {
            mScrollListener.resetpagecount();
            initializeHomeUI();
            loadHomeItemList(0);
        } else if (SearchAdvance.applyFilter) {
            SearchAdvance.applyFilter = false;
            swipeRefresh();
            mScrollListener.resetpagecount();
            pulldown = true;
            initializeHomeUI();
            loadHomeItemList(0);
        }
    }


    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    /**
     * function for get the location from gps
     **/

//    private void setLocationTxt() {
//        if (LocationActivity.locationRemoved) {
//            locationTxt.setText(getString(R.string.world_wide));
//        } else if (!LocationActivity.location.equals(getString(R.string.world_wide))) {
//            locationTxt.setText(LocationActivity.location);
//        } else {
//            if (googleApiClient == null) {
////                setUpGClient();
//                locationTxt.setText(getString(R.string.world_wide));
//            } else if (mylocation == null) {
////                getMyLocation();
//                locationTxt.setText(getString(R.string.world_wide));
//            } else {
////                LocationActivity.lat = mylocation.getLatitude();
////                LocationActivity.lon = mylocation.getLongitude();
////                Log.v(TAG, "lat = " + LocationActivity.lat + "&lon=" + LocationActivity.lon);
//
////                refreshLocation();
//            }
//        }
//    }

    private void refreshLocation() {
        try {
            new GetLocationAsync(LocationActivity.lat, LocationActivity.lon).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        mScrollListener.resetpagecount();
        pulldown = true;
        if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
            initializeHomeUI();
            loadHomeItemList(0);
        }
    }

    /**
     * function for get the applied filters to Ary
     **/
    private void getFilterAry() {
        if (SearchAdvance.categoryId.size() > 0) {
            for (int i = 0; i < SearchAdvance.categoryId.size(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(Constants.TAG_TYPE, "category");
                map.put(Constants.NAME, SearchAdvance.categoryName.get(i));
                map.put(Constants.CATEGORYID, SearchAdvance.categoryId.get(i));

                filterAry.add(map);
            }
        }

        if (!SearchAdvance.distance.equals("0")) {
            String distanceType = JoysaleApplication.adminPref.getString(Constants.PREF_DISTANCE_TYPE, "km");
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, "distance");
            if (distanceType.equalsIgnoreCase("km")) {
                map.put(Constants.NAME, getString(R.string.within) + " " + SearchAdvance.distance + " " + getString(R.string.kilometers));
            } else {
                map.put(Constants.NAME, getString(R.string.within) + " " + SearchAdvance.distance + " " + getString(R.string.miles));
            }
            filterAry.add(map);
        }

        if (!SearchAdvance.postedWithin.equals("")) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, "postedWithin");
            map.put(Constants.NAME, SearchAdvance.postedTxt);
            filterAry.add(map);
        }

        if (SearchAdvance.zeroMax == 0 && SearchAdvance.priceMin == 0 && SearchAdvance.priceMax ==0) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, Constants.TAG_GIVING_AWAY);
            map.put(Constants.NAME, "Giving Away");
            filterAry.add(map);
        } else if (SearchAdvance.priceMin != 0 || SearchAdvance.priceMax != 0) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, Constants.TAG_PRICE);
            map.put(Constants.NAME, String.valueOf(SearchAdvance.priceMin) + "-" + String.valueOf(SearchAdvance.priceMax));
            filterAry.add(map);
            Log.v(TAG, "PRICEEE" + map);
        }

        if (!SearchAdvance.sortBy.equals("1")) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, "sortBy");
            map.put(Constants.NAME, SearchAdvance.sortTxt);
            filterAry.add(map);
        }
        if (!SearchActivity.searchQuery.equals("")) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_TYPE, "search");
            map.put(Constants.NAME, SearchActivity.searchQuery);
            filterAry.add(map);
        }
    }

   //To initialize the adapter
    private void setFilterAdapter() {
        Log.v(TAG, "filterAry=" + filterAry);

        filterAdapter = new FilterAdapter(FragmentMainActivity.this, filterAry);
        filterList.setAdapter(filterAdapter);
    }

    private RecyclerItemClickListener categoryItemClick(Context context, RecyclerView recyclerView) {

        RecyclerItemClickListener recyclerItemClickListener = new RecyclerItemClickListener(context, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                if (categoryFlag) {
                    if (!SearchAdvance.categoryId.contains(categoryAry.get(position).get(Constants.TAG_CATEGORYID))) {
                        SearchAdvance.categoryId.add(categoryAry.get(position).get(Constants.TAG_CATEGORYID));
                        SearchAdvance.categoryName.add(categoryAry.get(position).get(Constants.TAG_CATEGORYNAME));

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(Constants.TAG_TYPE, "category");
                        map.put(Constants.NAME, categoryAry.get(position).get(Constants.TAG_CATEGORYNAME));
                        map.put(Constants.CATEGORYID, categoryAry.get(position).get(Constants.TAG_CATEGORYID));
                        map.put(Constants.LANG_TYPE,AppUtils.getCurrentLanguageCode(FragmentMainActivity.this));

                        filterAry.add(map);

                        setFilterAdapter();

                        filterList.setVisibility(View.VISIBLE);
                        filterView.setVisibility(View.VISIBLE);
                        swipeRefresh();
                        mScrollListener.resetpagecount();
                        pulldown = true;
                        categoryFlag = false;
                        if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                            initializeHomeUI();
                            loadHomeItemList(0);
                        }
                    }
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        });

        return recyclerItemClickListener;
    }

    private void enableDisableSwipeRefresh(boolean enabled) {
        if (enabled) {
            swipeLayout.setEnabled(true);
        } else {
            swipeLayout.setEnabled(false);
        }
    }

    /**
     * function for open the corresponding activity from sliding menu
     **/

    public void openActivity(String from) {
        Log.v(TAG, "from=" + from);
        if (from.equals(getString(R.string.sell_your_stuff))) {
            if (GetSet.isLogged()) {
                /*Clear previously added images*/
                Intent m = new Intent(FragmentMainActivity.this, CameraActivity.class);
                m.putExtra(Constants.FROM, "home");
                startActivity(m);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
            }
        } else if (from.equals(getString(R.string.chat))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, MessageActivity.class);
                startActivity(i);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
            }
        } else if (from.equals(getString(R.string.categories))) {
            Intent c = new Intent(FragmentMainActivity.this, CategoryActivity.class);
            startActivity(c);
            if(locationListLay.getVisibility() == View.VISIBLE){
                locationListLay.setVisibility(GONE);
                downArrow.setScaleY(-1f);
                actv1.setEnabled(false);
            }
        } else if (from.equals(getString(R.string.myprofile))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, Profile.class);
                i.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                startActivity(i);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
            }
        } else if (from.equals(getString(R.string.myorders_sales))) {
           /* if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, MySalesnOrder.class);
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
            }*/
        } else if (from.equals(getString(R.string.myexchange))) {
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, ExchangeActivity.class);
                startActivity(i);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
            }
        } else if (from.equals(getString(R.string.my_promotions))) {
            if(locationListLay.getVisibility() == View.VISIBLE){
                locationListLay.setVisibility(GONE);
                downArrow.setScaleY(-1f);
                actv1.setEnabled(false);
            }
            if (GetSet.isLogged()) {
                Intent i = new Intent(FragmentMainActivity.this, MyPromotions.class);
                startActivity(i);
            } else {
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
            }
        } else if (from.equals(getString(R.string.invite_friends))) {
            if(locationListLay.getVisibility() == View.VISIBLE){
                locationListLay.setVisibility(GONE);
                downArrow.setScaleY(-1f);
                actv1.setEnabled(false);
            }
            inviteDialog();
        } else if (from.equals(getString(R.string.help))) {
            if(locationListLay.getVisibility() == View.VISIBLE){
                locationListLay.setVisibility(GONE);
                downArrow.setScaleY(-1f);
                actv1.setEnabled(false);
            }
            Intent Hl = new Intent(FragmentMainActivity.this, Help.class);
            startActivity(Hl);
        }
    }

    public void inviteDialog() {
        inviteDialog = new Dialog(FragmentMainActivity.this);
        inviteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        inviteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        inviteDialog.setContentView(R.layout.invite_dialog);

        inviteDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = inviteDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
//        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        inviteDialog.setCancelable(true);
        inviteDialog.setCanceledOnTouchOutside(false);

        RelativeLayout fblay, whatsapplay, emaillay;
        fblay = (RelativeLayout) inviteDialog.findViewById(R.id.fbLay);
        whatsapplay = (RelativeLayout) inviteDialog.findViewById(R.id.whatsaplay);
        emaillay = (RelativeLayout) inviteDialog.findViewById(R.id.emaillay);

        final String inviteContent = getString(R.string.invite_content) + " " + "https://play.google.com/store/apps/details?id=" +
                getApplicationContext().getPackageName();

        fblay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.facebook.orca");
                if (installed) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.facebook.orca");
                    startActivity(sendIntent);
                    inviteDialog.dismiss();
                    inviteDialog.cancel();
                } else {
                    Toast.makeText(FragmentMainActivity.this, "Facebook Messenger is not currently installed on your phone", Toast.LENGTH_SHORT).show();
                }
            }
        });
        whatsapplay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.whatsapp");
                if (installed) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.whatsapp");
                    startActivity(sendIntent);
                    inviteDialog.dismiss();
                    inviteDialog.cancel();
                } else {
                    Toast.makeText(FragmentMainActivity.this, "Whatsapp is not currently installed on your phone", Toast.LENGTH_SHORT).show();
                }
            }
        });

        emaillay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean installed = appInstalledOrNot("com.google.android.gm");
                if (installed) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/html");
                    sendIntent.setPackage("com.google.android.gm");
                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{});
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + "!!! " + getString(R.string.invite_subject));
                    sendIntent.putExtra(Intent.EXTRA_TEXT, inviteContent);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(sendIntent);
                    inviteDialog.dismiss();
                    inviteDialog.cancel();
                } else {
                    Toast.makeText(FragmentMainActivity.this, "Gmail is not currently installed on your phone", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (!inviteDialog.isShowing()) {
            inviteDialog.show();
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    @Override
    public void onBackPressed() {
        if(locationListLay.getVisibility() == View.VISIBLE){
            locationListLay.setVisibility(View.GONE);
            downArrow.setScaleY(-1f);
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            //moveTaskToBack(true);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                FragmentMainActivity.this.finishAffinity();
                            } else {
                                moveTaskToBack(true);
                                FragmentMainActivity.this.finish();
                                //ActivityCompat.finishAffinity(FragmentChangeActivity.this);
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.reallyExit))
                    .setPositiveButton(getResources().getString(R.string.exit),
                            dialogClickListener)
                    .setNegativeButton(getResources().getString(R.string.keep),
                            dialogClickListener).show();
        } else {
            super.onBackPressed();
        }
    }

//    private synchronized void setUpGClient() {
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, 0, this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        googleApiClient.connect();
//    }

    private void checkPermissions() {
        int permissionLocation = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        } else {
//            getMyLocation();
        }
    }

//    @Override
//    public void onLocationChanged(Location location) {
//        mylocation = location;
//        Log.e("location lat","- >"+mylocation.getLatitude());
//        Log.e("location lon","- >"+mylocation.getLongitude());
//
//        Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(mylocation.getLatitude()));
//        Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(mylocation.getLongitude()));
//
//        if (LocationActivity.lat == 0 && LocationActivity.lon == 0 && mylocation != null) {
//            LocationActivity.lat = mylocation.getLatitude();
//            LocationActivity.lon = mylocation.getLongitude();
//            Log.v(TAG, "lat = " + LocationActivity.lat + "&lon=" + LocationActivity.lon);
//            refreshLocation();
//        } else if (!LocationActivity.location.equals(getString(R.string.world_wide))) {
//            locationTxt.setText(LocationActivity.location);
//        }
//        if (googleApiClient.isConnected())
//            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
//    }

//    @Override
//    public void onConnected(Bundle bundle) {
//        checkPermissions();
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//
//    }

//    private void getMyLocation() {
//        if (googleApiClient != null) {
//            if (googleApiClient.isConnected()) {
//                int permissionLocation = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
//                        ACCESS_FINE_LOCATION);
//                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
//                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//                    LocationRequest locationRequest = new LocationRequest();
//                    locationRequest.setInterval(3000);
//                    locationRequest.setFastestInterval(3000);
//                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                            .addLocationRequest(locationRequest);
//                    builder.setAlwaysShow(true);
//                    LocationServices.FusedLocationApi
//                            .requestLocationUpdates(googleApiClient, locationRequest, FragmentMainActivity.this);
//                    PendingResult result =
//                            LocationServices.SettingsApi
//                                    .checkLocationSettings(googleApiClient, builder.build());
//                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//
//                        @Override
//                        public void onResult(LocationSettingsResult result) {
//                            final Status status = result.getStatus();
//                            switch (status.getStatusCode()) {
//                                case LocationSettingsStatusCodes.SUCCESS:
//                                    // All location settings are satisfied.
//                                    // You can initialize location requests here.
//                                    int permissionLocation = ContextCompat
//                                            .checkSelfPermission(FragmentMainActivity.this,
//                                                    ACCESS_FINE_LOCATION);
//                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
//                                        mylocation = LocationServices.FusedLocationApi
//                                                .getLastLocation(googleApiClient);
//                                        Log.v(TAG, "mylocation=" + mylocation);
//                                    }
//                                    break;
//                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                                    // Location settings are not satisfied.
//                                    // But could be fixed by showing the user a dialog.
//                                    try {
//                                        // Show the dialog by calling startResolutionForResult(),
//                                        // and check the result in onActivityResult().
//                                        // Ask to turn on GPS automatically
//                                        status.startResolutionForResult(FragmentMainActivity.this,
//                                                REQUEST_CHECK_SETTINGS_GPS);
//                                    } catch (IntentSender.SendIntentException e) {
//                                        // Ignore the error.
//                                    }
//                                    break;
//                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                                    // Location settings are not satisfied. However, we have no way to fix the
//                                    // settings so we won't show the dialog.
//                                    //finish();
//                                    break;
//                            }
//                        }
//                    });
//                }
//            }
//        }
//    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
//                setLocationTxt();
                pulldown = true;
                mScrollListener.resetpagecount();
                if(actv1.getText().toString() == null || actv1.getText().toString().equals("")){
                    lat = 0.0;
                    lon = 0.0;
                }
                initializeHomeUI();
                getAdminDatas();
                loadHomeItemList(0);
                if (GetSet.isLogged()) {
                    getCountDetails();
                }
            } else {
                swipeLayout.setRefreshing(false);
            }
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
//                        getMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:
//                        locationTxt.setText(getString(R.string.world_wide));
                        break;
                }
                break;
        }
    }

    /**
     * Function for get the user profile information
     **/
    private void getProfileInformation() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PROFILE, new Response.Listener<String>() {

            @Override
            public void onResponse(String json) {
                Log.v(TAG, "getProfileInformationRes=" + json);
                try {
                    JSONObject obj = new JSONObject(json);
                    String response = DefensiveClass.optString(obj, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        JSONObject result = obj.optJSONObject("result");
                        if (!(result == null)) {
                            if (DefensiveClass.optString(result, Constants.TAG_USERID).equalsIgnoreCase(GetSet.getUserId())) {
                                Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                                        MODE_PRIVATE);
                                Constants.editor = Constants.pref.edit();
                                Constants.editor.putString(Constants.TAG_PHOTO, DefensiveClass.optString(result, Constants.TAG_USERIMG));
                                Constants.editor.putString(Constants.TAG_USERNAME, DefensiveClass.optString(result, Constants.TAG_USERNAME));
                                Constants.editor.putString(Constants.TAG_FULL_NAME, DefensiveClass.optString(result, Constants.TAG_FULL_NAME));
                                Constants.editor.putString(Constants.TAG_RATING, DefensiveClass.optString(result, Constants.TAG_RATING));
                                Constants.editor.putString(Constants.TAG_RATING_USER_COUNT, DefensiveClass.optString(result, Constants.TAG_RATING_USER_COUNT));
                                Constants.editor.commit();

                                GetSet.setImageUrl(Constants.pref.getString(Constants.TAG_PHOTO, null));
                                GetSet.setUserName(Constants.pref.getString(Constants.TAG_USERNAME, null));
                                GetSet.setFullName(Constants.pref.getString(Constants.TAG_FULL_NAME, null));
                                GetSet.setRating(Constants.pref.getString(Constants.TAG_RATING, null));
                                GetSet.setRatingUserCount(Constants.pref.getString(Constants.TAG_RATING_USER_COUNT, null));

                                setNavigationUI();

                                Log.v(TAG, "userimage" + DefensiveClass.optString(result, "user_img"));
                            }
                        }
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

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        prevPosition = 0;
        JoysaleApplication.unregisterReceiver(FragmentMainActivity.this);
        super.onPause();
        //viewPager.stopAutoScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume" + homeItemList.size());
        // For Internet checking
        JoysaleApplication.registerReceiver(FragmentMainActivity.this);

        if (GetSet.isLogged()) {
            getCountDetails();
            getProfileInformation();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG, "requestCode=" + requestCode);
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            int permissionLocation = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                    ACCESS_FINE_LOCATION);
            if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
//                getMyLocation();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            int permissionCamera = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                    CAMERA);
            int permissionStorage = ContextCompat.checkSelfPermission(FragmentMainActivity.this,
                    WRITE_EXTERNAL_STORAGE);

            if (permissionCamera == PackageManager.PERMISSION_GRANTED &&
                    permissionStorage == PackageManager.PERMISSION_GRANTED) {
                /*Clear previously added images*/
                Intent m = new Intent(FragmentMainActivity.this, CameraActivity.class);
                m.putExtra(Constants.FROM, "home");
                startActivity(m);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
            }
        }
    }

    // Load home page Api
    private void loadHomeItemList(final int pageCount) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_HOME, new Response.Listener<String>() {
            @Override
            public void onResponse(final String json) {
                Log.v(TAG, "homeitemListRes=" + json);
                FragmentMainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                        ItemsParsing parse = new ItemsParsing(FragmentMainActivity.this, GetSet.getUserId());
                        temp.addAll(parse.parsing(json));
                        if (mScrollListener != null && temp.size() >= ITEM_LIMIT) {
                            mScrollListener.setLoading(false);
                        }
                        if (!homeItemList.contains(temp)) {
                            homeItemList.addAll(temp);
                        }
                        Log.v(TAG, "homeItemList=" + homeItemList);
                    }
                });
                if (pulldown) {
                    pulldown = false;
                }
                swipeLayout.setRefreshing(false);
                progress.setVisibility(GONE);
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.stopScroll();
                        itemAdapter.notifyDataSetChanged();
                    }
                });

                if (homeItemList.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(GONE);
                } else {
                    nullLay.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                categoryFlag = true;
                filterFlag = true;

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
                map.put(Constants.TAG_TYPE, "search");
                map.put(Constants.LANG_TYPE, AppUtils.getCurrentLanguageCode(FragmentMainActivity.this));
                Log.v(TAG, "LocationActivity.lat=" + lat);
                Log.v(TAG, "LocationActivity.lon=" + lon);
                if (lat != null && lat != 0.0) {
//                    map.put(Constants.TAG_LAT, Double.toString(LocationActivity.lat));
                    map.put(Constants.TAG_LAT, String.valueOf(lat));
                } else {
                    map.put(Constants.TAG_LAT, "");
                }
                if (lon != null && lon != 0.0) {
//                    map.put(Constants.TAG_LAT, Double.toString(LocationActivity.lat));
                    map.put(Constants.TAG_LON, String.valueOf(lon));
                } else {
                    map.put(Constants.TAG_LON, "");
                }
                if (!SearchAdvance.distance.equals("0")) {
                    map.put(Constants.TAG_DISTANCE, SearchAdvance.distance);
                    String distanceType = JoysaleApplication.adminPref.getString(Constants.PREF_DISTANCE_TYPE, "km");
                    map.put(Constants.TAG_DISTANCE_TYPE, distanceType);
                }
                if (!SearchAdvance.sortBy.equals("")) {
                    map.put(Constants.TAG_SORTING_ID, SearchAdvance.sortBy);
                }
                if (!SearchAdvance.postedWithin.equals("") && !SearchAdvance.postedWithin.equals("all")) {
                    map.put(Constants.TAG_POSTED_WITHIN, SearchAdvance.postedWithin);
                }
                if (!SearchActivity.searchQuery.equals("")) {
                    map.put(Constants.TAG_SEARCH_KEY, SearchActivity.searchQuery);
                }
                if (SearchAdvance.categoryId.size() > 0) {
                    ArrayList<String> main = new ArrayList<String>();
                    ArrayList<String> sub = new ArrayList<String>();
                    for (int i = 0; i < SearchAdvance.categoryId.size(); i++) {
                        String subc = SearchAdvance.subcategoryId.get(SearchAdvance.categoryId.get(i));
                        if (subc == null || subc.equals("") || subc.equals("all")) {
                            main.add(SearchAdvance.categoryId.get(i));
                        } else {
                            sub.add(subc);
                        }
                    }
                    if (main.size() > 0) {
                        map.put(Constants.TAG_CATEGORYID, main.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
                    }

                    if (sub.size() > 0) {
                        map.put(Constants.TAG_SUBCATEGORY_ID, sub.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
                    }
                }
                if ((SearchAdvance.priceMin != 0 || SearchAdvance.priceMax != 0) || SearchAdvance.zeroMax == 0) {
                    map.put("price", String.valueOf(SearchAdvance.priceMin) + "-" + String.valueOf(SearchAdvance.priceMax));
                }

                map.put(Constants.TAG_OFFSET, String.valueOf(pageCount));
                map.put(Constants.TAG_LIMIT, String.valueOf(ITEM_LIMIT));
                if (GetSet.isLogged()) {
                    map.put(Constants.TAG_USERID, GetSet.getUserId());
                }
                Log.v(TAG, "homeitemListParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }


    // home items
    private void initializeHomeUI() {
        if (mScrollListener != null) {
            mScrollListener.setLoading(true);
        }
        nullLay.setVisibility(View.GONE);
        if (pulldown) {
            homeItemList.clear();
            itemAdapter.notifyDataSetChanged();
            progress.setVisibility(GONE);
            swipeRefresh();
        } else if (homeItemList.size() > 0) {
            progress.setVisibility(GONE);
            swipeRefresh();
        } else {
            progress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Function for getting notification and chat badge count
     **/
    private void getCountDetails() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_COUNT_DETAILS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                Log.v(TAG, "getCountDetailsRes=" + res);
                try {
                    JSONObject jobj = new JSONObject(res);

                    if (jobj.getString(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        JSONObject result = jobj.getJSONObject(Constants.TAG_RESULT);
                        String notificationCount = DefensiveClass.optString(result, Constants.TAG_NOTIFICATION_COUNT);
                        chatCount = DefensiveClass.optString(result, Constants.TAG_CHAT_COUNT);

                        if (!notificationCount.equals("0") && !notificationCount.equals("")) {
                            notifyBadge.setText(notificationCount);
                            notifyBadge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                            notifyBadge.setBadgeMargin(7);
                            notifyBadge.setTextSize(12);
                            notifyBadge.setGravity(Gravity.CENTER);
                            notifyBadge.show();
                        } else {
                            notifyBadge.hide();
                        }

                        if (!chatCount.equals("")) {
                            adapter.notifyDataSetChanged();
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
                Log.v(TAG, "getCountDetailsParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() > 0) {
            crossIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() > 0) {
            FragmentMainActivity.this.locationListAdapter.getFilter().filter(charSequence, new Filter.FilterListener() {
                @Override
                public void onFilterComplete(int i) {
                    if(i>6){
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationListLay.getLayoutParams();
                        int hght = displayMetrics.heightPixels;
                        layoutParams.height=JoysaleApplication.dpToPx(FragmentMainActivity.this, 250);
                        locationListLay.setLayoutParams(layoutParams);
                    }else{
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationListLay.getLayoutParams();
                        int hght = displayMetrics.heightPixels;
                        layoutParams.height= RelativeLayout.LayoutParams.WRAP_CONTENT;
                        locationListLay.setLayoutParams(layoutParams);
                    }
                }
            });
            crossIcon.setVisibility(View.VISIBLE);
        } else {
            FragmentMainActivity.this.locationListAdapter.getFilter().filter("",new Filter.FilterListener() {
                @Override
                public void onFilterComplete(int i) {
                    if(i>6){
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationListLay.getLayoutParams();
                        int hght = displayMetrics.heightPixels;
                        layoutParams.height=JoysaleApplication.dpToPx(FragmentMainActivity.this, 250);
                        locationListLay.setLayoutParams(layoutParams);
                    }else{
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationListLay.getLayoutParams();
                        int hght = displayMetrics.heightPixels;
                        layoutParams.height= RelativeLayout.LayoutParams.WRAP_CONTENT;
                        locationListLay.setLayoutParams(layoutParams);
                    }
                }
            });
//            partial_autolocationListAry =autolocationListAry;
            locationListAdapter.notifyDataSetChanged();
            crossIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private static class NpaGridLayoutManager extends GridLayoutManager {
        public NpaGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public NpaGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        public NpaGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }

    /**
     * Adapter for Home Items
     **/
    public class ItemViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        ArrayList<HashMap<String, String>> itemList;
        Context mContext;

        public ItemViewAdapter(Context ctx, ArrayList<HashMap<String, String>> itemList) {
            this.itemList = itemList;
            this.mContext = ctx;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.home_list_items, parent, false);
                return new MyViewHolder(itemView);
            } else if (viewType == TYPE_HEADER) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.home_banner, parent, false);
                return new HeaderView(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            try {
                if (viewHolder instanceof MyViewHolder) {
                    MyViewHolder holder = (MyViewHolder) viewHolder;

                    if (position % 2 == 0) {
                        holder.mainLay.setPadding(JoysaleApplication.dpToPx(mContext, 5), JoysaleApplication.dpToPx(mContext, 10), JoysaleApplication.dpToPx(mContext, 10), 0);
                    } else {
                        holder.mainLay.setPadding(JoysaleApplication.dpToPx(mContext, 10), JoysaleApplication.dpToPx(mContext, 10), JoysaleApplication.dpToPx(mContext, 5), 0);
                    }

                    final HashMap<String, String> tempMap = itemList.get(position - 1);

                    Picasso.with(FragmentMainActivity.this).load(tempMap.get(Constants.TAG_ITEM_URL_350)).into(holder.singleImage);
                    holder.itemName.setText(tempMap.get(Constants.TAG_TITLE).trim());
                    if (tempMap.get(Constants.TAG_PRICE).equals("0")) {
                        holder.itemPrice.setText(getResources().getString(R.string.giving_away));
                        holder.itemPrice.setTextColor(getResources().getColor(R.color.colorPrimary));
                    } else {
                        holder.itemPrice.setText(tempMap.get(Constants.TAG_PRICE) + " "
                                + tempMap.get(Constants.TAG_CURRENCY_SYM));
                        holder.itemPrice.setTextColor(getResources().getColor(R.color.primaryText));
                    }
                    holder.location.setText(tempMap.get(Constants.TAG_LOCATION));

                    if (tempMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")) {
                        holder.productType.setVisibility(View.VISIBLE);
                        holder.productType.setText(getString(R.string.sold));
                        holder.productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.soldbg));
                    } else {
                        if (Constants.PROMOTION) {
                            if (tempMap.get(Constants.TAG_PROMOTION_TYPE).equalsIgnoreCase("Ad")) {
                                holder.productType.setVisibility(View.VISIBLE);
                                holder.productType.setText(getString(R.string.ad));
                                holder.productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.adbg));
                            } else if (tempMap.get(Constants.TAG_PROMOTION_TYPE).equalsIgnoreCase("Urgent")) {
                                holder.productType.setVisibility(View.VISIBLE);
                                holder.productType.setText(getString(R.string.urgent));
                                holder.productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.urgentbg));
                            } else {
                                holder.productType.setVisibility(GONE);
                            }
                        } else {
                            holder.productType.setVisibility(GONE);
                        }
                    }

                    long timestamp = 0;
                  String time = tempMap.get(Constants.TAG_POSTED_TIME);

                    if (time != null) {
                        timestamp = Long.parseLong(time) * 1000;

                    }
                    TimeAgo timeAgo = new TimeAgo(mContext);
//                    holder.postedTime.setText(timeAgo.timeAgo(timestamp));
                    holder.postedTime.setText(timeAgo.timeAgo(timestamp));
                    Log.v("time", "time=" + timeAgo.timeAgo(timestamp));
                } else if (viewHolder instanceof HeaderView) {
                    HeaderView holder = (HeaderView) viewHolder;
                    Log.v("header", "header");

                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return itemList.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position))
                return TYPE_HEADER;

            return TYPE_ITEM;
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
            ImageView singleImage;
            TextView itemPrice, itemName, location, postedTime,servertime, productType;
            RelativeLayout imageLay;
            LinearLayout mainLay;

            public MyViewHolder(View view) {
                super(view);

                singleImage = (ImageView) view.findViewById(R.id.singleImage);
                itemPrice = (TextView) view.findViewById(R.id.priceText);
                itemName = (TextView) view.findViewById(R.id.itemName);
                productType = (TextView) view.findViewById(R.id.productType);
                location = (TextView) view.findViewById(R.id.location);
                postedTime = (TextView) view.findViewById(R.id.postedTime);
                imageLay = (RelativeLayout) view.findViewById(R.id.imageLay);
                mainLay = (LinearLayout) view.findViewById(R.id.mainLay);

                singleImage.getLayoutParams().height = screenHalf;
                imageLay.getLayoutParams().height = screenHalf;

                singleImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.singleImage:
                        try {
                            if (homeItemList.size() > 0) {
                                Intent i = new Intent(FragmentMainActivity.this,
                                        DetailActivity.class);
                                i.putExtra("data", homeItemList.get(getAdapterPosition() - 1));
                                i.putExtra("position", (getAdapterPosition() - 1));
                                i.putExtra(Constants.FROM, "home");
                                startActivity(i);
                                if(locationListLay.getVisibility() == View.VISIBLE){
                                    locationListLay.setVisibility(GONE);
                                    downArrow.setScaleY(-1f);
                                    actv1.setEnabled(false);
                                }
                            }
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.mainLay:
                        if(locationListLay.getVisibility() == View.VISIBLE){
                            locationListLay.setVisibility(GONE);
                            downArrow.setScaleY(-1f);
                            actv1.setEnabled(false);
                        }
                        break;
                    case R.id.itemName:
                        if(locationListLay.getVisibility() == View.VISIBLE){
                            locationListLay.setVisibility(GONE);
                            downArrow.setScaleY(-1f);
                            actv1.setEnabled(false);
                        }
                        break;
                    case R.id.location:
                        if(locationListLay.getVisibility() == View.VISIBLE){
                            locationListLay.setVisibility(GONE);
                            downArrow.setScaleY(-1f);
                            actv1.setEnabled(false);
                        }
                        break;
                    case R.id.priceText:
                        if(locationListLay.getVisibility() == View.VISIBLE){
                            locationListLay.setVisibility(GONE);
                            downArrow.setScaleY(-1f);
                            actv1.setEnabled(false);
                        }
                        break;
                }
            }
        }

        public class HeaderView extends RecyclerView.ViewHolder {

            AutoScrollViewPager viewPager;
            LinePageIndicator pageIndicator;
            RecyclerView categoryView;

            public HeaderView(View itemView) {
                super(itemView);

                viewPager = (AutoScrollViewPager) itemView.findViewById(R.id.view_pager);
                pageIndicator = (LinePageIndicator) itemView.findViewById(R.id.indicator);
                categoryView = (RecyclerView) itemView.findViewById(R.id.categoryView);

                categoryView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if(locationListLay.getVisibility() == View.VISIBLE){
                            locationListLay.setVisibility(GONE);
                            downArrow.setScaleY(-1f);
                            actv1.setEnabled(false);
                        }
                        return false;
                    }
                });

                showheaderContent();
            }

            public void showheaderContent() {
                if (homeBanner.equalsIgnoreCase("enable") && bannerAry.size() > 0) {
                    viewPager.setVisibility(View.VISIBLE);
                    pageIndicator.setVisibility(View.VISIBLE);
                } else {
                    viewPager.setVisibility(View.GONE);
                    pageIndicator.setVisibility(View.INVISIBLE);
                    ViewGroup.MarginLayoutParams marginLayoutParams =
                            (ViewGroup.MarginLayoutParams) categoryView.getLayoutParams();
                    marginLayoutParams.setMargins(0, JoysaleApplication.dpToPx(getApplicationContext(), 30), 0, 0);
                    categoryView.setLayoutParams(marginLayoutParams);
                }

                if (categoryAry.size() > 0) {
                    categoryView.setVisibility(View.VISIBLE);
                } else {
                    categoryView.setVisibility(View.GONE);
                }

                viewPager.getLayoutParams().height = bannerHeight;

                BannerPagerAdapter pagerAdapter = new BannerPagerAdapter(mContext, bannerAry);
                viewPager.setAdapter(pagerAdapter);
                pageIndicator.setViewPager(viewPager);

                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                    @Override
                    public void onPageScrollStateChanged(int state) {
                        enableDisableSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE);
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {

                    }

                    @Override
                    public void onPageSelected(int position) {

                    }
                });

                viewPager.startAutoScroll(1000);

                final float MILLISECONDS_PER_INCH = 500f; /*Large amount = slow speed*/
                LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
                    }
                };

                LinearLayoutManager layoutManager = new LinearLayoutManager(FragmentMainActivity.this, LinearLayoutManager.HORIZONTAL, false);
                categoryView.setLayoutManager(layoutManager);
//                categoryView.setLayoutManager(new SpeedyLinearLayoutManager(getApplicationContext(), SpeedyLinearLayoutManager.HORIZONTAL, false));
                categoryView.post(new Runnable() {
                    @Override
                    public void run() {
                        categoryView.setAdapter(new RecyclerViewAdapter(categoryAry));
                    }
                });
                categoryView.setHasFixedSize(true);
                categoryView.setNestedScrollingEnabled(false);
                categoryView.addOnItemTouchListener(categoryItemClick(FragmentMainActivity.this, categoryView));

            }
        }
    }

    /**
     * adapter for showing the applied filters
     **/
    public class FilterAdapter extends BaseAdapter {

        ArrayList<HashMap<String, String>> datas;
        ViewHolder holder = null;
        Context mContext;

        public FilterAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            datas = data;
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
            final LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.home_filter_item, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.crossIcon = (ImageView) convertView.findViewById(R.id.cross_icon);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                holder.name.setText(datas.get(position).get("name"));

                holder.crossIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(locationListLay.getVisibility() == View.VISIBLE){
                            locationListLay.setVisibility(GONE);
                            downArrow.setScaleY(-1f);
                            actv1.setEnabled(false);
                        }
                        switch (datas.get(position).get(Constants.TAG_TYPE)) {
                            case "category":
                                if (SearchAdvance.categoryId.size() > 0) {
                                    SearchAdvance.categoryId.remove(datas.get(position).get("categoryId"));
                                    SearchAdvance.categoryName.remove(datas.get(position).get("name"));
                                    SearchAdvance.subcategoryId.remove(datas.get(position).get("categoryId"));
                                }
                                break;
                            case "distance":
                                SearchAdvance.distance = "0";
                                SearchAdvance.distanceX = 0;
                                break;
                            case "postedWithin":
                                SearchAdvance.postedWithin = "";
                                break;
                            case "sortBy":
                                SearchAdvance.sortBy = "1";
                                break;
                            case "search":
                                SearchActivity.searchQuery = "";
                                break;
                            case "price":
                                SearchAdvance.priceMin = 0;
                                SearchAdvance.priceMax = 0;
                                SearchAdvance.storePriceMin = 0;
                                SearchAdvance.storePriceMax = 0;
                                break;
                            case Constants.TAG_GIVING_AWAY:
                                SearchAdvance.zeroMax = 1;
                                SearchAdvance.priceMin = 0;
                                SearchAdvance.priceMax = 0;
                                break;
                        }
                        if (filterFlag) {
                            filterFlag = false;
                            filterAry.remove(position);
                            filterAdapter.notifyDataSetChanged();
                            swipeRefresh();
                            mScrollListener.resetpagecount();
                            pulldown = true;
                            if (JoysaleApplication.isNetworkAvailable(FragmentMainActivity.this)) {
                                initializeHomeUI();
                                loadHomeItemList(0);
                            }
                            if (filterAry.size() == 0) {
                                filterList.setVisibility(GONE);
                                filterView.setVisibility(GONE);
                            }
                            Log.v(TAG, "filterAry" + filterAry);
                            Log.v(TAG, "categoryId" + SearchAdvance.categoryId);
                            Log.v(TAG, "categoryName" + SearchAdvance.categoryName);
                            Log.v(TAG, "subcategoryId" + SearchAdvance.subcategoryId);
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
            ImageView crossIcon;
            TextView name;
        }
    }

    /**
     * Adapter for Category
     **/
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> itemList;

        public RecyclerViewAdapter(ArrayList<HashMap<String, String>> itemList) {
            this.itemList = itemList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.category_single_view, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final HashMap<String, String> tempMap = itemList.get(position);
            Picasso.with(FragmentMainActivity.this).load(tempMap.get("category_img")).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.singleImage);
            holder.singleTitle.setText(tempMap.get("category_name"));
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView singleImage;
            TextView singleTitle;
            LinearLayout singleLayout;

            public MyViewHolder(View view) {
                super(view);
                singleLayout = (LinearLayout) view.findViewById(R.id.singleLayout);
                singleImage = (ImageView) view.findViewById(R.id.singleImage);
                singleTitle = (TextView) view.findViewById(R.id.singleTitle);

                /*Set the category last item partially visible */
                Display display = getWindowManager().getDefaultDisplay();
                // display size in pixels
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                width = width / 10;
                width = width + width;
                int height = size.y;
                LinearLayout.LayoutParams oldParams = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                oldParams.setMargins(JoysaleApplication.dpToPx(getApplicationContext(), 5), JoysaleApplication.dpToPx(getApplicationContext(), 10), 0, 0);
                singleLayout.setLayoutParams(oldParams);
            }
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            drawer.closeDrawers();
            mDrawerItemClicked = true;
            mDrawerPosition = position;
        }
    }

    /**
     * Adapter for showing banner image
     **/
    class BannerPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater inflater;
        ArrayList<HashMap<String, String>> data;

        public BannerPagerAdapter(Context act, ArrayList<HashMap<String, String>> newary) {
            this.data = newary;
            this.context = act;
        }

        public int getCount() {
            return data.size();
        }

        public Object instantiateItem(ViewGroup collection, final int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.banner_image,
                    collection, false);

            ImageView image = (ImageView) itemView.findViewById(R.id.image);
            String img = data.get(position).get("image");
            //Log.v("banner img", "img=" + img);
            if (!img.equals("")) {
                Picasso.with(FragmentMainActivity.this).load(img).into(image);
            }

            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Patterns.WEB_URL.matcher(data.get(position).get("url")).matches()) {
                        try {
                            Intent b = new Intent(Intent.ACTION_VIEW, Uri.parse(data.get(position).get("url")));
                            startActivity(b);
                            if (inviteDialog != null) {
                                inviteDialog.cancel();
                            }
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(FragmentMainActivity.this, getString(R.string.url_invalid), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            ((ViewPager) collection).addView(itemView, 0);
            return itemView;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    /**
     * class for get the address from lat, lon
     **/
    private class GetLocationAsync extends AsyncTask<String, Void, String> {
        // boolean duplicateResponse;
        double x, y;

        public GetLocationAsync(double latitude, double longitude) {
            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            addresses = JoysaleApplication.getLocationFromLatLng(FragmentMainActivity.this, "home", x, y);
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (addresses.size() > 0) {
                    LocationActivity.location = addresses.get(0).getAddressLine(0)
                            + addresses.get(0).getAddressLine(1) + " ";
//                    locationTxt.setText(LocationActivity.location);

                    Constants.fileditor.putString(Constants.TAG_LOCATION, LocationActivity.location);
                    Constants.fileditor.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    /**
     * Function for OnClick Event
     **/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                drawer.closeDrawers();
                Intent i = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                startActivity(i);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
                break;
            case R.id.homefilterbtn:
                Intent j = new Intent(FragmentMainActivity.this, SearchAdvance.class);
                startActivity(j);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
                break;
            case R.id.menubtn:
                drawer.openDrawer(Gravity.START);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
                break;
            case R.id.locationLay:
                Intent k = new Intent(FragmentMainActivity.this, LocationActivity.class);
                k.putExtra(Constants.FROM, "home");
                startActivity(k);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
                break;
            case R.id.searchbtn:
                Intent l = new Intent(FragmentMainActivity.this, SearchActivity.class);
                startActivity(l);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
                break;
            case R.id.btnAddStuff:
                if (GetSet.isLogged()) {
                    if(locationListLay.getVisibility() == View.VISIBLE){
                        locationListLay.setVisibility(GONE);
                        downArrow.setScaleY(-1f);
                        actv1.setEnabled(false);
                    }
                    if (ContextCompat.checkSelfPermission(FragmentMainActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(FragmentMainActivity.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(FragmentMainActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                    } else if (ContextCompat.checkSelfPermission(FragmentMainActivity.this, CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(FragmentMainActivity.this, new String[]{CAMERA}, REQUEST_CAMERA_PERMISSION);
                    } else if (ContextCompat.checkSelfPermission(FragmentMainActivity.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(FragmentMainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                    } else {
                        Intent m = new Intent(FragmentMainActivity.this, CameraActivity.class);
                        /*Clear previously added images*/
                        m.putExtra(Constants.FROM, "home");
                        startActivity(m);
                    }
                } else {
                    if(locationListLay.getVisibility() == View.VISIBLE){
                        locationListLay.setVisibility(GONE);
                        downArrow.setScaleY(-1f);
                        actv1.setEnabled(false);
                    }
                    Intent m = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                    startActivity(m);
                }
                break;
            case R.id.header:
                drawer.closeDrawers();
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
                if (GetSet.isLogged()) {
                    Intent n = new Intent(FragmentMainActivity.this, Profile.class);
                    n.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                    startActivity(n);
                } else {
                    Intent n = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                    startActivity(n);
                }
                break;
            case R.id.notifybtn:
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(GONE);
                    downArrow.setScaleY(-1f);
                    actv1.setEnabled(false);
                }
                if (GetSet.isLogged()) {
                    Intent o = new Intent(FragmentMainActivity.this, Notification.class);
                    startActivity(o);
                } else {
                    Intent n = new Intent(FragmentMainActivity.this, WelcomeActivity.class);
                    startActivity(n);
                }
                break;
            case R.id.cross_icon:
                actv.setText("");
                actv1.setText("");
                crossIcon.setVisibility(View.GONE);
                if(locationListLay.getVisibility() == View.VISIBLE){
                    imm.showSoftInput(actv1, 1);
                    actv1.setEnabled(true);
                }else {
                    actv1.setEnabled(false);
                }
                break;
            case R.id.LocationTextView:
                if(locationListLay.getVisibility() == View.VISIBLE){
                    locationListLay.setVisibility(View.GONE);
                    actv.setEnabled(false);
                    actv1.setEnabled(false);
                    downArrow.setScaleY(-1f);
                    actv1.setVisibility(View.GONE);
                    actv1.setEnabled(false);
                    imm.hideSoftInputFromWindow(actv1.getWindowToken(),0);
                }else {
                    locationListLay.setVisibility(View.VISIBLE);
                    actv.setEnabled(true);
                    actv1.setEnabled(true);
                    downArrow.setScaleY(1f);
                    actv1.setVisibility(View.VISIBLE);
                    actv1.setEnabled(true);
                    imm.showSoftInput(actv1, 0);
                }
                break;
        }
    }

    private void getAdminDatas() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ADMIN_DATAS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    Log.v(TAG, "Get Admin Response=" + String.valueOf(json));

                    if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        JSONObject result = json.optJSONObject(Constants.TAG_RESULT);

                        FragmentMainActivity.homeBanner = DefensiveClass.optString(result, Constants.TAG_BANNER);
                        JoysaleApplication.adminEditor.putString(Constants.PREF_DISTANCE_TYPE, DefensiveClass.optString(result, Constants.TAG_DISTANCE_TYPE));
                        JoysaleApplication.adminEditor.commit();
                        /*if (DefensiveClass.optString(result, Constants.TAG_BUYNOW).equalsIgnoreCase("enable")) {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_BUYNOW, true);
                            JoysaleApplication.adminEditor.commit();
                            Constants.BUYNOW = true;
                        } else {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_BUYNOW, false);
                            JoysaleApplication.adminEditor.commit();
                            Constants.BUYNOW = false;
                        }*/

                        if (DefensiveClass.optString(result, Constants.TAG_EXCHANGE).equalsIgnoreCase("enable")) {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EXCHANGE, true);
                            JoysaleApplication.adminEditor.commit();
                            Constants.EXCHANGE = true;
                        } else {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_EXCHANGE, false);
                            JoysaleApplication.adminEditor.commit();
                            Constants.EXCHANGE = false;
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

                        if (DefensiveClass.optString(result, Constants.TAG_GIVING_AWAY).equalsIgnoreCase("enable")) {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_GIVINGAWAY, true);
                            JoysaleApplication.adminEditor.commit();
                            Constants.GIVINGAWAY = true;
                        } else {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_GIVINGAWAY, false);
                            JoysaleApplication.adminEditor.commit();
                            Constants.GIVINGAWAY = false;
                        }

                        JSONArray bannerAry = result.optJSONArray(Constants.TAG_BANNER_DATA);
                        if (bannerAry != null) {
                            for (int i = 0; i < bannerAry.length(); i++) {
                                JSONObject temp = bannerAry.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(Constants.KEY_IMAGE, DefensiveClass.optString(temp, Constants.TAG_BANNER_IMAGE));
                                map.put(Constants.KEY_URL, DefensiveClass.optString(temp, Constants.TAG_BANNER_URL));
                                FragmentMainActivity.bannerAry.add(map);
                            }
                        }

                        JSONArray categoryAry = result.optJSONArray("category");
                        if (categoryAry != null) {
                            if (FragmentMainActivity.categoryAry != null) {
                                FragmentMainActivity.categoryAry.clear();
                            }
                            for (int i = 0; i < categoryAry.length(); i++) {
                                JSONObject temp = categoryAry.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(Constants.TAG_CATEGORYID, DefensiveClass.optString(temp, Constants.TAG_CATEGORYID));
                                map.put(Constants.TAG_CATEGORYNAME, DefensiveClass.optString(temp, Constants.TAG_CATEGORYNAME));
                                map.put(Constants.TAG_CATEGORYIMG, DefensiveClass.optString(temp, Constants.TAG_CATEGORYIMG).replace("resized/40", "resized/150"));
                                map.put(Constants.LANG_TYPE,AppUtils.getCurrentLanguageCode(FragmentMainActivity.this));
                                FragmentMainActivity.categoryAry.add(map);
                            }
                        }

                        JSONArray locationAry = result.optJSONArray("locations");
                        if (categoryAry != null) {
                            if (FragmentMainActivity.locationAry != null) {
                                FragmentMainActivity.locationAry.clear();
                            }
                            for (int i = 0; i < locationAry.length(); i++) {
                                JSONObject temp = locationAry.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(Constants.TAG_LOCATION_ID, DefensiveClass.optString(temp, Constants.TAG_LOCATION_ID));
                                map.put(Constants.TAG_LOCATION_REGION, DefensiveClass.optString(temp, Constants.TAG_LOCATION_REGION));
                                map.put(Constants.TAG_LOCATION_CITY, DefensiveClass.optString(temp, Constants.TAG_LOCATION_CITY));
                                map.put(Constants.TAG_LOCATION_COUNTRY,DefensiveClass.optString(temp, Constants.TAG_LOCATION_COUNTRY));
                                map.put(Constants.TAG_LOCATION_LAT, DefensiveClass.optString(temp, Constants.TAG_LOCATION_LAT));
                                map.put(Constants.TAG_LOCATION_LON,DefensiveClass.optString(temp, Constants.TAG_LOCATION_LON));
                                FragmentMainActivity.locationAry.add(map);
                            }
                        }

                        locationExtraction("refresh");

                        JSONArray templateAry = result.optJSONArray(Constants.TAG_CHAT_TEMPLATE);
                        if (templateAry != null) {
                            for (int i = 0; i < templateAry.length(); i++) {
                                JSONObject temp = templateAry.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put(Constants.NAME, DefensiveClass.optString(temp, Constants.NAME));
                                ChatActivity.templatMsgAry.add(map);
                            }
                        }


                    }
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
                map.put(Constants.LANG_TYPE, "en");
                Log.v(TAG, "Get Admin Params=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }


}