package com.app.lystiq;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.external.HorizontalListView;
import com.app.external.MyTagHandler;
import com.app.external.TimeAgo;
import com.app.external.URLSpanNoUnderline;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.ItemsParsing;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;
import com.nirhart.parallaxscroll.views.ParallaxScrollView.OnScrollViewListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.viewpagerindicator.CirclePageIndicator;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.CALL_PHONE;

/**
 * Created by hitasoft
 * <p>
 * This class is for Display Details of a Product
 */

public class DetailActivity extends Activity implements OnClickListener, OnScrollViewListener, OnItemClickListener {

    /**
     * Declare Layout Elements
     **/
    ViewPager viewPager;
    HorizontalListView listView;
    Target target;
    ParallaxScrollView sview;
    Display display;
    RelativeLayout actionbar, main, reviewLay;
    LinearLayout commentLay, detailLay;
    CirclePageIndicator pageIndicator;
    AVLoadingIndicatorView progress;
    RatingBar ratingBar;
    ProgressDialog progressDialog;
    public static TextView commentCount, title, itemPrice, itemCond, likeCount, userName, description, itemStatus,
            postedTime, viewCount, moreItems, location, chat, offer, titleText, call, ratingCount;
    ImageView backBtn, shareBtn, settingBtn, mblVerify, fbVerify, mailVerify, image, userImg, map, likeImg, edit;


    /**
     * Declare Varaibles
     **/
    final String TAG = "DetailActivity";
    public static boolean fromEdit = false, fromStop = false, isSeller = false;
    public boolean fromCall = false;
    int height1, height2, screenWidth2, screenWidth, listHeight, screenheight, position, screenHalf;
    String chatId = "", from, shopaddress, productLikecount;
    boolean chatClicked = false;
    public static HashMap<String, String> itemMap = new HashMap<String, String>();
    private static ArrayList<HashMap<String, String>> MoreItems = new ArrayList<HashMap<String, String>>();
    public HashMap<String, String> backupMap = new HashMap<String, String>();
    ArrayList<String> photosAry = new ArrayList<String>();
    ItemAdapter itemAdapter;
    ViewPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_main_layout);

        title = (TextView) findViewById(R.id.title);
        itemPrice = (TextView) findViewById(R.id.itemPrice);
        itemCond = (TextView) findViewById(R.id.itemCond);
        likeCount = (TextView) findViewById(R.id.likesCount);
        commentCount = (TextView) findViewById(R.id.commentCount);
        userName = (TextView) findViewById(R.id.userName);
        description = (TextView) findViewById(R.id.description);
        postedTime = (TextView) findViewById(R.id.postedTime);
        viewCount = (TextView) findViewById(R.id.viewCount);
        chat = (TextView) findViewById(R.id.chat);
        offer = (TextView) findViewById(R.id.offer);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        shareBtn = (ImageView) findViewById(R.id.shareBtn);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        userImg = (ImageView) findViewById(R.id.userImage);
        actionbar = (RelativeLayout) findViewById(R.id.actionbar);
        listView = (HorizontalListView) findViewById(R.id.listView);
        sview = (ParallaxScrollView) findViewById(R.id.scrollView);
        moreItems = (TextView) findViewById(R.id.moretext);
        main = (RelativeLayout) findViewById(R.id.main);
        pageIndicator = (CirclePageIndicator) findViewById(R.id.pagerIndicator);
        location = (TextView) findViewById(R.id.location);
        itemStatus = (TextView) findViewById(R.id.itemStatus);
        map = (ImageView) findViewById(R.id.banner);
        commentLay = (LinearLayout) findViewById(R.id.commentLay);
        settingBtn = (ImageView) findViewById(R.id.settingBtn);
        likeImg = (ImageView) findViewById(R.id.likereditBtn);
        edit = (ImageView) findViewById(R.id.edit);
        mblVerify = (ImageView) findViewById(R.id.mblverify);
        fbVerify = (ImageView) findViewById(R.id.fbverify);
        mailVerify = (ImageView) findViewById(R.id.mailverify);
        detailLay = (LinearLayout) findViewById(R.id.detailLay);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        titleText = (TextView) findViewById(R.id.title_text);
        call = (TextView) findViewById(R.id.call);
        reviewLay = (RelativeLayout) findViewById(R.id.reviewLay);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingCount = (TextView) findViewById(R.id.ratingCount);

        progressDialog = new ProgressDialog(DetailActivity.this,R.style.AppCompatAlertDialogStyle);

        actionbar.bringToFront();

        MoreItems = new ArrayList<HashMap<String, String>>();

        backBtn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
        sview.setOnScrollViewListener(this);
        commentCount.setOnClickListener(this);
        likeCount.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        userImg.setOnClickListener(this);
        commentLay.setOnClickListener(this);
        settingBtn.setOnClickListener(this);
        offer.setOnClickListener(this);
        chat.setOnClickListener(this);
        likeImg.setOnClickListener(this);
        edit.setOnClickListener(this);
        detailLay.setOnClickListener(null);
        call.setOnClickListener(this);

        itemMap = new HashMap<String, String>();
        position = (int) getIntent().getExtras().get("position");
        from = (String) getIntent().getExtras().get(Constants.FROM);
        backupMap = (HashMap<String, String>) getIntent().getExtras().get("data");
        itemMap = backupMap;
        Log.v(TAG, "itemMap=" + itemMap);
        productLikecount = itemMap.get(Constants.TAG_LIKECOUNT);
        titleText.setText(itemMap.get(Constants.TAG_TITLE));

        display = this.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        screenheight = display.getHeight();
        screenWidth = width * 49 / 100;
        screenWidth2 = display.getWidth();
        listHeight = width * 75 / 100;
        screenHalf = width / 2;
        height1 = screenheight * 55 / 100;
        height2 = screenheight * 65 / 100;
        Log.v(TAG, "screenheight=" + screenheight);

        viewPager.getLayoutParams().height = height2;

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.secondaryText), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.starColor), PorterDuff.Mode.SRC_ATOP);

        setData();

        progress.setVisibility(View.VISIBLE);

        // call getitems Api
        getHomeDatas(0);

        itemAdapter = new ItemAdapter(DetailActivity.this, MoreItems);
        Log.v(TAG, "moreitems=" + MoreItems);
        listView.setAdapter(itemAdapter);

        getImageAry();
        checkUser();
        updateView();

        viewPager.setOnPageChangeListener(mOnPageChangeListener);

    }

    /**
     * set uploaded item datas to elements
     **/

    private void setData() {
        title.setText(itemMap.get(Constants.TAG_TITLE));
        String curren = itemMap.get(Constants.TAG_CURRENCY_CODE);
        if (itemMap.get(Constants.TAG_PRICE).equals("0")) {
            itemPrice.setText(getString(R.string.giving_away));
            itemPrice.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else {
            if (curren.contains("-")) {
                String cur[] = curren.split("-");
                curren = cur[0];
                if (curren != null) {
                   // itemPrice.setText(curren + " " + itemMap.get(Constants.TAG_PRICE));
                    itemPrice.setText(itemMap.get(Constants.TAG_PRICE)+" "+curren);
                }
            } else {
                itemPrice.setText(itemMap.get(Constants.TAG_PRICE) + " "+itemMap.get(Constants.TAG_CURRENCY_CODE));
            }
        }
        if (itemMap.get(Constants.TAG_ITEM_CONDITION).equals("") || itemMap.get(Constants.TAG_ITEM_CONDITION).equals("0")) {
            itemCond.setVisibility(View.GONE);
        } else {
            itemCond.setText(itemMap.get(Constants.TAG_ITEM_CONDITION));
        }
        Spannable spannedText = (Spannable) Html.fromHtml(itemMap.get(Constants.TAG_ITEM_DES), null, new MyTagHandler());
        likeCount.setText(itemMap.get(Constants.TAG_LIKECOUNT) + " " + getResources().getString(R.string.likes));
        commentCount.setText(itemMap.get(Constants.TAG_COMMENTCOUNT) + " " + getResources().getString(R.string.comments));
        userName.setText(itemMap.get(Constants.TAG_SELLERNAME));
        description.setText(spannedText);
        description.setMovementMethod(LinkMovementMethod.getInstance());
        stripUnderlines(description);
        viewCount.setText(JoysaleApplication.format(Double.parseDouble(itemMap.get(Constants.TAG_VIEWCOUNT))) + " " + getResources().getString(R.string.views));
        moreItems.setText(getResources().getString(R.string.more_items_from) + " " + itemMap.get(Constants.TAG_SELLERNAME));
        location.setText(itemMap.get(Constants.TAG_LOCATION));
        shopaddress = itemMap.get(Constants.TAG_LOCATION);

        if (!itemMap.get(Constants.TAG_SELLERID).equals(GetSet.getUserId())) {
            checkItemStatus();
        }

        if (JoysaleApplication.isRTL(DetailActivity.this)) {
            title.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            description.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            title.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            description.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }

        try {
            long timestamp = 0;
            String time = itemMap.get(Constants.TAG_POSTED_TIME);
            if (time != null) {
                timestamp = Long.parseLong(time) * 1000;
            }
            TimeAgo timeAgo = new TimeAgo(DetailActivity.this);
            postedTime.setText(timeAgo.timeAgo(timestamp));
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = "http://maps.google.com/maps/api/staticmap?center=" + itemMap.get(Constants.TAG_LATITUDE) + "," + itemMap.get(Constants.TAG_LONGITUDE) +
                "&zoom=15&size=" + screenWidth2 + "x" + screenWidth2 / 2 + "&sensor=false&key=" + Constants.GOOGLE_API_KEY;
//        Log.v("url", "url=" + url);
        Picasso.with(DetailActivity.this).load(url).into(map);
        if (!itemMap.get(Constants.TAG_SELLERIMG).equals("")) {
            Picasso.with(DetailActivity.this).load(itemMap.get(Constants.TAG_SELLERIMG)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImg);
        }

        String liked = itemMap.get(Constants.TAG_LIKED);
        if (liked.equalsIgnoreCase("yes")) {
            likeImg.setImageResource((R.drawable.like_icon));
        } else {
            likeImg.setImageResource(R.drawable.unlike_icon);
        }
        if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")) {
            itemStatus.setVisibility(View.VISIBLE);
            itemStatus.setText(getString(R.string.sold));
            itemStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.soldbg));
        } else {
            if (Constants.PROMOTION) {
                if (itemMap.get(Constants.TAG_PROMOTION_TYPE).equalsIgnoreCase("Ad")) {
                    itemStatus.setVisibility(View.VISIBLE);
                    itemStatus.setText(getString(R.string.ad));
                    itemStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.adbg));
                } else if (itemMap.get(Constants.TAG_PROMOTION_TYPE).equalsIgnoreCase("Urgent")) {
                    itemStatus.setVisibility(View.VISIBLE);
                    itemStatus.setText(getString(R.string.urgent));
                    itemStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.urgentbg));
                } else {
                    itemStatus.setVisibility(View.GONE);
                }
            } else {
                itemStatus.setVisibility(View.GONE);
            }

        }

        if (Constants.BUYNOW) {
            reviewLay.setVisibility(View.VISIBLE);
            try {
                ratingBar.setRating(Float.parseFloat(itemMap.get(Constants.TAG_SELLER_RATING)));
                if(itemMap.get(Constants.TAG_RATING_USER_COUNT)!=null){
                    ratingCount.setText("(" + itemMap.get(Constants.TAG_RATING_USER_COUNT) + ")");
                }else{
                    ratingCount.setText("");
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
        }

        if (itemMap.get(Constants.TAG_FACEBOOK_VERIFICATION).equals("true")) {
            fbVerify.setImageResource(R.drawable.fb_veri);
        } else {
            fbVerify.setImageResource(R.drawable.fb_unveri);
        }
        if (itemMap.get(Constants.TAG_EMAIL_VERIFICATION).equals("true")) {
            mailVerify.setImageResource(R.drawable.mail_veri);
        } else {
            mailVerify.setImageResource(R.drawable.mail_unveri);
        }
        if (itemMap.get(Constants.TAG_MOBILE_VERIFICATION).equals("true")) {
            mblVerify.setImageResource(R.drawable.mob_veri);
        } else {
            mblVerify.setImageResource(R.drawable.mob_unveri);
        }

        if (itemMap.get(Constants.TAG_SHOW_SELLER_MOB).equals("true")) {
            call.setVisibility(View.VISIBLE);
        } else {
            call.setVisibility(View.GONE);
        }
    }

    /**
     * for change the bottom button by user
     **/

    private void checkUser() {
        if (itemMap.get(Constants.TAG_SELLERID).equals(GetSet.getUserId())) {
            edit.setVisibility(View.VISIBLE);
            likeImg.setVisibility(View.GONE);
            isSeller = true;
            chat.setVisibility(View.GONE);
            if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")) {
                offer.setText(getString(R.string.mark_as_available));
            } else {
                if (Constants.PROMOTION) {
                    if (itemMap.get(Constants.TAG_PROMOTION_TYPE).equals("Normal")) {
                        offer.setText(getString(R.string.promote_your_product));
                    } else {
                        offer.setText(getString(R.string.promotion_details));
                    }
                } else {
                    offer.setVisibility(View.GONE);
                }
            }
        } else {
            edit.setVisibility(View.GONE);
            likeImg.setVisibility(View.VISIBLE);
            chat.setVisibility(View.VISIBLE);
            isSeller = false;
            if (Constants.BUYNOW) {
                offer.setText(getString(R.string.instantbuy));
                if (itemMap.get(Constants.TAG_INSTANT_BUY).equals("0") || itemMap.get(Constants.TAG_INSTANT_BUY).equals("2")
                        || itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")) {
                    offer.setVisibility(View.GONE);
                } else {
                    offer.setVisibility(View.VISIBLE);
                }
                if (itemMap.get(Constants.TAG_PRICE).equals("0")) {
                    offer.setVisibility(View.GONE);
                }
            } else {
                offer.setText(getString(R.string.make_an_offer));
                if (itemMap.get(Constants.TAG_MAKE_OFFER).equals("0")) {
                    offer.setVisibility(View.VISIBLE);
                } else {
                    offer.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * for get the images from json to array
     **/

    private void getImageAry() {
        if (itemMap.get(Constants.TAG_PHOTOS).equals("") || itemMap.get(Constants.TAG_PHOTOS).equals(null)) {
            Log.v(TAG, "photos emptyyyy");
        } else {
            try {
                JSONArray photos = new JSONArray(itemMap.get(Constants.TAG_PHOTOS));
                for (int i = 0; i < photos.length(); i++) {
                    JSONObject jph = photos.getJSONObject(i);
                    String imageurl = DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_ORG);
                    //photosAry.clear();
                    photosAry.add(imageurl);
                }
                pagerAdapter = new ViewPagerAdapter(DetailActivity.this, photosAry);
                viewPager.setAdapter(pagerAdapter);
                viewPager.setCurrentItem(0);
                //Setting the indicator for pager
                if (photosAry.size() > 1) {
                    if (screenheight > 800) {
                        pageIndicator.setRadius(10);
                    } else {
                        pageIndicator.setRadius(5);
                    }
                    pageIndicator.setViewPager(viewPager);
                } else {
                    pageIndicator.setVisibility(View.GONE);
                }
                Log.v(TAG, "photosAry=" + photosAry);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int arg0) {
            if (arg0 == 0) {
                try {
                    if (arg0 == viewPager.getCurrentItem()) {
                        //getImageAry();
                        pagerAdapter = new ViewPagerAdapter(DetailActivity.this, photosAry);
                        viewPager.setAdapter(pagerAdapter);
                        viewPager.setCurrentItem(0);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            try {
                if (arg0 == viewPager.getCurrentItem()) {
                    pageIndicator.setViewPager(viewPager, viewPager.getCurrentItem());
                    int index = viewPager.getCurrentItem();
                    View itemView = viewPager.findViewWithTag("pos" + index);
                    Log.v(TAG, "itemView=" + itemView);
                    Log.v(TAG, "position=" + index);
                    if (itemView != null) {
                        final ImageView image = (ImageView) itemView.findViewById(R.id.imgDisplay);
                        String imageloadingurl = photosAry.get(index);
                        setImage(image, imageloadingurl);
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPageSelected(int position) {

        }
    };


    private void setImage(final ImageView image, String imageloadingurl) {
        Log.v(TAG, "imageloadingurl=" + imageloadingurl);
        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //stubImage.setVisibility(View.GONE);
                try {
                    float ht = bitmap.getHeight();
                    float scale = (float) display.getWidth() / bitmap.getWidth();
                    int newHeight = (int) Math.round(ht * scale);
                    Log.v(TAG, "hai" + newHeight + "," + "setimage" + ht);

                    viewPager.getLayoutParams().height = newHeight;
                    viewPager.getLayoutParams().width = display.getWidth();
                    image.getLayoutParams().height = newHeight;
                    image.getLayoutParams().width = display.getWidth();
                    image.setImageBitmap(JoysaleApplication.getResizedBitmap(bitmap, 1024));

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.v(TAG, "on failed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.with(DetailActivity.this).load(imageloadingurl).into(target);
    }

    public void dialog(String name, String imageurl) {
        final Dialog dialog = new Dialog(DetailActivity.this, R.style.DialogSlideAnim);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.contactme_dialog);

        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView contactName = (TextView) dialog.findViewById(R.id.contactName);
        LinearLayout send = (LinearLayout) dialog.findViewById(R.id.send);
        ImageView contactImg = (ImageView) dialog.findViewById(R.id.contactImg);
        final EditText contactMsg = (EditText) dialog.findViewById(R.id.contactMsg);
        final EditText makeOffer = (EditText) dialog.findViewById(R.id.makeOffer);
        LinearLayout offerLay = (LinearLayout) dialog.findViewById(R.id.offerLay);
        LinearLayout emptyLay = (LinearLayout) dialog.findViewById(R.id.emptyLay);
        final RelativeLayout mainLay = (RelativeLayout) dialog.findViewById(R.id.mainLay);

        contactMsg.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(500)});
        makeOffer.setFilters(new InputFilter[]{new JoysaleApplication.DecimalDigitsInputFilter(6, 2)});
        contactName.setText(name);
        Picasso.with(DetailActivity.this).load(imageurl).into(contactImg);

        if (name.equals(getString(R.string.make_an_offer))) {
            offerLay.setVisibility(View.VISIBLE);
        } else {
            offerLay.setVisibility(View.GONE);
        }

        emptyLay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mainLay.getWindowToken(), 0);
                dialog.dismiss();
            }
        });

        send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (JoysaleApplication.isNetworkAvailable(DetailActivity.this)) {
                    if (contactMsg.getText().toString().trim().length() != 0 && makeOffer.getText().toString().trim().length() != 0) {
                        if (!(itemMap.get(Constants.TAG_PRICE).equals("0")) && Float.parseFloat(itemMap.get(Constants.TAG_PRICE)) <= Float.parseFloat(makeOffer.getText().toString())) {
                            Toast.makeText(DetailActivity.this, getString(R.string.offer_should_not_above), Toast.LENGTH_SHORT).show();
                        } else if (!(itemMap.get(Constants.TAG_PRICE).equals("0")) && Float.parseFloat(makeOffer.getText().toString()) == 0) {
                            Toast.makeText(DetailActivity.this, getString(R.string.offer_should_not_zero), Toast.LENGTH_SHORT).show();
                        } else {
                            initializeGetChat();
                            progressDialog.setMessage(getString(R.string.pleasewait));
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                                Drawable drawable = new ProgressBar(DetailActivity.this).getIndeterminateDrawable().mutate();
                                drawable.setColorFilter(ContextCompat.getColor(DetailActivity.this, R.color.progressColor),
                                        PorterDuff.Mode.SRC_IN);
                                progressDialog.setIndeterminateDrawable(drawable);
                            }
                            progressDialog.show();
                            getChatId("offer", contactMsg.getText().toString().trim(), makeOffer.getText().toString().trim());
                            //makeOffer(contactMsg.getText().toString().trim(), makeOffer.getText().toString().trim());
                            dialog.dismiss();
                        }
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.please_fill_all), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    JoysaleApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                }

            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void initializeGetChat() {
        if (chatClicked) {
            this.progressDialog.setMessage(getString(R.string.pleasewait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            this.progressDialog.show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            try {
                //      sview.setViewsBounds(ParallaxScollListView.ZOOM_X2);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * for show the popupmenu window
     **/

    public void shareImage(View v) {
        final ArrayList<String> values = new ArrayList<>();
        Log.v(TAG, "makeofferfromapi=" + itemMap.get(Constants.TAG_MAKE_OFFER));
        if (itemMap.get(Constants.TAG_SELLERID).equals(GetSet.getUserId())) {
            if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")) {
                values.add(getString(R.string.delete_product));
                values.add(getString(R.string.mark_as_available));
            } else {
                values.add(getString(R.string.delete_product));
                values.add(getString(R.string.mark_as_sold));
            }
        } else {
            if (Constants.EXCHANGE && itemMap.get(Constants.TAG_EXCHANGE_BUY).equalsIgnoreCase("1")) {
                    values.add(getString(R.string.request_exchange));
            }
            if (Constants.BUYNOW && (itemMap.get(Constants.TAG_MAKE_OFFER).equals("0")) && !(itemMap.get(Constants.TAG_PRICE).equals("0"))) {
                values.add(getString(R.string.make_an_offer));
            }

            if (itemMap.get(Constants.TAG_REPORT).equals("yes")) {
                values.add(getString(R.string.undo_report));
            } else {
                values.add(getString(R.string.report_product));
            }

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.share_new, android.R.id.text1, values);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.share, null);
        layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
        final PopupWindow popup = new PopupWindow(DetailActivity.this);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setContentView(layout);
        popup.setWidth(display.getWidth() * 60 / 100);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.showAtLocation(main, Gravity.TOP | Gravity.RIGHT, 0, 20);

        final ListView lv = (ListView) layout.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        popup.showAsDropDown(v);

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                popup.dismiss();
                openAction(values.get(position));
            }
        });
    }

    public void openAction(String from) {
        Log.v(TAG, "from=" + from);
        if (from.equals(getString(R.string.delete_product))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.delete_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.mark_as_available))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.back_sale_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.mark_as_sold))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.sold_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.request_exchange))) {
            if (GetSet.isLogged()) {
                if (itemMap.get(Constants.TAG_ITEM_STATUS).equals("sold")) {
                    Toast.makeText(this, getString(R.string.item_sold_message_other), Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(DetailActivity.this, CreateExchange.class);
                    i.putExtra("itemId", itemMap.get(Constants.TAG_ID));
                    startActivity(i);
                }
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.make_an_offer))) {
            if (GetSet.isLogged()) {
                dialog(getString(R.string.make_an_offer), itemMap.get(Constants.TAG_SELLERIMG));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.undo_report))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.undoreport_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        } else if (from.equals(getString(R.string.report_product))) {
            if (GetSet.isLogged()) {
                confirmdialog(getString(R.string.report_product_confirmation));
            } else {
                Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                startActivity(j);
            }
        }
    }

    public void confirmdialog(final String Message) {
        final Dialog dialog = new Dialog(DetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_button);

        message.setText(Message);

        cancel.setVisibility(View.VISIBLE);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Message.equals(getResources().getString(R.string.report_product_confirmation)) ||
                        Message.equals(getResources().getString(R.string.undoreport_product_confirmation))) {
                    reportItem();
                    //new ReportItem().execute(itemMap.get(Constants.TAG_ID));
                } else if (Message.equals(getResources().getString(R.string.delete_product_confirmation))) {
                    deleteProduct();
                    //new deleteProduct().execute();
                } else if (Message.equals(getResources().getString(R.string.back_sale_confirmation)) ||
                        Message.equals(getResources().getString(R.string.sold_product_confirmation))) {
                    if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")) {
                        initializeSoldStatus();
                        changeSoldStatus("0");
                    } else {
                        initializeSoldStatus();
                        changeSoldStatus("1");
                    }
                }
                dialog.dismiss();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void initializeSoldStatus() {
        progressDialog.setMessage(getString(R.string.pleasewait));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void approveDialog(final Context ctx) {
        final Dialog dialog = new Dialog(DetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);

        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, LinearLayout.LayoutParams.WRAP_CONTENT);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        TextView message = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);
        TextView cancel = (TextView) dialog.findViewById(R.id.cancel_button);

        message.setText(getString(R.string.product_waiting_for_admin_approval));

        cancel.setVisibility(View.GONE);

        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    switch (from) {
                        case "home":
                            FragmentMainActivity.homeItemList.remove(position);
                            FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                            break;
                        case "mylisting":
                            MyListing.addedItems.remove(position);
                            MyListing.itemAdapter.notifyDataSetChanged();
                            break;
                        case "liked":
                            LikedItems.likedItems.remove(position);
                            LikedItems.itemAdapter.notifyDataSetChanged();
                            break;
                        case "detail":
                            MoreItems.remove(position);
                            itemAdapter.notifyDataSetChanged();
                            break;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                ((Activity) ctx).finish();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void stripUnderlines(TextView textView) {
        Spannable s = (Spannable) (textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s.toString().trim());
        Log.e(TAG, "setData: " + s);
    }

    @Override
    public void onBackPressed() {
        //MoreItems.clear();
        super.onBackPressed();
        this.finish();
    }


    @Override
    public void onScrollChanged(ParallaxScrollView v, int l, int t, int oldl,
                                int oldt) {
        //cd.setAlpha(getAlphaforActionBar(v.getScrollY()));

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        try {
            Intent i = new Intent(DetailActivity.this, DetailActivity.class);
            i.putExtra(Constants.DATA, MoreItems.get(position));
            i.putExtra(Constants.POSITION, position);
            i.putExtra(Constants.FROM, "detail");
            startActivity(i);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        Log.v(TAG, "fromOnStop=" + from + " " + MoreItems);
        if (itemMap.equals(backupMap) && !MoreItems.isEmpty()) {
            MoreItems.clear();
            Log.v(TAG, "Cleared");
            fromStop = true;
            itemAdapter.notifyDataSetChanged();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(DetailActivity.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        // For Internet checking
        JoysaleApplication.registerReceiver(DetailActivity.this);
        if (fromEdit) {
            fromEdit = false;
            progressDialog.setMessage(getString(R.string.pleasewait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            loadHomeItems();
            //new homeLoadItems().execute();
        }
        if ((!itemMap.equals(backupMap) || fromStop)) {
            if (fromCall) {
                fromCall = false;
                progress.setVisibility(View.GONE);
            } else {
                progress.setVisibility(View.VISIBLE);
            }
            getHomeDatas(0);
        }
        itemMap = backupMap;
        super.onResume();
    }

    class ViewPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater inflater;
        ArrayList<String> temp;

        public ViewPagerAdapter(Context act, ArrayList<String> newary) {
            this.temp = newary;
            this.context = act;
        }

        public int getCount() {
            return temp.size();
        }

        public Object instantiateItem(ViewGroup collection, final int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.layout_fullscreen,
                    collection, false);

            itemView.setTag("pos" + position);

            image = (ImageView) itemView.findViewById(R.id.imgDisplay);

            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(DetailActivity.this, SingleView.class);
                    i.putExtra("mimages", photosAry);
                    i.putExtra("position", position);
                    startActivity(i);
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
     * Function to get a Home Datas
     **/

    private void getHomeDatas(final int pageCount) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_HOME, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                Log.v(TAG, "getItemsRes=" + json);
                MoreItems.clear();
                ItemsParsing parse = new ItemsParsing(DetailActivity.this, GetSet.getUserId());
                MoreItems.addAll(parse.parsing(json));
                progress.setVisibility(View.GONE);
                if (MoreItems.size() == 0) {
                    moreItems.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                } else {
                    itemAdapter.notifyDataSetChanged();
                    listView.getLayoutParams().height = listHeight;
                    Log.v(TAG, "moreheight=" + listView.getHeight());
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
                int offset = (pageCount * 20);
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_TYPE, "moreitems");
                map.put(Constants.TAG_SELLERID, itemMap.get(Constants.TAG_SELLERID));
                map.put(Constants.TAG_ITEM_ID, itemMap.get(Constants.TAG_ID));
                map.put(Constants.TAG_OFFSET, Integer.toString(offset));
                map.put(Constants.TAG_LIMIT, "20");
                if (GetSet.isLogged()) {
                    map.put(Constants.TAG_USERID, GetSet.getUserId());
                } else {
                    map.put(Constants.TAG_USERID, "");
                }
                map.put(Constants.LANG_TYPE, AppUtils.getCurrentLanguageCode(DetailActivity.this));
                Log.v(TAG, "getItemsParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);

    }

    /**
     * adapter for showing more items
     **/

    public class ItemAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> homeItems;
        ViewHolder holder = null;
        private Context mContext;

        public ItemAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            homeItems = data;
        }

        @Override
        public int getCount() {
            return homeItems.size();
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
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.home_list_items,
                        parent, false);// layout
                holder = new ViewHolder();
                holder.singleImage = (ImageView) convertView.findViewById(R.id.singleImage);
                holder.itemPrice = (TextView) convertView.findViewById(R.id.priceText);
                holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
                holder.productType = (TextView) convertView.findViewById(R.id.productType);
                holder.location = (TextView) convertView.findViewById(R.id.location);
                holder.postedTime = (TextView) convertView.findViewById(R.id.postedTime);
                holder.imageLay = (RelativeLayout) convertView.findViewById(R.id.imageLay);
                holder.mainLay = (LinearLayout) convertView.findViewById(R.id.mainLay);

                holder.singleImage.getLayoutParams().height = screenHalf;
                holder.imageLay.getLayoutParams().height = screenHalf;
                holder.singleImage.getLayoutParams().width = screenHalf;
                holder.imageLay.getLayoutParams().width = screenHalf;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                final HashMap<String, String> tempMap = homeItems.get(position);

                holder.mainLay.setPadding(0, 0, JoysaleApplication.dpToPx(mContext, 5), 0);

                Picasso.with(DetailActivity.this).load(tempMap.get(Constants.TAG_ITEM_URL_350)).into(holder.singleImage);
                holder.itemName.setText(tempMap.get(Constants.TAG_TITLE));
                if (tempMap.get(Constants.TAG_PRICE).equals("0")) {
                    holder.itemPrice.setText(getString(R.string.giving_away));
                    holder.itemPrice.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.itemPrice.setText(tempMap.get(Constants.TAG_PRICE) + " "
                            + tempMap.get(Constants.TAG_CURRENCY_SYM));
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
                            holder.productType.setVisibility(View.GONE);
                        }
                    } else {
                        holder.productType.setVisibility(View.GONE);
                    }
                }

                long timestamp = 0;
                String time = tempMap.get(Constants.TAG_POSTED_TIME);
                if (time != null) {
                    timestamp = Long.parseLong(time) * 1000;
                }
                TimeAgo timeAgo = new TimeAgo(mContext);
                holder.postedTime.setText(timeAgo.timeAgo(timestamp));

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            ImageView singleImage;
            TextView itemPrice, itemName, location, postedTime, productType;
            RelativeLayout imageLay;
            LinearLayout mainLay;
        }
    }

    /**
     * Function for update the view
     **/

    private void updateView() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_UPDATE_VIEW, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                try {
                    JSONObject json = new JSONObject(result);
                    String status = json.getString(Constants.TAG_STATUS);
                    if (status.equalsIgnoreCase("true")) {
                        String count = itemMap.get(Constants.TAG_VIEWCOUNT);
                        viewCount.setText(JoysaleApplication.format(Double.parseDouble(count)) + " " + getResources().getString(R.string.views));
                        final int view = (Integer.parseInt(count) + 1);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (from) {
                                    case "home":
                                        FragmentMainActivity.homeItemList.get(position).put(Constants.TAG_VIEWCOUNT, Integer.toString(view));
                                        FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                        break;
                                    case "mylisting":
                                        MyListing.addedItems.get(position).put(Constants.TAG_VIEWCOUNT, Integer.toString(view));
                                        break;
                                    case "liked":
                                        LikedItems.likedItems.get(position).put(Constants.TAG_VIEWCOUNT, Integer.toString(view));
                                        break;
                                    case "detail":
                                        Log.v(TAG, "FromDetailUpdated=" + MoreItems.get(position).get(Constants.TAG_ID) + " " + MoreItems.get(position).get(Constants.TAG_VIEWCOUNT));
                                        MoreItems.get(position).put(Constants.TAG_VIEWCOUNT, Integer.toString(view));
                                        break;
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
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
                map.put(Constants.TAG_ITEM_ID, itemMap.get(Constants.TAG_ID));
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }


    /**
     * Function for get chat id between logined user and the seller
     **/

    private String getChatId(final String from, final String message, final String offer) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_CHAT_ID, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                try {
                    Log.v(TAG, "chatIdResponse=" + json);
                    Log.v(TAG, "chatIdResponseFrom=" + from);
                    final JSONObject jobj = new JSONObject(json);
                    if (jobj.getString(Constants.TAG_STATUS).equals("true")) {
                        chatId = jobj.getString("chat_id");
                    }
                    if (from.equals("offer"))
                        makeOffer(message, offer, chatId);
                    chat.setOnClickListener(DetailActivity.this);
                    if (from.equals("chat")) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        chatClicked = false;
                        Log.v(TAG, "sellerName=" + itemMap.get(Constants.TAG_SELLERNAME));
                        Intent i = new Intent(DetailActivity.this, ChatActivity.class);
                        i.putExtra(Constants.TAG_USERNAME, itemMap.get(Constants.TAG_SELLER_USERNAME));
                        i.putExtra(Constants.TAG_USER_ID, itemMap.get(Constants.TAG_SELLERID));
                        i.putExtra(Constants.CHATID, chatId);
                        i.putExtra(Constants.TAG_USERIMAGE_M, itemMap.get(Constants.TAG_SELLERIMG));
                        i.putExtra(Constants.TAG_FULL_NAME, itemMap.get(Constants.TAG_SELLERNAME));
                        i.putExtra("data", itemMap);
                        i.putExtra(Constants.FROM, "detail");
                        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
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
                map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                map.put(Constants.TAG_RECEIVER_ID, itemMap.get(Constants.TAG_SELLERID));
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
        return chatId;
    }

    /**
     * Function for change the product to sold and back to sale
     **/

    private void changeSoldStatus(final String value) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SOLD_ITEM, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    JSONObject json = new JSONObject(res);
                    String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        if (DefensiveClass.optString(json, Constants.TAG_MESSAGE).equalsIgnoreCase("Item Status changed to Sold")) {
                            Toast.makeText(DetailActivity.this, getString(R.string.item_status_changed_to_sold), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(DetailActivity.this, getString(R.string.item_status_changed_to_available), Toast.LENGTH_LONG).show();
                        }
                        String value = "";
                        String promotionType = "";
                        if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")) {
                            value = "onsale";
                            offer.setText(getString(R.string.promote_your_product));
                            promotionType = itemMap.get(Constants.TAG_PROMOTION_TYPE);
                        } else {
                            value = "sold";
                            offer.setText(getString(R.string.mark_as_available));
                            itemMap.put(Constants.TAG_PROMOTION_TYPE, "Normal");
                            promotionType = itemMap.get(Constants.TAG_PROMOTION_TYPE);
                        }
                        itemMap.put(Constants.TAG_ITEM_STATUS, value);

                        switch (from) {
                            case "home":
                                FragmentMainActivity.homeItemList.get(position).put(Constants.TAG_ITEM_STATUS, value);
                                FragmentMainActivity.homeItemList.get(position).put(Constants.TAG_PROMOTION_TYPE, promotionType);
                                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                break;
                            case "mylisting":
                                MyListing.addedItems.get(position).put(Constants.TAG_ITEM_STATUS, value);
                                MyListing.addedItems.get(position).put(Constants.TAG_PROMOTION_TYPE, promotionType);
                                MyListing.itemAdapter.notifyDataSetChanged();
                                break;
                            case "liked":
                                LikedItems.likedItems.get(position).put(Constants.TAG_ITEM_STATUS, value);
                                LikedItems.likedItems.get(position).put(Constants.TAG_PROMOTION_TYPE, promotionType);
                                LikedItems.itemAdapter.notifyDataSetChanged();
                                break;
                            case "detail":
                                MoreItems.get(position).put(Constants.TAG_ITEM_STATUS, value);
                                MoreItems.get(position).put(Constants.TAG_PROMOTION_TYPE, promotionType);
                                itemAdapter.notifyDataSetChanged();
                                break;
                        }
                        finish();
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
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
                map.put(Constants.TAG_VALUE, value);
                map.put(Constants.TAG_ITEM_ID, itemMap.get(Constants.TAG_ID));
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);

    }

    /**
     * Function for remove the product from listing
     **/

    private void deleteProduct() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_DELETE_PRODUCT, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        Toast.makeText(DetailActivity.this, getString(R.string.product_deleted_duccessfully), Toast.LENGTH_LONG).show();
                        finish();
                        switch (from) {
                            case "home":
                                FragmentMainActivity.homeItemList.remove(position);
                                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                break;
                            case "mylisting":
                                MyListing.addedItems.remove(position);
                                MyListing.itemAdapter.notifyDataSetChanged();
                                break;
                            case "liked":
                                LikedItems.likedItems.remove(position);
                                LikedItems.itemAdapter.notifyDataSetChanged();
                                break;
                            case "detail":
                                MoreItems.remove(position);
                                itemAdapter.notifyDataSetChanged();
                                break;
                        }
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
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
                map.put(Constants.TAG_ITEM_ID, itemMap.get(Constants.TAG_ID));
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);

    }

    /**
     * Function for send the offer request to seller
     **/

    private void makeOffer(final String message, final String offerPrice, final String chat_id) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEND_OFFER_REQ, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
                try {
                    JSONObject json = new JSONObject(res);
                    String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        Toast.makeText(DetailActivity.this, getString(R.string.message_send_successfully), Toast.LENGTH_LONG).show();
                    } else if ((response.equals("false"))) {
                        Toast.makeText(DetailActivity.this, getString(R.string.conversation_blocked), Toast.LENGTH_SHORT).show();
                    }
                    /*else if((response.equals("false") && DefensiveClass.optString(json, Constants.TAG_MESSAGE).equals("You blocked this seller"))){
                        Toast.makeText(DetailActivity.this, getString(R.string.you_blocked_this_seller), Toast.LENGTH_SHORT).show();
                    }
                    else if((response.equals("false") && DefensiveClass.optString(json, Constants.TAG_MESSAGE).equals("Seller is blocked by you"))){
                        Toast.makeText(DetailActivity.this, getString(R.string.seller_blocked_by_you), Toast.LENGTH_SHORT).show();
                    }*/
                    else {
                        Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
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
                long unixTime = System.currentTimeMillis() / 1000L;
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                map.put(Constants.TAG_CHAT_ID, chat_id);
                map.put(Constants.TAG_SOURCE_ID, itemMap.get(Constants.TAG_ID));
                map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
                map.put(Constants.TAG_MESSAGE, message);
                map.put(Constants.TAG_OFFER_PRICE, offerPrice);
                map.put(Constants.LANG_TYPE, AppUtils.getCurrentLanguageCode(DetailActivity.this));
                Log.v(TAG, "makeofferParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);

    }

    /**
     * Function  for checking item approval or not
     **/

    private void checkItemStatus() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CHECK_ITEM_STATUS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        if (DefensiveClass.optInt(json, Constants.TAG_ITEM_APPROVE).equals("0")) {
                            approveDialog(DetailActivity.this);
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
                map.put(Constants.TAG_ITEM_ID, itemMap.get(Constants.TAG_ID));
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for like_icon & unlike the product
     **/

    private void likeItem() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_ITEM_LIKE, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                likeCount.setOnClickListener(DetailActivity.this);
                likeImg.setOnClickListener(DetailActivity.this);
                try {
                    Log.v(TAG, "likeResponse=" + result);
                    JSONObject json = new JSONObject(result);
                    String status = json.getString(Constants.TAG_STATUS);
                    String results = json.getString(Constants.TAG_RESULT);

                    if (status.equals("true")) {

                    } else {
                        JoysaleApplication.dialog(DetailActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (ArrayIndexOutOfBoundsException e) {
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
                map.put(Constants.TAG_ITEM_ID, itemMap.get(Constants.TAG_ID));
                Log.v(TAG, "likeParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().getRequestQueue().cancelAll("Like");
        JoysaleApplication.getInstance().addToRequestQueue(req, "Like");
    }

    private void changeLikeStatus() {
        String flag = "no";
        String count = itemMap.get(Constants.TAG_LIKECOUNT);
        if (itemMap.get("liked").equals("no")) {
            count = Integer.toString((Integer.parseInt(count) + 1));
            flag = "yes";
            likeCount.setText(count + " " + getResources().getString(R.string.likes));
            likeImg.setImageResource(R.drawable.like_icon);
            itemMap.put(Constants.TAG_LIKECOUNT, count);
            itemMap.put("liked", "yes");
        } else {
            count = Integer.toString((Integer.parseInt(count) - 1));
            flag = "no";
            likeCount.setText(count + " " + getResources().getString(R.string.likes));
            likeImg.clearColorFilter();
            likeImg.setImageResource(R.drawable.unlike_icon);
            itemMap.put(Constants.TAG_LIKECOUNT, count);
            itemMap.put("liked", "no");
        }
        switch (from) {
            case "home":
                FragmentMainActivity.homeItemList.get(position).put(Constants.TAG_LIKECOUNT, count);
                FragmentMainActivity.homeItemList.get(position).put(Constants.TAG_LIKED, flag);
                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                break;
            case "mylisting":
                MyListing.addedItems.get(position).put(Constants.TAG_LIKECOUNT, count);
                MyListing.addedItems.get(position).put(Constants.TAG_LIKED, flag);
                MyListing.itemAdapter.notifyDataSetChanged();
                break;
            case "liked":
                LikedItems.likedItems.get(position).put(Constants.TAG_LIKECOUNT, count);
                LikedItems.likedItems.get(position).put(Constants.TAG_LIKED, flag);
                LikedItems.itemAdapter.notifyDataSetChanged();
                break;
            case "detail":
                MoreItems.get(position).put(Constants.TAG_LIKECOUNT, count);
                MoreItems.get(position).put(Constants.TAG_LIKED, flag);
                itemAdapter.notifyDataSetChanged();
                break;
        }
    }

    /**
     * Function for report and undo report
     */

    private void reportItem() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_REPORT_ITEM, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                String value = "";
                try {
                    JSONObject json = new JSONObject(result);
                    String status = json.getString(Constants.TAG_STATUS);
                    String message = json.getString(Constants.TAG_MESSAGE);
                    if (status.equalsIgnoreCase("true")) {
                        if (message.equalsIgnoreCase("Reported Successfully")) {
                            message = getString(R.string.reported_successfully);
                        } else {
                            message = getString(R.string.unreported_successfully);
                        }
                        Toast.makeText(DetailActivity.this, message, Toast.LENGTH_LONG).show();
                        if (itemMap.get(Constants.TAG_REPORT).equals("yes")) {
                            itemMap.put(Constants.TAG_REPORT, "no");
                            value = "no";
                        } else {
                            itemMap.put(Constants.TAG_REPORT, "yes");
                            value = "yes";
                        }
                        switch (from) {
                            case "home":
                                FragmentMainActivity.homeItemList.get(position).put(Constants.TAG_REPORT, value);
                                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                                break;
                            case "mylisting":
                                MyListing.addedItems.get(position).put(Constants.TAG_REPORT, value);
                                MyListing.itemAdapter.notifyDataSetChanged();
                                break;
                            case "liked":
                                LikedItems.likedItems.get(position).put(Constants.TAG_REPORT, value);
                                LikedItems.itemAdapter.notifyDataSetChanged();
                                break;
                            case "detail":
                                MoreItems.get(position).put(Constants.TAG_REPORT, value);
                                itemAdapter.notifyDataSetChanged();
                                break;
                        }
                    } else {
                        JoysaleApplication.dialog(DetailActivity.this, getString(R.string.alert), getString(R.string.somethingwrong));
                    }
                } catch (JSONException e) {
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
                map.put(Constants.TAG_ITEM_ID, itemMap.get(Constants.TAG_ID));
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);

    }

    /**
     * Function for get the promotional details of the product
     **/

    private void getPromotionDetails() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CHECK_PROMOTION, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                HashMap<String, String> map = new HashMap<String, String>();
                try {
                    JSONObject jobj = new JSONObject(json);
                    String response = jobj.getString(Constants.TAG_STATUS);

                    if (response.equalsIgnoreCase("true")) {

                        JSONObject temp = jobj.optJSONObject(Constants.TAG_RESULT);
                        if (temp != null) {
                            map.put(Constants.TAG_ID, DefensiveClass.optString(temp, Constants.TAG_ID));
                            map.put(Constants.TAG_PROMOTION_NAME, DefensiveClass.optString(temp, Constants.TAG_PROMOTION_NAME));
                            map.put(Constants.TAG_PAID_AMOUNT, DefensiveClass.optString(temp, Constants.TAG_PAID_AMOUNT));
                            map.put(Constants.TAG_CURRENCY_SYM, DefensiveClass.optString(temp, Constants.TAG_CURRENCY_SYM));
                            map.put(Constants.TAG_CURRENCY_CODE, DefensiveClass.optString(temp, Constants.TAG_CURRENCY_CODE));
                            map.put(Constants.TAG_UPTO, DefensiveClass.optString(temp, Constants.TAG_UPTO));
                            map.put(Constants.TAG_TRANSACTION_ID, DefensiveClass.optString(temp, Constants.TAG_TRANSACTION_ID));
                            map.put(Constants.TAG_STATUS, DefensiveClass.optString(temp, Constants.TAG_STATUS));
                            map.put(Constants.TAG_ITEM_ID, DefensiveClass.optString(temp, Constants.TAG_ITEM_ID));
                            map.put(Constants.TAG_ITEM_NAME, DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME));
                            map.put(Constants.TAG_ITEM_IMAGE, DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE));
                        }
                    }
                    Log.v("promotionAry", "promotionAry==" + map);
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (map.size() > 0) {
                        Intent j = new Intent(DetailActivity.this, PromotionDetail.class);
                        j.putExtra("data", map);
                        startActivity(j);
                    } else {
                        Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    }
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
                map.put(Constants.TAG_ITEM_ID, itemMap.get(Constants.TAG_ID));
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);

    }

    private void loadHomeItems() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEARCH_ITEM, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                ArrayList<HashMap<String, String>> HomeItems = new ArrayList<HashMap<String, String>>();
                ItemsParsing parse = new ItemsParsing(DetailActivity.this, GetSet.getUserId());
                HomeItems.addAll(parse.parsing(json));
                Log.v(TAG, "HomeItems=" + HomeItems);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if (HomeItems.size() == 0) {
                    Toast.makeText(DetailActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                } else {
                    itemMap.clear();
                    itemMap.putAll(HomeItems.get(0));
                    setData();
                    photosAry.clear();
                    getImageAry();
                    checkUser();
                    viewPager.setOnPageChangeListener(mOnPageChangeListener);
                    switch (from) {
                        case "home":
                            if (FragmentMainActivity.homeItemList.size() > 0) {
                                FragmentMainActivity.homeItemList.get(position).putAll(HomeItems.get(0));
                                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                            }
                            break;
                        case "mylisting":
                            if (MyListing.addedItems.size() > 0) {
                                MyListing.addedItems.get(position).putAll(HomeItems.get(0));
                                MyListing.itemAdapter.notifyDataSetChanged();
                            }
                            break;
                        case "liked":
                            if (LikedItems.likedItems.size() > 0) {
                                LikedItems.likedItems.get(position).putAll(HomeItems.get(0));
                                LikedItems.itemAdapter.notifyDataSetChanged();
                            }
                            break;
                        case "detail":
                            //  MoreItems.get(position).put(Constants.TAG_REPORT, value);
                            //  itemAdapter.notifyDataSetChanged();
                            break;
                    }
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
                map.put(Constants.TAG_ITEM_ID, itemMap.get(Constants.TAG_ID));
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);

    }

    /**
     * Method for get saved addresses
     **/

    private void getAddress() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_SHIPPING, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                ArrayList<HashMap<String, String>> addressAry = new ArrayList<>();
                HashMap<String, String> map;
                try {
                    Log.v(TAG, "getAddressesResponse=" + res);
                    JSONObject json = new JSONObject(res);
                    String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        JSONArray result = json.optJSONArray("result");
                        if (!(result == null)) {
                            for (int i = 0; i < result.length(); i++) {
                                map = new HashMap<String, String>();
                                JSONObject temp = result.getJSONObject(i);
                                map.put(Constants.TAG_SHIPPINGID, DefensiveClass.optInt(temp, Constants.TAG_SHIPPINGID));
                                map.put(Constants.TAG_NICKNAME, DefensiveClass.optString(temp, Constants.TAG_NICKNAME));
                                map.put(Constants.TAG_NAME, DefensiveClass.optString(temp, Constants.TAG_NAME));
                                map.put(Constants.TAG_COUNTRY, DefensiveClass.optString(temp, Constants.TAG_COUNTRY));
                                map.put(Constants.TAG_STATE, DefensiveClass.optString(temp, Constants.TAG_STATE));
                                map.put(Constants.TAG_ADDRESS1, DefensiveClass.optString(temp, Constants.TAG_ADDRESS1));
                                map.put(Constants.TAG_ADDRESS2, DefensiveClass.optString(temp, Constants.TAG_ADDRESS2));
                                map.put(Constants.TAG_CITY, DefensiveClass.optString(temp, Constants.TAG_CITY));
                                map.put(Constants.TAG_ZIPCODE, DefensiveClass.optString(temp, Constants.TAG_ZIPCODE));
                                map.put(Constants.TAG_PHONE, DefensiveClass.optString(temp, Constants.TAG_PHONE));
                                map.put(Constants.TAG_COUNTRYCODE, DefensiveClass.optString(temp, Constants.TAG_COUNTRYCODE));
                                map.put(Constants.TAG_DEFAULTSHIPPING, DefensiveClass.optString(temp, Constants.TAG_DEFAULTSHIPPING));

                                addressAry.add(map);
                            }
                        }
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        if (addressAry.size() > 0) {
                            int haveDefaultAddress = 0;
                            for (int i = 0; i < addressAry.size(); i++) {
                                if (addressAry.get(i).get(Constants.TAG_DEFAULTSHIPPING).equals("1")) {
                                    haveDefaultAddress = i;
                                    break;
                                }
                            }
                            /*if (haveDefaultAddress == 0) {
                                Intent l = new Intent(DetailActivity.this, Addresses.class);
                                l.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                l.putExtra(Constants.FROM, "checkout");
                                l.putExtra("shippingId", Integer.toString(haveDefaultAddress));
                                l.putExtra("itemData", itemMap);
                                startActivity(l);
                            } else {
                                Intent i = new Intent(DetailActivity.this, Checkout.class);
                                i.putExtra("itemData", itemMap);
                                i.putExtra("shippingData", addressAry.get(haveDefaultAddress));
                                startActivity(i);
                            }*/
                        }
                    } else if (response.equalsIgnoreCase("error")) {
                        JoysaleApplication.disabledialog(DetailActivity.this, json.optString(Constants.TAG_MESSAGE), GetSet.getUserId());
                    } else {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        /*Intent i = new Intent(DetailActivity.this, AddAddress.class);
                        i.putExtra(Constants.FROM, "checkout");
                        i.putExtra("to", "add");
                        i.putExtra("itemData", itemMap);
                        startActivity(i);*/
                    }
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
                map.put(Constants.TAG_ITEM_ID, itemMap.get(Constants.TAG_ID));
                Log.v(TAG, "getAddressesParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Onclick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shareBtn:
                Log.d(TAG, "product URL" + itemMap.get(Constants.TAG_PROURL));
                Intent g = new Intent(Intent.ACTION_SEND);
                g.setType("text/plain");
                g.putExtra(Intent.EXTRA_TEXT, itemMap.get(Constants.TAG_PROURL));
                startActivity(Intent.createChooser(g, "Share"));
                break;
            case R.id.backbtn:
                //MoreItems.clear();
                finish();
                break;
            case R.id.edit:
                Intent a = new Intent(DetailActivity.this, AddProductDetail.class);
                a.putExtra(Constants.FROM, "edit");
                a.putExtra("data", itemMap);
                startActivity(a);
                break;
            case R.id.chat:
                if (GetSet.isLogged()) {
                    if (JoysaleApplication.isNetworkAvailable(DetailActivity.this)) {
                        chat.setOnClickListener(null);
                        chatClicked = true;
                        initializeGetChat();
                        getChatId("chat", "", "");
                    } else {
                        JoysaleApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                    }

                } else {
                    Intent i = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.commentCount:
                Intent c = new Intent(DetailActivity.this, CommentsActivity.class);
                c.putExtra("itemId", itemMap.get(Constants.TAG_ID));
                c.putExtra("position", position);
                c.putExtra(Constants.FROM, from);
                c.putExtra("productName", itemMap.get(Constants.TAG_TITLE));
                c.putExtra("productImage", itemMap.get(Constants.TAG_ITEM_URL_350));
                startActivity(c);
                break;
            case R.id.settingBtn:
                shareImage(v);
                break;
            case R.id.offer:
                if (GetSet.isLogged()) {
                    if (isSeller) {
                        if (itemMap.get(Constants.TAG_ITEM_STATUS).equalsIgnoreCase("sold")) {
                            confirmdialog(getString(R.string.back_sale_confirmation));
                        } else {
                            if (itemMap.get(Constants.TAG_PROMOTION_TYPE).equals("Normal")) {
                                Intent i = new Intent(DetailActivity.this, CreatePromote.class);
                                i.putExtra("itemId", itemMap.get(Constants.TAG_ID));
                                startActivity(i);
                            } else {
                                progressDialog.setMessage(getString(R.string.pleasewait));
                                progressDialog.setCancelable(false);
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.show();
                                getPromotionDetails();
                            }
                        }
                    } else {
                        if (Constants.BUYNOW) {
                            progressDialog.setMessage(getString(R.string.pleasewait));
                            progressDialog.setCancelable(false);
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();
                            getAddress();
                            //new getAddress().execute();
                        } else {
                            dialog(getString(R.string.make_an_offer), itemMap.get(Constants.TAG_SELLERIMG));
                        }
                    }
                } else {
                    Intent i = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.likesImg:
            case R.id.likereditBtn:
                if (GetSet.isLogged()) {
                    if (JoysaleApplication.isNetworkAvailable(DetailActivity.this)) {
                        if (itemMap.get("liked").equals("no")) {
                            changeLikeStatus();
                            likeItem();
                        } else {
                            changeLikeStatus();
                            likeItem();
                        }
                    } else {
                        JoysaleApplication.dialog(DetailActivity.this, getResources().getString(R.string.error), getResources().getString(R.string.checkconnection));
                    }

                } else {
                    Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(j);
                }
                break;
            case R.id.userImage:
                Intent u = new Intent(DetailActivity.this, Profile.class);
                u.putExtra(Constants.TAG_USER_ID, itemMap.get(Constants.TAG_SELLERID));
                startActivity(u);
                break;
            case R.id.call:
                if (GetSet.isLogged()) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + itemMap.get(Constants.TAG_MOBILE_NO)));
                    if (ActivityCompat.checkSelfPermission(DetailActivity.this,
                            CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailActivity.this, new String[]{CALL_PHONE}, 101);
                    } else {
                        fromCall = true;
                        startActivity(callIntent);
                    }
                } else {
                    Intent j = new Intent(DetailActivity.this, WelcomeActivity.class);
                    startActivity(j);
                }
                break;
        }
    }

}
