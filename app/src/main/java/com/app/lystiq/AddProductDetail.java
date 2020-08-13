package com.app.lystiq;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.external.GPSTracker;
import com.app.external.HorizontalListView;
import com.app.helper.OnButtonClick;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.DeviceShareDialog;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AddProductDetail extends AppCompatActivity implements View.OnClickListener {

    // Widget Declaration
    public static LinearLayout bottomLay, instantLay, buynowLay;
    public static RelativeLayout exchangeLay, offerLay, conditionLay,facebookLay;
    public static TextView itemCondition, location, category;
    public static ImageView condArrow, locArrow, catArrow;
    public static SwitchCompat chatSwitch, exchangeSwitch, buySwitch, givingAwaySwitch,facebookSwitch;
    ImageView backBtn, cancelIcon;
    TextView title, cancel, post, share ,promote, alert_title, uploadStatus, successText;
    EditText productName, productDes, price, paypalId, shippingFee;
    Spinner currency;
    ProgressBar loadingProgress;
    HorizontalListView imageList;
    LinearLayout parentLay, saveLay, priceLay;
    RelativeLayout uploadSuccessLay, imageLoadingLay, locationLay, categoryLay, buyLay, givingLay;
    AVLoadingIndicatorView loadingView, postProgress;
    CallbackManager mCallbackManager;
    ShareDialog shareDialog;

    public static AddProductDetail activity;
    public ImagesAdapter imagesAdapter;
    ArrayAdapter currencyadapter;
    Dialog dialog;


    public ArrayList<HashMap<String, Object>> images = new ArrayList<HashMap<String, Object>>();
    static ArrayList<String> removeAry = new ArrayList<String>();
    public static HashMap<String, String> itemMap = new HashMap<String, String>();
    ArrayList<String> pathsAry = new ArrayList<String>();
    ArrayList<HashMap<String, String>> categAry = new ArrayList<HashMap<String, String>>();
    ArrayList<ArrayList<HashMap<String, String>>> subcategAry = new ArrayList<ArrayList<HashMap<String, String>>>();
    ArrayList<HashMap<String, String>> conditionAry = new ArrayList<HashMap<String, String>>();
    ArrayList<String> uploadedImage = new ArrayList<String>();
    private ArrayList<String> currencyID = new ArrayList<String>();
    private ArrayList<String> currencyspin = new ArrayList<String>();
    private ArrayList<String> currencyspin_api = new ArrayList<String>();
    private ArrayList<String> countryId = new ArrayList<String>();
    private ArrayList<String> countryName = new ArrayList<String>();
    private ArrayList<String> countryCode = new ArrayList<String>();

    // Variable Declaration
    public static final int ACTION_EDIT = 300;
    private static final int ADD_IMAGES = 306;
    static final String TAG = "AddProductDetail";
    public static String itemCond = "", loc = "", categId = "", subcategId = "", mcountryId = "";
    public static double lat, lon;
    boolean isValidPrice = false;
    String from = "", currencyid = "", productUrl = "", postPrice = "", posteditemId = "";
    int count;
    private static int prevPosition;
    String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private FacebookCallback<Sharer.Result> shareCallback =
            new FacebookCallback<Sharer.Result>() {
                @Override
                public void onCancel() {
                    Log.d("HelloFacebook", "Canceled");
                    loadingView.setVisibility(View.GONE);
                    dialog.show();
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
                    loadingView.setVisibility(View.GONE);
                }

                @Override
                public void onSuccess(Sharer.Result result) {
                    Log.d("HelloFacebook", "Success!");
                    //Toast.makeText(AddProductDetail.this, "Product published on facebook", Toast.LENGTH_SHORT).show();
                    loadingView.setVisibility(View.GONE);
                    dialog.show();
                    if (result.getPostId() != null) {
                        String id = result.getPostId();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addproduct_detail);

        activity = this;

        // Elements Initialization
        backBtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        cancel = (TextView) findViewById(R.id.cancel);
        post = (TextView) findViewById(R.id.post);
        productName = (EditText) findViewById(R.id.productName);
        productDes = (EditText) findViewById(R.id.productDes);
        price = (EditText) findViewById(R.id.price);
        currency = (Spinner) findViewById(R.id.currency);
        chatSwitch = (SwitchCompat) findViewById(R.id.chatSwitch);
        exchangeSwitch = (SwitchCompat) findViewById(R.id.exchangeSwitch);
        facebookSwitch = (SwitchCompat) findViewById(R.id.facebookSwitch);
        //twitterSwitch = (SwitchCompat) findViewById(R.id.twitterSwitch);
        imageList = (HorizontalListView) findViewById(R.id.imageList);
        parentLay = (LinearLayout) findViewById(R.id.parentLay);
        saveLay = (LinearLayout) findViewById(R.id.saveLay);
        loadingView = (AVLoadingIndicatorView) findViewById(R.id.progress);
        catArrow = (ImageView) findViewById(R.id.catArrow);
        condArrow = (ImageView) findViewById(R.id.condArrow);
        categoryLay = (RelativeLayout) findViewById(R.id.categoryLay);
        conditionLay = (RelativeLayout) findViewById(R.id.conditionLay);
        locationLay = (RelativeLayout) findViewById(R.id.locationLay);
        exchangeLay = (RelativeLayout) findViewById(R.id.exchangeLay);
        offerLay = (RelativeLayout) findViewById(R.id.offerLay);
        location = (TextView) findViewById(R.id.location);
        itemCondition = (TextView) findViewById(R.id.itemCondition);
        category = (TextView) findViewById(R.id.category);
        locArrow = (ImageView) findViewById(R.id.locArrow);
        bottomLay = (LinearLayout) findViewById(R.id.bottomLay);
        instantLay = (LinearLayout) findViewById(R.id.instantLay);
        buySwitch = (SwitchCompat) findViewById(R.id.buySwitch);
        paypalId = (EditText) findViewById(R.id.paypalId);
        shippingFee = (EditText) findViewById(R.id.shippingFee);
        buynowLay = (LinearLayout) findViewById(R.id.buynowLay);
        givingAwaySwitch = (SwitchCompat) findViewById(R.id.givingAwaySwitch);
        priceLay = (LinearLayout) findViewById(R.id.priceLay);
        buyLay = (RelativeLayout) findViewById(R.id.buyLay);
        givingLay = (RelativeLayout) findViewById(R.id.givingLay);
        facebookLay = (RelativeLayout)findViewById(R.id.facebookLay);

        mCallbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        title.setText(getString(R.string.add_your_stuff));
        from = getIntent().getExtras().getString("from");

        if (getIntent().getStringExtra("data") != null) {
            try {
                images.clear();
                itemMap.put(Constants.TAG_PHOTOS, "" + new JSONArray(getIntent().getStringExtra("data")));

                JSONArray photos = new JSONArray(itemMap.get(Constants.TAG_PHOTOS));

                for (int i = 0; i < photos.length(); i++) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    JSONObject jph = photos.getJSONObject(i);
                    Log.e(TAG, "onCreate: " + jph);
                    String imageurl = "";
                    if (jph.has(Constants.TAG_ITEM_URL_350)) {
                        imageurl = DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_350);
                        map.put(Constants.TAG_TYPE, Constants.KEY_URL);
                    } else if (jph.has(Constants.TAG_TYPE)) {
                        imageurl = DefensiveClass.optString(jph, Constants.KEY_IMAGE);
                        map.put(Constants.TAG_TYPE, DefensiveClass.optString(jph, Constants.TAG_TYPE));
                    }

                    String fileName = imageurl.substring(imageurl.lastIndexOf('/') + 1, imageurl.length());
//                        if (!isImgRemoved(fileName)) {
                    map.put("image", imageurl);
                    if (!images.contains(map)) {
                        images.add(map);
                    }
//                        }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Elements visibility
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        //Elements set listener
        backBtn.setOnClickListener(this);
        cancel.setOnClickListener(this);
        post.setOnClickListener(this);
        conditionLay.setOnClickListener(this);
        locationLay.setOnClickListener(this);
        categoryLay.setOnClickListener(this);

        //productName.addTextChangedListener(new addListenerOnTextChange(this, productName));
        productDes.addTextChangedListener(new addListenerForDes(this, productDes));
        price.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        shippingFee.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        if (JoysaleApplication.isRTL(AddProductDetail.this)) {
            isValidPrice = true;
            price.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            shippingFee.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        } else {
            price.setFilters(new InputFilter[]{new JoysaleApplication.DecimalDigitsInputFilter(Constants.NO_OF_DIGIT_BEFORE_DECIMAL, Constants.NO_OF_DIGIT_AFTER_DECIMAL)});
            shippingFee.setFilters(new InputFilter[]{new JoysaleApplication.DecimalDigitsInputFilter(Constants.NO_OF_DIGIT_BEFORE_DECIMAL, Constants.NO_OF_DIGIT_AFTER_DECIMAL)});
        }
        productName.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(70)});
        productDes.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER});

        price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals(".")){
                    price.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        shippingFee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals(".")){
                    shippingFee.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        parentLay.setVisibility(View.GONE);
        saveLay.setVisibility(View.GONE);

        if (Constants.GIVINGAWAY) {
            givingLay.setVisibility(View.VISIBLE);
        } else {
            givingLay.setVisibility(View.GONE);
            if (offerLay.getVisibility() == View.GONE) {
                offerLay.setVisibility(View.VISIBLE);
            }
        }

        getCategories();

        if (!from.equals("edit")) {
            setLocationTxt();
            setImageAdapter();
        }

        loadingView.setVisibility(View.VISIBLE);

        JoysaleApplication.setupUI(AddProductDetail.this, parentLay);

        // Init Dialog
        dialog = new Dialog(AddProductDetail.this, R.style.PostDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.product_upload_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        promote = (TextView) dialog.findViewById(R.id.promote);
       // share = (TextView) dialog.findViewById(R.id.share);
        alert_title = (TextView) dialog.findViewById(R.id.alert_title);
        uploadStatus = (TextView) dialog.findViewById(R.id.uploadStatus);
        cancelIcon = (ImageView) dialog.findViewById(R.id.cancelIcon);
        loadingProgress = (ProgressBar) dialog.findViewById(R.id.loadingProgress);
        postProgress = (AVLoadingIndicatorView) dialog.findViewById(R.id.postProgress);
        uploadSuccessLay = (RelativeLayout) dialog.findViewById(R.id.uploadSuccessLay);
        imageLoadingLay = (RelativeLayout) dialog.findViewById(R.id.imageLoadingLay);
        successText = (TextView) dialog.findViewById(R.id.success_txt);


        //Dialog elements listener
        promote.setOnClickListener(this);
       // share.setOnClickListener(this);
        cancelIcon.setOnClickListener(this);


        givingAwaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    priceLay.setVisibility(View.GONE);
                    offerLay.setVisibility(View.GONE);
                    if (buySwitch.isChecked()) {
                        buynowLay.setVisibility(View.GONE);
                        buyLay.setVisibility(View.GONE);
                        buySwitch.setChecked(false);
                    } else {
                        buyLay.setVisibility(View.GONE);
                    }
                } else {
                    priceLay.setVisibility(View.VISIBLE);
                    buyLay.setVisibility(View.VISIBLE);
                    offerLay.setVisibility(View.VISIBLE);
                    if (buySwitch.isChecked()) {
                        buySwitch.setChecked(true);
                        buynowLay.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        buySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buynowLay.setVisibility(View.VISIBLE);
                } else {
                    buynowLay.setVisibility(View.GONE);
                }
            }
        });

        if (JoysaleApplication.isRTL(AddProductDetail.this)) {
            catArrow.setRotation(180);
            condArrow.setRotation(180);
            locArrow.setRotation(180);
            location.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            category.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            itemCondition.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            catArrow.setRotation(0);
            condArrow.setRotation(0);
            locArrow.setRotation(0);
            location.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            category.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            itemCondition.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }
    }


    /**
     * Function for set current location from Gps
     **/
    private void setLocationTxt() {
        GPSTracker gps = new GPSTracker(AddProductDetail.this);
        if (lat == 0 && lon == 0) {
            if (gps.canGetLocation()) {
                if (JoysaleApplication.isNetworkAvailable(AddProductDetail.this)) {
                    lat = gps.getLatitude();
                    lon = gps.getLongitude();
                    Log.v("lati", "lat" + lat);
                    Log.v("longi", "longi" + lon);
                    new GetLocationAsync(lat, lon).execute();
                }
            }
        }
    }

    /**
     * Function for set already edited datas
     **/
    private void setEditProducts() {
        try {
            itemMap.clear();
            itemMap = (HashMap<String, String>) getIntent().getExtras().get("data");
            Log.v("itemMap", "itemMap" + itemMap);
            productName.setText(itemMap.get(Constants.TAG_TITLE));
            //    productName.setSelection(itemMap.get(Constants.TAG_TITLE).length() - 1);
            productDes.setText(Html.fromHtml(itemMap.get(Constants.TAG_ITEM_DES)));
            price.setText(itemMap.get(Constants.TAG_PRICE));
            Log.v(TAG, "" + Constants.GIVINGAWAY);
            if (Constants.GIVINGAWAY && itemMap.get(Constants.TAG_PRICE).equals("0")) {
                price.setText("");
                givingAwaySwitch.setChecked(true);
                if (offerLay.getVisibility() == View.VISIBLE) {
                    offerLay.setVisibility(View.GONE);
                }
            } else if (!Constants.GIVINGAWAY && itemMap.get(Constants.TAG_PRICE).equals("0")) {
                givingLay.setVisibility(View.VISIBLE);
                price.setText("");
                givingAwaySwitch.setChecked(true);
                if (offerLay.getVisibility() == View.VISIBLE) {
                    offerLay.setVisibility(View.GONE);
                }
            } else if (!Constants.GIVINGAWAY && !itemMap.get(Constants.TAG_PRICE).equals("0")) {
                givingLay.setVisibility(View.GONE);
                givingAwaySwitch.setChecked(false);
                if (offerLay.getVisibility() == View.GONE) {
                    offerLay.setVisibility(View.VISIBLE);
                }
            } else {
                if (givingLay.getVisibility() == View.GONE) {
                    givingLay.setVisibility(View.VISIBLE);
                }
                givingAwaySwitch.setChecked(false);
                if (offerLay.getVisibility() == View.GONE) {
                    offerLay.setVisibility(View.VISIBLE);
                }
            }
            category.setText(itemMap.get(Constants.TAG_CATEGORYNAME));
            category.setTextColor(getResources().getColor(R.color.primaryText));
            catArrow.setColorFilter(getResources().getColor(R.color.primaryText));
            categId = itemMap.get(Constants.TAG_CATEGORYID);
            subcategId = itemMap.get(Constants.TAG_SUBCATEGORYID);
            location.setText(itemMap.get(Constants.TAG_LOCATION));
            location.setTextColor(getResources().getColor(R.color.primaryText));
            locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
            loc = itemMap.get(Constants.TAG_LOCATION);
            try {
                lat = Double.parseDouble(itemMap.get(Constants.TAG_LATITUDE));
                lon = Double.parseDouble(itemMap.get(Constants.TAG_LONGITUDE));
            } catch (NullPointerException | NumberFormatException e) {
                e.printStackTrace();
                lat = 0;
                lon = 0;
            } catch (Exception e) {
                e.printStackTrace();
                lat = 0;
                lon = 0;
            }
            if (itemMap.get(Constants.TAG_EXCHANGE_BUY).equals("1")) {
                exchangeSwitch.setChecked(true);
            } else {
                exchangeSwitch.setChecked(false);
            }
            if (itemMap.get(Constants.TAG_MAKE_OFFER).equals("1")) {
                chatSwitch.setChecked(true);
            } else {
                chatSwitch.setChecked(false);
            }
            if (itemMap.get(Constants.TAG_INSTANT_BUY).equals("1")) {
                buySwitch.setChecked(true);
            } else {
                buySwitch.setChecked(false);
            }


            itemCond = itemMap.get(Constants.TAG_ITEM_CONDITION);
            if (itemMap.get(Constants.TAG_ITEM_CONDITION).equals("0")) {
                itemCond = "";
            }
            itemCondition.setText(itemCond);
            itemCondition.setTextColor(getResources().getColor(R.color.primaryText));
            condArrow.setColorFilter(getResources().getColor(R.color.primaryText));
            paypalId.setText(itemMap.get(Constants.TAG_PAYPALID));
            shippingFee.setText(itemMap.get(Constants.TAG_SHIPPING_COST));
            currency.setSelection(getIndex(currency, itemMap.get(Constants.TAG_CURRENCY_CODE)));
            selected_locID = itemMap.get(Constants.TAG_LOCATION_ID1);
            } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * For setting  product condition depends on category
     **/
    private void setCategoryConditions() {
        bottomLay.setVisibility(View.GONE);
        instantLay.setVisibility(View.GONE);
        Log.v("categoryId", "categoryId=" + categId);
        if (categAry.size() > 0) {
            for (int i = 0; i < categAry.size(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map = categAry.get(i);
                if (map.get("id").equals(categId)) {
                    if (map.get("product_condition").equals("disable") && map.get("exchange_buy").equals("disable")
                            && map.get("make_offer").equals("disable")) {
                        bottomLay.setVisibility(View.GONE);
                        conditionLay.setVisibility(View.GONE);
                        exchangeLay.setVisibility(View.GONE);
                        facebookLay.setVisibility(View.GONE);
                        offerLay.setVisibility(View.GONE);
                    } else {
                        bottomLay.setVisibility(View.VISIBLE);
                        if (map.get("product_condition").equals("enable")) {
                            conditionLay.setVisibility(View.VISIBLE);
                        } else {
                            conditionLay.setVisibility(View.GONE);
                        }

                        if (map.get("exchange_buy").equals("enable")) {
                            if (Constants.EXCHANGE) {
                                exchangeLay.setVisibility(View.VISIBLE);
                            } else {
                                exchangeLay.setVisibility(View.GONE);
                            }
                        } else {
                            exchangeLay.setVisibility(View.GONE);
                        }
                    }

                    if (map.get("instant_buy").equals("disable")) {
                        instantLay.setVisibility(View.GONE);
                    } else {
                        if (Constants.BUYNOW) {
                            instantLay.setVisibility(View.VISIBLE);
                            if (buySwitch.isChecked()) {
                                buynowLay.setVisibility(View.VISIBLE);
                            } else {
                                buynowLay.setVisibility(View.GONE);
                            }
                        } else {
                            instantLay.setVisibility(View.GONE);
                        }
                    }
                    break;
                }
            }
        }
    }


    private int getIndex(Spinner spinner, String myString) {

        int index = 0;
        if (myString.contains("-")) {
            String cur[] = myString.split("-");
            myString = cur[1] + "-" + cur[0];
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        Log.v("index spin=" + spinner.getCount(), "index=" + myString + "==" + index);
        return index;
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            from = data.getExtras().getString("from");
            itemMap = (HashMap<String, String>)data.getExtras().get("data");
        }
    }*/


    /**
     * For set images to listview
     **/
    private void setImageAdapter() {
        try {
            if (from.equals("edit")) {
                if (itemMap.get(Constants.TAG_PHOTOS).equals("") || itemMap.get(Constants.TAG_PHOTOS) == null) {
                    Log.v("photosss", "photos emptyyyy");
                } else {
                    images.clear();
                    if (imagesAdapter != null) {
                        imagesAdapter.notifyDataSetChanged();
                    }
                    JSONArray photos = new JSONArray(itemMap.get(Constants.TAG_PHOTOS));
                    for (int i = 0; i < photos.length(); i++) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        JSONObject jph = photos.getJSONObject(i);
//                        Log.e(TAG, "setImageAdapter: " + jph);
                        String imageurl = "";
                        if (jph.has(Constants.TAG_ITEM_URL_350)) {
                            imageurl = DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_350);
                            map.put(Constants.TAG_TYPE, Constants.KEY_URL);
                        } else if (jph.has(Constants.TAG_TYPE)) {
                            imageurl = DefensiveClass.optString(jph, Constants.KEY_IMAGE);
                            map.put(Constants.TAG_TYPE, DefensiveClass.optString(jph, Constants.TAG_TYPE));
                        }

                        String fileName = imageurl.substring(imageurl.lastIndexOf('/') + 1, imageurl.length());
//                        if (!isImgRemoved(fileName)) {
                        map.put("image", imageurl);
                        if (!images.contains(map)) {
                            images.add(map);
                        }
//                        }
                    }
//                    images.addAll(CameraActivity.images);
                    Log.d("imageFromEdit", images + " ");
//                    CameraActivity.images.clear();
                    if (images.size() < 10) {
                        addPlusIcon();
                    }
                    if (imagesAdapter == null) {
                        imagesAdapter = new ImagesAdapter(AddProductDetail.this, images);
                        imageList.setAdapter(imagesAdapter);
                        imagesAdapter.notifyDataSetChanged();
                    } else {
                        imagesAdapter.notifyDataSetChanged();
                    }
                }
            } else {

                images.clear();
                if (imagesAdapter != null) {
                    imagesAdapter.notifyDataSetChanged();
                }
                JSONArray photos = new JSONArray(itemMap.get(Constants.TAG_PHOTOS));
                for (int i = 0; i < photos.length(); i++) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    JSONObject jph = photos.getJSONObject(i);
                    String imageurl = "";
                    if (jph.has(Constants.TAG_ITEM_URL_350)) {
                        imageurl = DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_350);
                        map.put(Constants.TAG_TYPE, Constants.KEY_URL);
                    } else if (jph.has(Constants.TAG_TYPE)) {
                        imageurl = DefensiveClass.optString(jph, Constants.KEY_IMAGE);
                        map.put(Constants.TAG_TYPE, DefensiveClass.optString(jph, Constants.TAG_TYPE));
                    }

                    map.put("image", imageurl);
                    if (!images.contains(map)) {
                        images.add(map);
                    }
                }

                if (images.size() < 10) {
                    addPlusIcon();
                }

                if (imagesAdapter == null) {
                    imagesAdapter = new ImagesAdapter(AddProductDetail.this, images);
                    imageList.setAdapter(imagesAdapter);
                    imagesAdapter.notifyDataSetChanged();
                } else {
                    imagesAdapter.notifyDataSetChanged();
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


    private void addPlusIcon() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("type", "add");
        if (images.contains(map)) {
            images.remove(map);
        }
        images.add(map);
    }


    /*Check Image is Already Removed or not*/
    private boolean isImgRemoved(String fileName) {
        boolean isPresent = false;
        for (int i = 0; i < removeAry.size(); i++) {
            if (removeAry.get(i).equals(fileName)) {
                isPresent = true;
            } else {
                isPresent = false;
            }
        }
        return isPresent;
    }


    /**
     * function for update the captured image
     **/
    public File saveBitmapToFile(File file) {
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 2;
            o.inPreferredConfig = Bitmap.Config.RGB_565;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 1024;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPreferredConfig = Bitmap.Config.RGB_565;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file

            //file.createNewFile();
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/" + getString(R.string.app_name));
            dir.mkdirs();
            file = new File(dir, String.valueOf(System.currentTimeMillis()) + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            galleryAddPic(file.toString());
            outputStream.flush();
            outputStream.close();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void galleryAddPic(String file) {
        File f = new File(file);
        Uri contentUri = Uri.fromFile(f);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        sendBroadcast(mediaScanIntent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode , resultCode , data);
        if (resultCode == Activity.RESULT_OK) {
//            Log.i(TAG, "onActivityResult: " + requestCode + "," + resultCode);
            if (requestCode == ACTION_EDIT) {
                itemMap.put(Constants.TAG_PHOTOS, data.getStringExtra("data"));
                Log.i(TAG, "onActivityResult: " + itemMap);
                setImageAdapter();
            } else if (requestCode == ADD_IMAGES) {
                Log.i(TAG, "onActivityResult: " + data.getStringExtra("data"));
                itemMap.put(Constants.TAG_PHOTOS, data.getStringExtra("data"));
                setImageAdapter();
            }
        }
    }

    private void setListener(){
        facebookSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    ShareLinkContent content = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse("https://www.lystiq.com/media/item/1441/191597094184.jpg")).build();
                    if (ShareDialog.canShow(ShareLinkContent.class)){
                        shareDialog.show(content);
                    }
                }
            }
        });
    }

    public void addProduct(final String name, final String des) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_POST_PRODUCT, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject jonj = new JSONObject(result);
                    if (jonj.getString(Constants.TAG_STATUS).equalsIgnoreCase(
                            "true")) {
                        productUrl = DefensiveClass.optString(jonj, Constants.TAG_PRODUCT_URL);
                        posteditemId = DefensiveClass.optString(jonj, Constants.TAG_ITEM_ID);
                        uploadSuccessLay.setVisibility(View.VISIBLE);
                        imageLoadingLay.setVisibility(View.INVISIBLE);
                        if (DefensiveClass.optString(jonj, Constants.TAG_MESSAGE).equalsIgnoreCase("Product waiting for admin approval")) {
                            successText.setText(getString(R.string.product_waiting_for_admin_approval));
                        } else {
                            //successText.setText(getString(R.string.successfully_posted));
                            SearchAdvance.applyFilter = true;

                            if (facebookLay.getVisibility() == View.VISIBLE) {
                                if (facebookSwitch.isChecked()) {
                                   // successText.setText(getString(R.string.successfully_posted));
                                    String appUrl = "https://www.lystiq.com/?id="+posteditemId;
                                    shareDialog.registerCallback(mCallbackManager , shareCallback);
                                    ShareLinkContent content = new ShareLinkContent.Builder()
                                            .setContentUrl(Uri.parse(appUrl)).build();
                                    if (ShareDialog.canShow(ShareLinkContent.class)){
                                        shareDialog.show(content);
                                    }
                                }
                            }

                           /* if (twitterLay.getVisibility() == View.VISIBLE) {
                                if (twitterSwitch.isChecked()) {

                                } else {

                                }
                            }**/
                        }
//                        CameraActivity.images.clear();
                        images.clear();
                        if (DefensiveClass.optString(jonj, Constants.TAG_PROMOTION_TYPE).equalsIgnoreCase("Normal") && Constants.PROMOTION) {
                            if (from.equals("edit") && itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")) {
                                promote.setVisibility(View.GONE);
                            } else {
                                promote.setVisibility(View.VISIBLE);
                            }
                        } else {
                            promote.setVisibility(View.GONE);
                        }
                        if (from.equals("edit")) {
                            DetailActivity.fromEdit = true;
                        }
                    } else {
                        JoysaleApplication.dialog(AddProductDetail.this, getString(R.string.alert), getString(R.string.your_product_not));
                    }
                } catch (JSONException e) {
                    JoysaleApplication.dialog(AddProductDetail.this, getString(R.string.alert), getString(R.string.your_product_not));
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
                imageLoadingLay.setVisibility(View.GONE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                // Add your data
                if (from.equals("edit")) {
                    map.put(Constants.TAG_ITEM_ID, itemMap.get(Constants.TAG_ID));
                    map.put(Constants.TAG_SOLD, "false");
                    map.put(Constants.TAG_REMOVE_IMG, removeAry.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
                }
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_ITEM_NAME, name);
                map.put(Constants.TAG_ITEMDES, des);
                map.put(Constants.TAG_QUANTITY, "1");
                map.put(Constants.TAG_PRICE, postPrice);
                map.put(Constants.TAG_SIZE, "");
                map.put(Constants.TAG_CATEGORY, categId);
                map.put(Constants.TAG_SUBCATEGORY, subcategId);
                map.put(Constants.TAG_PRODUCT_IMG, uploadedImage.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", ""));
                map.put(Constants.TAG_SHIPPING_DETAIL, "");
                map.put(Constants.TAG_SHIPPING_TIME, "");
                map.put(Constants.TAG_ADDRESS, location.getText().toString().trim());
                map.put(Constants.TAG_LAT, Double.toString(lat));
                map.put(Constants.TAG_LON, Double.toString(lon));
                map.put(Constants.TAG_CURRENCY, currencyid);
                map.put(Constants.TAG_PAYPALID, paypalId.getText().toString().trim());
                map.put(Constants.TAG_COUNTRYID, mcountryId);
                map.put(Constants.TAG_SHIPPING_COST, shippingFee.getText().toString().trim());
                map.put(Constants.TAG_LOCATION_ID1, selected_locID);
              
                if (exchangeLay.getVisibility() == View.VISIBLE) {
                    if (exchangeSwitch.isChecked()) {
                        map.put(Constants.TAG_EXCHANGE_TO_BUY, "1");
                    } else {
                        map.put(Constants.TAG_EXCHANGE_TO_BUY, "0");
                    }
                } else {
                    map.put(Constants.TAG_EXCHANGE_TO_BUY, "2");
                }
                if (givingAwaySwitch.isChecked()) {
                    map.put(Constants.TAG_GIVING_AWAY, "yes");
                    map.put(Constants.TAG_MAKE_OFFER, "2");
                    map.put(Constants.TAG_INSTANT_BUY, "0");
                } else {
                    map.put(Constants.TAG_GIVING_AWAY, "no");
                    if (offerLay.getVisibility() == View.VISIBLE) {
                        if (chatSwitch.isChecked()) {
                            map.put(Constants.TAG_MAKE_OFFER, "1");
                        } else {
                            map.put(Constants.TAG_MAKE_OFFER, "0");
                        }
                    } else {
                        map.put(Constants.TAG_MAKE_OFFER, "2");
                    }
                    if (buyLay.getVisibility() == View.VISIBLE) {
                        if (buySwitch.isChecked()) {
                            map.put(Constants.TAG_INSTANT_BUY, "1");
                        } else {
                            map.put(Constants.TAG_INSTANT_BUY, "0");
                        }
                    } else {
                        map.put(Constants.TAG_INSTANT_BUY, "2");
                    }
                }

                if (conditionLay.getVisibility() == View.VISIBLE) {
                    map.put(Constants.TAG_ITEM_CONDITION, itemCond);
                } else {
                    map.put(Constants.TAG_ITEM_CONDITION, "0");
                }


                Log.v("parameters", "" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }


    @Override
    protected void onPause() {
        // For Internet checking disconnect
        prevPosition = 0;
        JoysaleApplication.unregisterReceiver(AddProductDetail.this);
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(AddProductDetail.this);
        if (ContextCompat.checkSelfPermission(AddProductDetail.this, WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddProductDetail.this, new String[]{WRITE_EXTERNAL_STORAGE}, 102);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("requestCode", "requestCode=" + requestCode);
        if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(AddProductDetail.this, getString(R.string.storage_permission_access), Toast.LENGTH_SHORT).show();
            //finish();
        } else {
            Toast.makeText(AddProductDetail.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void reset() {
        itemCond = "";
        categId = "";
        subcategId = "";
        loc = "";
        lat = 0;
        lon = 0;
        itemMap.clear();
        images.clear();
    }


    private String getCountryId() {
        mcountryId = "";
        try {
            Log.v(TAG, "from=" + from);
            if (from.equals("edit") && itemMap.size() > 0 && itemMap.get(Constants.TAG_LOCATION).equalsIgnoreCase(location.getText().toString())) {
                if (itemMap.get(Constants.TAG_INSTANT_BUY).equalsIgnoreCase("1")) {
                    mcountryId = itemMap.get(Constants.TAG_COUNTRYID);
                } else {
                    if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked()) {
                        String countryname = location.getText().toString();
                        Log.v("location", "location=" + countryname);
                        if (countryname.contains(",")) {
                            countryname = countryname.substring(countryname.lastIndexOf(',') + 1).trim();
                        }
                        countryname = countryname.trim();
                        Log.v("countryname", "countryname=" + countryname);
                        if (countryId.size() > 0) {
                            int index = countryName.indexOf(countryname);
                            Log.v("index", "index=" + index);
                            Log.v("countryId", "countryId=" + countryId);
                            mcountryId = countryId.get(index);
                        }
                    } else {
                        mcountryId = "0";
                    }
                }
            } else {
                Log.v(TAG, "Visibility=" + String.valueOf(instantLay.getVisibility()));
                if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked()) {
                    String countryname = location.getText().toString();
                    Log.v(TAG, "location=" + countryname);
                    if (countryname.contains(",")) {
                        countryname = countryname.substring(countryname.lastIndexOf(',') + 1).trim();
                    }
                    countryname = countryname.trim();
                    Log.v(TAG, "countryname=" + countryname);
                    if (countryId.size() > 0) {
                        int index = countryName.indexOf(countryname);
                        Log.v(TAG, "index=" + index);
                        Log.v(TAG, "countryId=" + countryId);
                        mcountryId = countryId.get(index);
                    }
                } else {
                    mcountryId = "0";
                }
            }
        } catch (NullPointerException e) {
            mcountryId = "";
            e.printStackTrace();
        } catch (Exception e) {
            mcountryId = "";
            e.printStackTrace();
        }
        return mcountryId;
    }


    @Override
    public void onBackPressed() {
//        reset();
        if (from.equals("home") || from.equals("add")) {
            JoysaleApplication.dialogOkCancel(AddProductDetail.this, getString(R.string.discard), getString(R.string.discard_post), new OnButtonClick() {
                @Override
                public void onOkClicked() {
                    finish();
                }

                @Override
                public void onCancelClicked() {

                }
            });
        } else {
            JoysaleApplication.dialogOkCancel(AddProductDetail.this, getString(R.string.discard), getString(R.string.discard_changes), new OnButtonClick() {
                @Override
                public void onOkClicked() {
                    finish();
                }

                @Override
                public void onCancelClicked() {

                }
            });
        }
    }


    private void getCategories() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PRODUCT_BEFORE_ADD, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonString) {
                Log.v("JSONSTRING", "" + jsonString);
                String stats;
                try {
                    JSONObject json = new JSONObject(jsonString);
                    stats = json.getString(Constants.TAG_STATUS);
                    if (stats.equalsIgnoreCase("true")) {
                        JSONObject res = json.getJSONObject("result");
                        Log.v(TAG, "Result JSON" + res);
                        JSONArray category = res.getJSONArray("category");
                        for (int i = 0; i < category.length(); i++) {
                            JSONObject jcat = category.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(Constants.NAME, DefensiveClass.optString(jcat, Constants.TAG_CATEGORYNAME));
                            map.put(Constants.TAG_ID, DefensiveClass.optString(jcat, Constants.TAG_CATEGORYID));
                            map.put(Constants.KEY_IMAGE, DefensiveClass.optString(jcat, Constants.TAG_CATEGORYIMG).replace("resized/40", "resized/150"));
                            map.put(Constants.TAG_PRODUCT_CONDITION, DefensiveClass.optString(jcat, Constants.TAG_PRODUCT_CONDITION));
                            map.put(Constants.TAG_EXCHANGE_BUY, DefensiveClass.optString(jcat, Constants.TAG_EXCHANGE_BUY));
                            map.put(Constants.TAG_MAKE_OFFER, DefensiveClass.optString(jcat, Constants.TAG_MAKE_OFFER));
                            map.put(Constants.TAG_INSTANT_BUY, DefensiveClass.optString(jcat, Constants.TAG_INSTANT_BUY));
                            categAry.add(map);

                            ArrayList<HashMap<String, String>> subTemp = new ArrayList<HashMap<String, String>>();
                            JSONArray subcat = jcat.getJSONArray("subcategory");
                            for (int x = 0; x < subcat.length(); x++) {
                                JSONObject scat = subcat.getJSONObject(x);

                                HashMap<String, String> smap = new HashMap<String, String>();
                                smap.put(Constants.NAME, scat.getString(Constants.TAG_SUBNAME));
                                smap.put(Constants.TAG_ID, scat.getString(Constants.TAG_SUBID));

                                subTemp.add(smap);
                            }
                            subcategAry.add(subTemp);
                        }

                        JSONArray currency = res.getJSONArray("currency");
                        for (int i = 0; i < currency.length(); i++) {
                            JSONObject jcur = currency.getJSONObject(i);

                            currencyID.add(jcur.getString(Constants.TAG_ID));
//                            currencyspin.add(jcur.getString(Constants.SYMBOL));
                            String[] cur_sym = jcur.getString(Constants.SYMBOL).split("-");
                            String cur_symbol = cur_sym[1];
                            String cur_symbol_api = cur_sym[1]+"-"+cur_sym[0];
                            currencyspin.add(cur_symbol);
                            currencyspin_api.add(cur_symbol_api);
                        }

                        JSONArray productCondition = res.getJSONArray("product_condition");
                        for (int i = 0; i < productCondition.length(); i++) {
                            JSONObject jcur = productCondition.getJSONObject(i);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(Constants.NAME, DefensiveClass.optString(jcur, Constants.NAME));
                            conditionAry.add(map);
                        }

                        JSONArray country = res.getJSONArray(Constants.TAG_COUNTRY);
                        for (int i = 0; i < country.length(); i++) {
                            JSONObject jobj = country.getJSONObject(i);

                            String counId = DefensiveClass.optString(jobj, Constants.TAG_COUNTRYID);
                            if (!counId.equals("0")) {
                                countryId.add(counId);
                                countryName.add(DefensiveClass.optString(jobj, Constants.TAG_COUNTRYNAME));
                                countryCode.add(DefensiveClass.optString(jobj, Constants.TAG_COUNTRY_CODE));
                            }
                        }
                        if (DefensiveClass.optString(res, Constants.TAG_GIVING_AWAY).equalsIgnoreCase("enable")) {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_GIVINGAWAY, true);
                            JoysaleApplication.adminEditor.commit();
                            Constants.GIVINGAWAY = true;
                        } else {
                            JoysaleApplication.adminEditor.putBoolean(Constants.PREF_GIVINGAWAY, false);
                            JoysaleApplication.adminEditor.commit();
                            Constants.GIVINGAWAY = false;
                        }
                    }
                    currencyadapter = new ArrayAdapter<String>(AddProductDetail.this, R.layout.spinner_item, currencyspin);
                    currencyadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    currency.setAdapter(currencyadapter);

                    currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view,
                                                   int position, long id) {
                            //   currencyid = currencyID.get(position);
                            try {
                                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorPrimary));

                                String selectedCurrency = currencyspin.get(position);
                                String selectedCurrency_api = currencyspin_api.get(position);
                                if (selectedCurrency.contains("-")) {
                                    String cur[] = selectedCurrency.split("-");
                                    currencyid = cur[1] + "-" + cur[0];
                                } else {
                                    currencyid = selectedCurrency_api;
                                }
                                Log.v("currencyid", "" + currencyid);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {

                        }
                    });

                    Log.v("from", "from=" + from);
                    if (from.equals("edit")) {
                        setEditProducts();
                        setImageAdapter();
                    }
                    setCategoryConditions();

                    parentLay.setVisibility(View.VISIBLE);
                    saveLay.setVisibility(View.VISIBLE);
                    loadingView.setVisibility(View.GONE);

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
                map.put(Constants.LANG_TYPE, AppUtils.getCurrentLanguageCode(AddProductDetail.this));
                Log.v(TAG, "beforeAddParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                JoysaleApplication.hideSoftKeyboard(AddProductDetail.this);
                onBackPressed();
                break;
            case R.id.cancelIcon:
                dialog.dismiss();
                finish();
                Intent in = new Intent(AddProductDetail.this, Profile.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                startActivity(in);
                reset();
                break;
            case R.id.cancel:
                onBackPressed();
                break;
            case R.id.post:
                try {
                    postPrice = price.getText().toString().trim();
                    if (productName.getText().toString().trim().equals("")) {
                        Log.v(TAG, "cond1");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (productDes.getText().toString().trim().equals("")) {
                        Log.v(TAG, "cond2");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (priceLay.getVisibility() == View.VISIBLE && (postPrice == null || postPrice.equals(""))) {
                        Log.v(TAG, "cond3");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (priceLay.getVisibility() == View.VISIBLE && postPrice.equals("0")) {// == 0) {
                        Log.v(TAG, "cond4");
                        Toast.makeText(AddProductDetail.this, getString(R.string.price_should), Toast.LENGTH_SHORT).show();
                    } else if (location.getText().toString().equals(getString(R.string.set_your_location)) ||
                            location.getText().toString().equals("")) {
                        Log.v(TAG, "cond5");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (getCountryId().equals("")) {
                        Log.v(TAG, "cond6");
                        Toast.makeText(AddProductDetail.this, getString(R.string.problem_location), Toast.LENGTH_SHORT).show();
                    } else if (category.getText().toString().equals(getString(R.string.select_your_category))) {
                        Log.v(TAG, "cond7");
                        Toast.makeText(AddProductDetail.this, getString(R.string.choose_category), Toast.LENGTH_SHORT).show();
                    } else if (itemCond.equals("") && conditionLay.getVisibility() == View.VISIBLE) {
                        Log.v(TAG, "cond8");
                        Toast.makeText(AddProductDetail.this, getString(R.string.choose_condition), Toast.LENGTH_SHORT).show();
                    } else if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked() && paypalId.getText().toString().trim().length() == 0) {
                        Log.v(TAG, "cond9");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked() && !paypalId.getText().toString().matches(emailPattern)) {
                        Log.v(TAG, "cond10");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_verify_paypalid), Toast.LENGTH_SHORT).show();
                    } else if (instantLay.getVisibility() == View.VISIBLE && buySwitch.isChecked() && shippingFee.getText().toString().trim().length() == 0) {
                        Log.v(TAG, "cond11");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    } else if (images.size() == 1 && images.get(0).get("type").equals("add")) {
                        Log.v(TAG, "cond12");
                        Toast.makeText(AddProductDetail.this, getString(R.string.please_upload_image), Toast.LENGTH_SHORT).show();
                    } else if (isValidPrice && !isValidPrice(postPrice, Constants.NO_OF_DIGIT_BEFORE_DECIMAL, Constants.NO_OF_DIGIT_AFTER_DECIMAL)) {
                        Log.v(TAG, "cond13");
                        Toast.makeText(AddProductDetail.this, getString(R.string.reqd_valid_price), Toast.LENGTH_SHORT).show();
                    } else if (isValidPrice && shippingFee.getText().toString().length() > 0 && !isValidPrice(shippingFee.getText().toString(), Constants.NO_OF_DIGIT_BEFORE_DECIMAL, Constants.NO_OF_DIGIT_AFTER_DECIMAL)) {
                        Log.v(TAG, "cond14");
                        Toast.makeText(AddProductDetail.this, getString(R.string.reqd_valid_price), Toast.LENGTH_SHORT).show();
                    }else if(!isValidShipping(shippingFee.getText().toString())){
                        Toast.makeText(AddProductDetail.this, getString(R.string.enter_valid_ship), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.v(TAG, "cond15");
                        for (int i = 0; i < images.size(); i++) {
                            if (images.get(i).get("type").equals("path")) {
                                pathsAry.add(images.get(i).get("image").toString());
                            }
                        }
                        count = pathsAry.size();
                        loadingProgress.setMax(count);
                        //dialog.show();
                        loadingView.setVisibility(View.VISIBLE);
                        String paths = pathsAry.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", "");
                        if (paths.contains(",")) {
                            String path[] = paths.split(",");
                            Log.v("path", "path" + path);
                            new UploadImage().execute(path);
                        } else {
                            if (pathsAry.size() > 0) {
                                new UploadImage().execute(paths);
                            } else {
                                loadingView.setVisibility(View.VISIBLE);
                                alert_title.setText(getString(R.string.posting_list));
                                loadingProgress.setVisibility(View.GONE);
                                postProgress.setVisibility(View.VISIBLE);
                                uploadStatus.setVisibility(View.GONE);
                                for (int i = 0; i < images.size(); i++) {
                                    if (images.get(i).get("type").equals("url")) {
                                        String imageurl = images.get(i).get("image").toString();
                                        imageurl = imageurl.substring(imageurl.lastIndexOf("/") + 1);
                                        uploadedImage.add(imageurl);
                                    }
                                }
                                String name = "", des = "";
                                name = productName.getText().toString().trim();
                                des = Html.toHtml(new SpannableString(productDes.getText()));
                                Log.d(TAG, "description" + des);
                                /*try {

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }*/
                                addProduct(name, des);
                                //new SendProducts().execute();
                            }
                        }
                    }

                } catch (NullPointerException e) {
                    Log.v(TAG, "NullPointerException");
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    Log.v(TAG, "NumberFormatException");
                    e.printStackTrace();
                }
                /*catch (Exception e) {
                    e.printStackTrace();
                }*/

            /**case R.id.share:
                if (!posteditemId.equals("")) {

                   // JoysaleApplication.dialog(AddProductDetail.this, "url", itemMap.get(Constants.TAG_PROURL));

                  //  Toast.makeText(AddProductDetail.this, "partage ici:"+ posteditemId, Toast.LENGTH_SHORT).show();
                   reset();
                    finish();
                    Intent u = new Intent(AddProductDetail.this, CreatePromote.class);
                    u.putExtra("itemId", posteditemId);

                    startActivity(u);
                    String appUrl = "https://www.lystiq.com/?id="+posteditemId;
                    Intent g = new Intent(Intent.ACTION_SEND);
                    g.setType("text/plain");
                    g.putExtra(Intent.EXTRA_TEXT, appUrl);
                    startActivity(Intent.createChooser(g, "Share"));
                    break;


                } else {
                    JoysaleApplication.dialog(AddProductDetail.this, "no", "non");
                    Toast.makeText(AddProductDetail.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                }**/
                break;
            case R.id.conditionLay:
                Intent i = new Intent(AddProductDetail.this, SubCategory.class);
                i.putExtra("data", conditionAry);
                i.putExtra("from", "add");
                i.putExtra("name", itemCond);
                startActivity(i);
                break;
            case R.id.locationLay:
                showLocationDialog();
//                Intent j = new Intent(AddProductDetail.this, LocationActivity.class);
//                j.putExtra("from", "add");
//                startActivity(j);
                break;
            case R.id.categoryLay:
                Intent k = new Intent(AddProductDetail.this, SelectCategory.class);
                k.putExtra("from", "add");
                k.putExtra("categAry", categAry);
                k.putExtra("subcategAry", subcategAry);
                startActivity(k);
                break;
        }
    }


    /**
     * Function for Check a Valid Price
     **/

    private boolean isValidPrice(String postPrice, int mDigitsBeforeZero, int mDigitsAfterZero) {
        String[] tmp1 = postPrice.split("\\.");
        try {
            if (tmp1[0].length() <= mDigitsBeforeZero && tmp1[1].length() <= mDigitsAfterZero) {
                return true;
            } else {
                return false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            if (postPrice.length() <= mDigitsBeforeZero) {
                return true;
            } else {
                return false;
            }
        }
    }


    /**
     * Function for Check a Valid Shipping
     **/

    private boolean isValidShipping(String shipPrice) {
        boolean check =true;
        if(shipPrice.equals(".")){
            check = false;
        }

       return  check;
    }

    /**
     * class for restrict space and spl characters
     **/

    public static class addListenerOnTextChange implements TextWatcher {
        EditText mEdittextview;
        private Context mContext;

        public addListenerOnTextChange(Context context, EditText edittextview) {
            super();
            this.mContext = context;
            this.mEdittextview = edittextview;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEdittextview.getText().length() > 0) {
                mEdittextview.setError(null);
            }

            String result = s.toString().replaceAll("  ", " ");
            String specialChar = s.toString().replaceAll("[^\\s\\w]*", "");
            //for numbers
            //specialChar = specialChar.replaceAll("[0-9]", "");

            Log.v("special char", "=" + specialChar);
            if (!s.toString().equals(result)) {
                mEdittextview.setText(result);
                mEdittextview.setSelection(result.length());
                // alert the user
            }
            if (!s.toString().equals(specialChar)) {
                mEdittextview.setText(specialChar);
                mEdittextview.setSelection(specialChar.length());
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }


    /**
     * Class for filter_icon the multiple spaces
     **/
    public static class addListenerForDes implements TextWatcher {
        EditText mEdittextview;
        private Context mContext;

        public addListenerForDes(Context context, EditText edittextview) {
            super();
            this.mContext = context;
            this.mEdittextview = edittextview;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEdittextview.getText().length() > 0) {
                mEdittextview.setError(null);
            }

            String result = s.toString().replaceAll("  ", " ");

            if (!s.toString().equals(result)) {
                mEdittextview.setText(result);
                mEdittextview.setSelection(result.length());
                // alert the user
            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }


    public class ImagesAdapter extends BaseAdapter {

        ArrayList<HashMap<String, Object>> imgAry;
        private Context mContext;

        public ImagesAdapter(Context ctx2, ArrayList<HashMap<String, Object>> data) {
            mContext = ctx2;
            imgAry = data;
            Log.d("imagecomplete", imgAry + " ");
        }

        @Override
        public int getCount() {
            return imgAry.size();
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
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.addproduct_image, parent, false);//layout
            } else {
                view = convertView;
                view.forceLayout();
            }
            try {
                ImageView singleImage = (ImageView) view.findViewById(R.id.imageView);
                ImageView delete = (ImageView) view.findViewById(R.id.delete);

                final HashMap<String, Object> tempMap = imgAry.get(position);
                Log.e(TAG, "getView: " + tempMap);
                if (tempMap.get("type").equals("add")) {
                    delete.setVisibility(View.GONE);
                    singleImage.setImageResource(R.drawable.plus_sign);
                    singleImage.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            CameraActivity.fromedit = true;
                            images.remove(tempMap);
                            if (from.equals("edit")) {
                                itemMap.put(Constants.TAG_PHOTOS, "" + new JSONArray(images));
                                Intent i = new Intent(AddProductDetail.this, CameraActivity.class);
                                i.putExtra("from", from);
                                i.putExtra("data", itemMap);
                                i.putExtra("isAddProduct", true);
                                startActivityForResult(i, ACTION_EDIT);
                            } else {
                                itemMap.put(Constants.TAG_PHOTOS, "" + new JSONArray(images));
                                Intent i = new Intent(AddProductDetail.this, CameraActivity.class);
                                i.putExtra("from", "add");
                                i.putExtra("data", itemMap);
                                i.putExtra("isAddProduct", true);
//                                startActivity(i);
                                startActivityForResult(i, ADD_IMAGES);
                            }
                        }
                    });
                } else if (tempMap.get(Constants.TAG_TYPE).equals(Constants.TAG_PATH)) {
                    delete.setVisibility(View.VISIBLE);
                    Bitmap bitmap = BitmapFactory.decodeFile("" + tempMap.get(Constants.KEY_IMAGE));
//                    Picasso.with(AddProductDetail.this).load(tempMap.get("image").toString()).into(singleImage);
                    singleImage.setImageBitmap(bitmap);
//                    singleImage.setImageBitmap(JoysaleApplication.getResizedBitmap((Bitmap) tempMap.get("image"), 70));
                } else {
                    delete.setVisibility(View.VISIBLE);
                    Picasso.with(AddProductDetail.this).load(tempMap.get(Constants.KEY_IMAGE).toString()).into(singleImage);
                }

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (images.size() > 0) {
                            if (tempMap.get(Constants.TAG_TYPE).equals("url")) {
                                String imageurl = tempMap.get("image").toString();
                                imageurl = imageurl.substring(imageurl.lastIndexOf("/") + 1);
                                removeAry.add(imageurl);
                                Log.d("removeindex", images.indexOf(tempMap) + " ");
                            }
                            images.remove(tempMap);
                            images.indexOf(tempMap);
                            addPlusIcon();
                        }
                        imagesAdapter.notifyDataSetChanged();
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return view;
        }

    }


    /**
     * class for uploading images to server
     **/
    class UploadImage extends AsyncTask<String, Integer, Integer> {
        JSONObject jsonobject = null;
        String Json = "";
        String status;

        @Override
        protected Integer doInBackground(String... imgpath) {
            for (int i = 0; i < count; i++) {
                Log.v("i", "" + i);
                publishProgress(Math.min(i, count));

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                DataInputStream inStream = null;
                StringBuilder builder = new StringBuilder();
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                String urlString = Constants.API_UPLOAD_IMAGE;
                try {
                    String exsistingFileName = imgpath[i];
                    Log.v(" exsistingFileName", exsistingFileName);
                    FileInputStream fileInputStream = new FileInputStream(saveBitmapToFile(new File(exsistingFileName)));
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
                    dos.writeBytes("item");
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
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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
                    status = jsonobject.getString("status");
                    if (status.equals("true")) {
                        JSONObject image = jsonobject.getJSONObject("Image");
                        String msg = image.getString("Message");
                        String uploadedimgname = image.getString("Name");
                        uploadedImage.add(uploadedimgname);
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

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            loadingProgress.setProgress(0);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.v("values[0]", "" + values[0]);
            loadingProgress.setProgress(values[0]);
            uploadStatus.setText(values[0] + " " + getString(R.string.of) + " " + count + getString(R.string.image_uploaded));
        }

        @Override
        protected void onPostExecute(Integer unused) {
            if (status.equals("true")) {
                loadingProgress.setProgress(count);
                uploadStatus.setText(count + " " + getString(R.string.of) + " " + count + getString(R.string.image_uploaded));
                alert_title.setText(getString(R.string.posting_list));
                loadingProgress.setVisibility(View.GONE);
                postProgress.setVisibility(View.VISIBLE);
                uploadStatus.setVisibility(View.GONE);
                if (from.equals("edit") && uploadedImage.size() > 0) {
                    ArrayList<String> tempAry = new ArrayList<String>();
                    tempAry.addAll(uploadedImage);
                    uploadedImage.clear();
                    int index = 0;
                    for (int i = 0; i < images.size(); i++) {
                        if (images.get(i).get("type").equals("url")) {
                            String imageurl = images.get(i).get("image").toString();
                            imageurl = imageurl.substring(imageurl.lastIndexOf("/") + 1);
                            uploadedImage.add(imageurl);
                        } else if (images.get(i).get("type").equals("path")) {
                            String imageurl = tempAry.get(index);
                            uploadedImage.add(imageurl);
                            index++;
                        }
                    }
                }
                String name = "", des = "";
                try {
                    name = productName.getText().toString().trim();
                    des = Html.toHtml(new SpannableString(productDes.getText()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                addProduct(name, des);
                //new SendProducts().execute();
            } else {
                JoysaleApplication.dialog(AddProductDetail.this, getString(R.string.alert), getString(R.string.image_cannot));
            }
        }
    }


    /**
     * for converting lat, lon to address
     **/
    private class GetLocationAsync extends AsyncTask<String, Void, String> {

        // boolean duplicateResponse;
        double x, y;
        private List<Address> addresses;

        public GetLocationAsync(double latitude, double longitude) {
            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            addresses = JoysaleApplication.getLocationFromLatLng(AddProductDetail.this, from, x, y);
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (addresses != null && !addresses.isEmpty()) {
                    if (addresses.get(0).getAddressLine(1) != null)
                        loc = addresses.get(0).getAddressLine(0) + ", "
                                + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                    else
                        loc = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getCountryName();

                    Log.v("loc", "loc=" + loc);
//                    location.setText(loc);
//                    location.setTextColor(getResources().getColor(R.color.primaryText));
                    locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    static ArrayList<String> addproduct_locationListAry = new ArrayList<String>();
    static ArrayList<String> addproduct_locationListIDAry = new ArrayList<String>();
    String selected_locID="";
    private void showLocationDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.country_select_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        if(FragmentMainActivity.autolocationListAry.get(0).equalsIgnoreCase(getString(R.string.world_wide))){
            addproduct_locationListAry = FragmentMainActivity.autolocationListAry;
            addproduct_locationListAry.remove(0);
            addproduct_locationListIDAry = FragmentMainActivity.locationListIDAry;
            addproduct_locationListIDAry.remove(0);
        }

        ListView locationList = (ListView) dialog.findViewById(R.id.countryLists);
        locationList.setAdapter(new ArrayAdapter<String>(AddProductDetail.this, android.R.layout.simple_list_item_1, FragmentMainActivity.autolocationListAry));
        if (!dialog.isShowing()) {
            dialog.show();
        }
        locationList.setSelection(prevPosition);

        locationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int positionn, long id) {
                String selected_loc = addproduct_locationListAry.get(positionn);
                selected_locID = addproduct_locationListIDAry.get(positionn);
                Log.v(TAG,"FragmentMainActivity.locationAry="+FragmentMainActivity.locationAry);
                /*Pass Country Id using setTag() and Country Name using setText()*/
                location.setTag(selected_locID);
                location.setText(selected_loc);
                if(selected_loc != null && !selected_loc.equals(getString(R.string.world_wide))){
                    location.setTextColor(getResources().getColor(R.color.primaryText));
                    lat = Double.valueOf(FragmentMainActivity.locationAry.get(positionn).get(Constants.TAG_LOCATION_LAT));
                    lon = Double.valueOf(FragmentMainActivity.locationAry.get(positionn).get(Constants.TAG_LOCATION_LON));
                }else if(selected_loc != null && selected_loc.equals(getString(R.string.world_wide))){
                    location.setText(getString(R.string.set_your_location));
                    Toast.makeText(AddProductDetail.this,getString(R.string.world_wide_not_allowed),Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(AddProductDetail.this,getString(R.string.choose_a_location),Toast.LENGTH_LONG).show();
                }

                prevPosition = positionn;
                dialog.dismiss();
            }
        });
    }

}
