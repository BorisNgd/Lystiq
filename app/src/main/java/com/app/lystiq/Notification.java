package com.app.lystiq;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.ItemsParsing;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 30/6/16.
 * <p>
 * This class is for Notification of all Messages.
 */

public class Notification extends AppCompatActivity implements AbsListView.OnScrollListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * Declare Layout Elements
     **/
    ListView listView;
    ImageView backbtn;
    TextView title;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    SwipeRefreshLayout swipeLayout;
    ProgressDialog dialog;

    AdapterForHdpi adapter;
    ArrayList<HashMap<String, String>> NotifyAry = new ArrayList<HashMap<String, String>>();

    /**
     * Declare Variables
     **/
    String TAG = "Notification";
    int visibleThreshold = 0, previousTotal = 0, currentPage = 0;
    boolean loading = true, pulldown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JoysaleApplication.setLanguage(this);

        setContentView(R.layout.notification_list);

        listView = (ListView) findViewById(R.id.listView);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);

        title.setText(getString(R.string.notifications));

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));
        listView.setOnScrollListener(this);
        swipeLayout.setOnRefreshListener(this);

        // For Set Login & Logout State
        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
        Log.i(TAG, "onCreate: "+JoysaleApplication.isRTL(getApplicationContext()));
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
        }

        dialog = new ProgressDialog(Notification.this);

        initializeUI();

        // to get notification from Api
        getNotification(0);

        //To initialize the Adapter
        adapter = new AdapterForHdpi(Notification.this, NotifyAry);
        listView.setAdapter(adapter);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    //To parse jsonObject to ArrayList
    private ArrayList<HashMap<String, String>> parsing(String json) {
        ArrayList<HashMap<String, String>> notifyAry = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject jobj = new JSONObject(json);
            String response = jobj.getString(Constants.TAG_STATUS);

            if (response.equalsIgnoreCase("true")) {

                JSONArray result = jobj
                        .optJSONArray(Constants.TAG_RESULT);
                if (result != null) {
                    for (int i = 0; i < result.length(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        JSONObject temp = result.getJSONObject(i);
                        String type = DefensiveClass.optString(temp, Constants.TAG_TYPE);

                        map.put(Constants.TAG_TYPE, type);

                        if (type.equals("add") || type.equals("myoffer") || type.equals("like") || type.equals("exchange")
                                || type.equals("comment") || type.equals("myoffer")) {
                            map.put(Constants.TAG_MESSAGE, DefensiveClass.optString(temp, Constants.TAG_MESSAGE));
                            map.put(Constants.TAG_EVENTTIME, DefensiveClass.optString(temp, Constants.TAG_EVENTTIME));
                            map.put(Constants.TAG_USERIMAGE, DefensiveClass.optString(temp, Constants.TAG_USERIMAGE));
                            map.put(Constants.TAG_USERID, DefensiveClass.optString(temp, Constants.TAG_USERID));
                            map.put(Constants.TAG_USERNAME, DefensiveClass.optString(temp, Constants.TAG_USERNAME));
                            map.put(Constants.TAG_ITEM_ID, DefensiveClass.optString(temp, Constants.TAG_ITEM_ID));
                            map.put(Constants.TAG_ITEM_TITLE, DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE));
                            map.put(Constants.TAG_ITEM_IMAGE, DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE));
                        } else if (type.equals("admin") || type.equals("adminpayment")) {
                            map.put(Constants.TAG_MESSAGE, DefensiveClass.optString(temp, Constants.TAG_MESSAGE));
                            map.put(Constants.TAG_EVENTTIME, DefensiveClass.optString(temp, Constants.TAG_EVENTTIME));
                            map.put(Constants.TAG_USERIMAGE, DefensiveClass.optString(temp, Constants.TAG_USERIMAGE));
                        } else if (type.equals("follow") || type.equals("order")) {
                            map.put(Constants.TAG_MESSAGE, DefensiveClass.optString(temp, Constants.TAG_MESSAGE));
                            map.put(Constants.TAG_EVENTTIME, DefensiveClass.optString(temp, Constants.TAG_EVENTTIME));
                            map.put(Constants.TAG_USERIMAGE, DefensiveClass.optString(temp, Constants.TAG_USERIMAGE));
                            map.put(Constants.TAG_USERID, DefensiveClass.optString(temp, Constants.TAG_USERID));
                            map.put(Constants.TAG_USERNAME, DefensiveClass.optString(temp, Constants.TAG_USERNAME));
                        }
                        notifyAry.add(map);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notifyAry;
    }

    //To change text changes for Notification
    private String translateNotification(String message) {
        String msg = "";
        if (message.contains("start Following you")) {
            msg = message.replace("start Following you", getString(R.string.start_following_you));
        } else if (message.contains("added a product")) {
            msg = message.replace("added a product", getString(R.string.added_a_product));
        } else if (message.contains("liked your product")) {
            msg = message.replace("liked your product", getString(R.string.liked_your_product));
        } else if (message.contains("comment on your product")) {
            msg = message.replace("comment on your product", getString(R.string.comment_on_your_product));
        } else if (message.contains("sent offer request")) {
            msg = message.replace("sent offer request", getString(R.string.sent_offer_request)).
                    replace("on your product", getString(R.string.on_your_product));
        } else if (message.contains("sent exchange request to your product")) {
            msg = message.replace("sent exchange request to your product", getString(R.string.sent_exchange_request_to_your_product));
        } else if (message.contains("accepted your exchange request on")) {
            msg = message.replace("accepted your exchange request on", getString(R.string.accepted_your_exchange_request_on));
        } else if (message.contains("declined your exchange request on")) {
            msg = message.replace("declined your exchange request on", getString(R.string.declined_your_exchange_request_on));
        } else if (message.contains("canceled your exchange request on")) {
            msg = message.replace("canceled your exchange request on", getString(R.string.canceled_your_exchange_request_on));
        } else if (message.contains("successed your exchange request on")) {
            msg = message.replace("successed your exchange request on", getString(R.string.successed_your_exchange_request_on));
        } else if (message.contains("failed your exchange request on")) {
            msg = message.replace("failed your exchange request on", getString(R.string.failed_your_exchange_request_on));
        } else if (message.contains("contacted you on your product")) {
            msg = message.replace("contacted you on your product", getString(R.string.contacted_you_on_your_product));
        } else if (message.contains("sent message")) {
            msg = message.replace("sent message", getString(R.string.sent_message));
        } else if (message.contains("placed an order in your shop, Order Id :")) {
            msg = message.replace("placed an order in your shop, Order Id :", getString(R.string.placed_an_order_in_your_shop));
        } else if (message.contains("your order has been cancelled Order Id :")) {
            msg = message.replace("your order has been cancelled Order Id :", getString(R.string.your_order_has_been_cancelled));
        } else if (message.contains("added tracking details for your order. Order Id :")) {
            msg = message.replace("added tracking details for your order. Order Id :", getString(R.string.added_tracking_details_for_your_order));
        } else if (message.contains("has marked your order as delivered. Order Id :")) {
            msg = message.replace("has marked your order as delivered. Order Id :", getString(R.string.has_marked_your_order_as_delivered));
        } else if (message.contains("paid the amount for your order. Order Id :")) {
            msg = message.replace("paid the amount for your order. Order Id :", getString(R.string.paid_the_amount_for_your_order));
        } else if (message.contains("refunded the amount for your order. Order Id :")) {
            msg = message.replace("refunded the amount for your order. Order Id :", getString(R.string.refunded_the_amount_for_your_order));
        } else if (message.contains("You have promoted your product")) {
            msg = message.replace("You have promoted your product", getString(R.string.you_have_promoted_your_product)).replace("by", getString(R.string.by));
        } else if (message.contains("your order has been marked as shipped Order Id :")) {
            msg = message.replace("your order has been marked as shipped Order Id :", getString(R.string.your_order_has_been_shipped));
        } else if (message.contains("your order has been marked as processing Order Id :")) {
            msg = message.replace("your order has been marked as processing Order Id :", getString(R.string.your_order_has_been_processing));
        } else if (message.contains("your order has been marked as delivered Order Id :")) {
            msg = message.replace("your order has been marked as delivered Order Id :", getString(R.string.your_order_has_been_delivered));
        } else {
            msg = message;
        }
        return msg;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
                currentPage++;
            }
        }

        if (!loading
                && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // I load the next page of thumbnails using a background task,
            if (currentPage != 0) {
                initializeUI();
                getNotification(currentPage);
                loading = true;
            }
        }
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(Notification.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(Notification.this);
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
            initializeUI();
            getNotification(0);
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * get the notification for user recent activities
     **/
    private void getNotification(final int pageCount) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_NOTIFICATIONS, new Response.Listener<String>() {
            @Override
            public void onResponse(final String json) {

                Log.v(TAG, "notificationResponse=" + json);

                if (pulldown) {
                    NotifyAry.clear();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                        temp.addAll(parsing(json));
                        if (!NotifyAry.contains(temp)) {
                            NotifyAry.addAll(temp);
                        }
                        Log.v(TAG, "NotifyAry" + NotifyAry);
                    }
                });

                if (pulldown) {
                    pulldown = false;
                    loading = true;
                }

                progress.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                swipeLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();

                if (NotifyAry.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    nullLay.setVisibility(View.GONE);
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
                map.put(Constants.TAG_USER_ID, GetSet.getUserId());
                map.put(Constants.TAG_OFFSET, Integer.toString(offset));
                map.put(Constants.TAG_LIMIT, "20");
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    private void initializeUI() {
        nullLay.setVisibility(View.INVISIBLE);
        if (pulldown) {
            listView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else if (NotifyAry.size() > 0) {
            listView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            swipeRefresh();
        } else {
            listView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    public class AdapterForHdpi extends BaseAdapter {

        Context context;
        ViewHolder holder = null;
        private ArrayList<HashMap<String, String>> dataNotifi;

        public AdapterForHdpi(Context ctx, ArrayList<HashMap<String, String>> data) {
            context = ctx;
            dataNotifi = data;
        }

        @Override
        public int getCount() {
            return dataNotifi.size();
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
                LayoutInflater mInflater = (LayoutInflater) context
                        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.notify_listitem, parent, false);
                holder = new ViewHolder();

                holder.user_name = (TextView) convertView
                        .findViewById(R.id.username);
                holder.time = (TextView) convertView.findViewById(R.id.date);
                holder.userImage = (ImageView) convertView
                        .findViewById(R.id.userimg);
                holder.mainLay = (RelativeLayout) convertView
                        .findViewById(R.id.mainLay);
                holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                final HashMap<String, String> tempMap = dataNotifi.get(position);
                String type = tempMap.get(Constants.TAG_TYPE);

                if (JoysaleApplication.isRTL(context)) {
                    holder.arrow.setRotation(180);
                } else {
                    holder.arrow.setRotation(0);
                }

                Picasso.with(Notification.this).load(tempMap.get(Constants.TAG_USERIMAGE)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);

                if (type.equals("admin") || type.equals("adminpayment")) {
                    String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.green_color))) + "'>" + getString(R.string.app_name) + "</font> " + getString(R.string.sent_message) + " " + translateNotification(tempMap.get(Constants.TAG_MESSAGE)) + "'";
                    holder.user_name.setText(Html.fromHtml(name));
                    holder.arrow.setVisibility(View.INVISIBLE);
                } else if (type.equals("follow") || type.equals("order")) {
                    String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.green_color))) + "'>" + tempMap.get(Constants.TAG_USERNAME) + "</font>" + " " + translateNotification(tempMap.get(Constants.TAG_MESSAGE));
                    holder.user_name.setText(Html.fromHtml(name));
                    holder.arrow.setVisibility(View.VISIBLE);
                } else {
                    String name = "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.green_color))) + "'>" + tempMap.get(Constants.TAG_USERNAME) + "</font>" + " " + translateNotification(tempMap.get(Constants.TAG_MESSAGE))
                            + " " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.primaryText))) + "'>" + tempMap.get(Constants.TAG_ITEM_TITLE) + "</font>";
                    holder.user_name.setText(Html.fromHtml(name));
                    holder.arrow.setVisibility(View.VISIBLE);
                }

                holder.userImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!tempMap.get(Constants.TAG_TYPE).equals("admin") && !tempMap.get(Constants.TAG_TYPE).equals("adminpayment")) {
                            Intent i = new Intent(Notification.this, Profile.class);
                            i.putExtra(Constants.TAG_USER_ID, tempMap.get(Constants.TAG_USERID));
                            startActivity(i);
                        }
                    }
                });

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.mainLay.setOnClickListener(null);
                        String type = tempMap.get(Constants.TAG_TYPE);
                        switch (type) {
                            case "add":
                            case "like":
                            case "comment":
                                try {
                                    dialog.setMessage(getString(R.string.pleasewait));
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.show();
                                    loadHomeItems(tempMap.get(Constants.TAG_ITEM_ID));
                                    holder.mainLay.setOnClickListener(this);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "follow": {
                                Intent i = new Intent(Notification.this, Profile.class);
                                i.putExtra(Constants.TAG_USER_ID, tempMap.get(Constants.TAG_USERID));
                                startActivity(i);
                                break;
                            }
                            case "myoffer": {
                                Intent i = new Intent(Notification.this, MessageActivity.class);
                                startActivity(i);
                                break;
                            }
                            case "exchange": {
                                Intent i = new Intent(Notification.this, ExchangeActivity.class);
                                startActivity(i);
                                break;
                            }
                            case "order":
                                /*Intent k = new Intent(Notification.this, MySalesnOrder.class);
                                startActivity(k);*/
                                break;
                        }
                    }
                });

                long timestamp = 0;
                String time = tempMap.get(Constants.TAG_EVENTTIME);
                if (time != null) {
                    timestamp = Long.parseLong(time);
                }
                holder.time.setText(JoysaleApplication.getDate(Notification.this, timestamp));

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
            ImageView userImage, arrow;
            TextView user_name, time;
            RelativeLayout mainLay;
        }
    }

    // To searchbyitem from Api
    private void loadHomeItems(final String itemId) {
        final ArrayList<HashMap<String, String>> HomeItems = new ArrayList<HashMap<String, String>>();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEARCH_ITEM, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {

                ItemsParsing parse = new ItemsParsing(Notification.this, GetSet.getUserId());
                HomeItems.addAll(parse.parsing(json));

                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (HomeItems.size() == 0) {
                    Toast.makeText(Notification.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(Notification.this, DetailActivity.class);
                    i.putExtra(Constants.DATA, HomeItems.get(0));
                    i.putExtra(Constants.POSITION, 0);
                    i.putExtra(Constants.FROM, "notification");
                    startActivity(i);
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
                map.put(Constants.TAG_ITEM_ID, itemId);
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }
}