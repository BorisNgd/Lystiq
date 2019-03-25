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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.external.ImagePicker;
import com.app.helper.ImageCompression;
import com.app.helper.ImageStorage;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.ItemsParsing;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft.
 * <p>
 * This class is for User Chat
 */

public class ChatActivity extends Activity implements OnClickListener, TextWatcher, OnScrollListener {

    /**
     * Declare Layout Elements
     **/
    ImageView backBtn, userImg, settingbtn;
    LinearLayout shareImg, sharelocation, send, bottom, sendMsgLay;
    TextView title, nullText, dateTxt, blockMsg;
    ListView listView;
    AVLoadingIndicatorView progress, topProgress, typing;
    EditText editText;
    ViewGroup header, footer;
    RelativeLayout main, blockUserLay;
    RecyclerView recyclerView;
    ProgressDialog dialog;

    RecyclerAdapter recyclerAdapter;
    ChatAdapter chatAdapter;
    Handler handler = new Handler();
    Runnable runnable;
    private Socket mSocket;

    /**
     * Declare Variables
     **/
    public static ArrayList<HashMap<String, String>> templatMsgAry = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> chats = new ArrayList<HashMap<String, String>>(), tempAry = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> data = new HashMap<String, String>();
    ArrayList<String> values = new ArrayList<>();
    HashMap<String, String> offerProdMap = new HashMap<>();
    static final String TAG = "ChatActivity";
    public static final int LOCATION_FETCH_ACTION = 1000;
    public static String fullName = "";
    String userName, userId, chatId, imageType = "local", userImage, existingFileName, chatUrl = "", from = "";
    boolean pulldown = false, loading = false, aboutMessageSent = false, meTyping, receiverTyping,isuserBlocked=false;
    int black, currentPage = 0, topPadding, leftPadding, rightPadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        try {
            backBtn = (ImageView) findViewById(R.id.backbtn);
            title = (TextView) findViewById(R.id.username);
            shareImg = (LinearLayout) findViewById(R.id.shareImg);
            sharelocation = (LinearLayout) findViewById(R.id.sharelocation);
            listView = (ListView) findViewById(R.id.listView);
            send = (LinearLayout) findViewById(R.id.send);
            editText = (EditText) findViewById(R.id.editText);
            progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
            main = (RelativeLayout) findViewById(R.id.main);
            userImg = (ImageView) findViewById(R.id.userImg);
            dateTxt = (TextView) findViewById(R.id.dateTxt);
            bottom = (LinearLayout) findViewById(R.id.bottom);
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            sendMsgLay = (LinearLayout) findViewById(R.id.sendMsgLay);
            settingbtn = (ImageView) findViewById(R.id.settingbtn);
            dialog = new ProgressDialog(ChatActivity.this,R.style.AppCompatAlertDialogStyle);
            blockUserLay = (RelativeLayout) findViewById(R.id.blockUserLay);
            blockMsg = (TextView) findViewById(R.id.blockMsg);

            header = (ViewGroup) getLayoutInflater().inflate(R.layout.chat_header, null);
            listView.addHeaderView(header);

            dialog.setMessage(getString(R.string.pleasewait));
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Drawable drawable = new ProgressBar(ChatActivity.this).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(ChatActivity.this, R.color.progressColor),
                        PorterDuff.Mode.SRC_IN);
                dialog.setIndeterminateDrawable(drawable);
            }

            footer = (ViewGroup) getLayoutInflater().inflate(R.layout.chat_footer, null);
            listView.addFooterView(footer);
            listView.setSmoothScrollbarEnabled(true);
            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

            topProgress = (AVLoadingIndicatorView) header.findViewById(R.id.topProgress);
            nullText = (TextView) header.findViewById(R.id.nulltext);
            typing = (AVLoadingIndicatorView) footer.findViewById(R.id.typing);

            backBtn.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);
            topProgress.setVisibility(View.GONE);
            nullText.setVisibility(View.GONE);
            typing.setVisibility(View.GONE);
            userImg.setVisibility(View.VISIBLE);
            dateTxt.setVisibility(View.GONE);
            settingbtn.setVisibility(View.VISIBLE);

            topPadding = JoysaleApplication.dpToPx(this, 10);
            leftPadding = JoysaleApplication.dpToPx(this, 18);
            rightPadding = JoysaleApplication.dpToPx(this, 12);

            LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerAdapter = new RecyclerAdapter(templatMsgAry);
            DividerItemDecoration itemDecorator = new DividerItemDecoration(ChatActivity.this, DividerItemDecoration.HORIZONTAL);
            itemDecorator.setDrawable(ContextCompat.getDrawable(ChatActivity.this, R.drawable.chat_template_divider));
            recyclerView.addItemDecoration(itemDecorator);
            recyclerView.setAdapter(recyclerAdapter);

            userName = getIntent().getExtras().getString(Constants.TAG_USERNAME);
            userId = getIntent().getExtras().getString(Constants.TAG_USER_ID);
            chatId = getIntent().getExtras().getString(Constants.CHATID);
            userImage = getIntent().getExtras().getString(Constants.TAG_USERIMAGE_M);
            from = getIntent().getExtras().getString(Constants.FROM);
            fullName = getIntent().getExtras().getString(Constants.TAG_FULL_NAME);

            blockMsg.setText(getString(R.string.block_user_msg));

            if (from.equals("detail")) {
                data = (HashMap<String, String>) getIntent().getExtras().get("data");
            }

            black = getResources().getColor(R.color.black);

            /** Method for join the user to chat **/

            JSONObject jobj = new JSONObject();
            try {
                jobj.put("joinid", GetSet.getUserName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JoysaleApplication app = (JoysaleApplication) getApplication();
            mSocket = app.getSocket();
            mSocket.on("message", onMessage);
            mSocket.on("messageTyping", onTyping);
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_CONNECT", "EVENT_CONNECT");
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_DISCONNECT", "EVENT_DISCONNECT");
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_CONNECT_ERROR", "EVENT_CONNECT_ERROR="+args[0]);
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("EVENT_ERROR", "EVENT_ERROR");
                }
            });
            mSocket.connect();

            mSocket.emit("join", jobj);

            backBtn.setOnClickListener(this);
            send.setOnClickListener(this);
            shareImg.setOnClickListener(this);
            sharelocation.setOnClickListener(this);
            editText.addTextChangedListener(this);
            settingbtn.setOnClickListener(this);
            userImg.setOnClickListener(this);
            editText.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER});
            chatAdapter = new ChatAdapter(ChatActivity.this, chats);
            listView.setAdapter(chatAdapter);

            Log.v(TAG, "userName=" + fullName);
            Log.v(TAG, "userImage=" + userImage);
            Picasso.with(ChatActivity.this).load(userImage).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImg);
            title.setText(fullName);

            values.add(getString(R.string.safety_tips));
            values.add(getString(R.string.block_user));

            initializeChat();
            getChat(0);

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function for receiving the instant messages & typing status
     **/

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.v("onTyping", "onTyping="+args[0]);
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        if (data.getString(Constants.SOCK_RECEIVER).equals(userName) && data.getString(Constants.TAG_MESSAGE).equals(Constants.TAG_TYPE)) {
                            if (!receiverTyping) {
                                receiverTyping = true;
                                typing.setVisibility(View.VISIBLE);
                                if (chats.size() > 0) {
                                    listView.setSelection(chats.size() - 1);
                                }
                                typing.startAnimation(AnimationUtils.loadAnimation(ChatActivity.this, R.anim.abc_slide_in_bottom));
                            }
                        } else {
                            receiverTyping = false;
                            typing.setVisibility(View.GONE);
                            typing.startAnimation(AnimationUtils.loadAnimation(ChatActivity.this, R.anim.abc_slide_out_bottom));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.v("onMessage", "onMessage="+args[0]);
            JSONObject data = (JSONObject) args[0];
            try {
                HashMap<String, String> hmap = new HashMap<String, String>();
                hmap.put(Constants.TAG_SENDER, data.getString(Constants.TAG_RECEIVER));
                hmap.put(Constants.DATE, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_CHATTIME));
                if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.SOCK_VIEW_URL).equals("")) {
                    hmap.put(Constants.TAG_TYPE, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_TYPE));
                    hmap.put(Constants.TAG_UPLOADED_IMG_URL, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.SOCK_VIEW_URL));
                } else if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LAT).equals("")) {
                    hmap.put(Constants.TAG_USR_LATITUDE, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LAT));
                    hmap.put(Constants.TAG_USR_LONGITUDE, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_LON));
                    hmap.put(Constants.TAG_TYPE, "share_location");
                } else if (!data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_MESSAGE).equals("")) {
                    hmap.put(Constants.TAG_MESSAGE, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_MESSAGE));
                    hmap.put(Constants.TAG_TYPE, "message");
                } else {
                    hmap.put(Constants.TAG_TYPE, "offer");
                    hmap.put(Constants.TAG_SENDER, data.getString(Constants.TAG_SENDER));
                    hmap.put(Constants.TAG_RECEIVER, data.getString(Constants.TAG_RECEIVER));
                    hmap.put(Constants.TAG_ITEM_ID, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_ITEM_ID));
                    hmap.put(Constants.TAG_ITEM_IMAGE, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_ITEM_IMAGE));
                    hmap.put(Constants.TAG_OFFER_ID, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_ID));
                    hmap.put(Constants.TAG_OFFER_PRICE, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_PRICE));
                    hmap.put(Constants.TAG_OFFER_TYPE, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_TYPE));
                    hmap.put(Constants.TAG_OFFER_STATUS, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_STATUS));
                    hmap.put(Constants.TAG_BUYNOW_STATUS, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_BUYNOW_STATUS));
                    hmap.put(Constants.TAG_INSTANT_BUY, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_INSTANT_BUY));
                    hmap.put(Constants.TAG_OFFER_CURRENCY_SYMBOL, data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_OFFER_CURRENCY_SYMBOL));
                    hmap.put(Constants.TAG_ITEM_STATUS,"1");
                    //hmap.put(Constants.TAG_ITEM_STATUS,data.getJSONObject(Constants.TAG_MESSAGE).getString(Constants.TAG_ITEM_STATUS));
                }

                Log.v(TAG, "chatdatafromsocket=" + hmap);
                if(data.getString(Constants.TAG_RECEIVER).equals(userName)) {
                    chats.add(hmap);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void disconnectSocket(){
        if (mSocket != null) {
            mSocket.off("message");
            mSocket.off("messageTyping");
            mSocket.disconnect();
        }
    }

    /**
     * Function to get a Time from Time Stamp
     */

    public static String getTime(long timeStamp) {
        try {
            DateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(ChatActivity.this);
        super.onPause();
        fullName = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(ChatActivity.this);
        fullName = getIntent().getExtras().getString(Constants.TAG_FULL_NAME);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fullName = "";
        disconnectSocket();
        if (from.equals("message")) {
            initializeChat();
            getChat(0);
            MessageActivity.fromChat = true;
        }

        JoysaleApplication.hideSoftKeyboard(ChatActivity.this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            Log.v(TAG, "scrolling stopped...");
            dateTxt.setVisibility(View.GONE);
        } else {
            if (dateTxt.getVisibility() != View.VISIBLE) {
                dateTxt.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        try {
            String chatDate = JoysaleApplication.getDate(ChatActivity.this, Long.parseLong(chats.get(firstVisibleItem).get(Constants.DATE)));
            dateTxt.setText(chatDate);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (firstVisibleItem == 0 && !(loading)) {
            loading = true;
            topProgress.setVisibility(View.VISIBLE);
            dateTxt.setVisibility(View.GONE);
            nullText.setVisibility(View.GONE);
            currentPage++;
            pulldown = true;
            if (JoysaleApplication.isNetworkAvailable(ChatActivity.this)) {
                initializeChat();
                getChat(currentPage);
            }
        }
    }

    /**
     * Function for Json Parsing
     */

    private ArrayList<HashMap<String, String>> parsing(String url) {
        boolean isUsrBlocked = false, isUsrBlockedByMe = false;
        ArrayList<HashMap<String, String>> chats = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject json = new JSONObject(url);
            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {

                chatUrl = DefensiveClass.optString(json, Constants.TAG_CHAT_URL);
                JSONObject chobj = json.optJSONObject(Constants.TAG_CHATS);
                if (chobj != null) {
                    JSONArray chat = chobj.optJSONArray(Constants.TAG_CHATS);
                    if (chat != null) {
                        for (int i = 0; i < chat.length(); i++) {
                            HashMap<String, String> map = new HashMap<String, String>();
                            JSONObject temp = chat.getJSONObject(i);
                            map.put(Constants.TAG_SENDER, DefensiveClass.optString(temp, Constants.TAG_SENDER));
                            map.put(Constants.TAG_RECEIVER, DefensiveClass.optString(temp, Constants.TAG_RECEIVER));
                            map.put(Constants.TAG_TYPE, DefensiveClass.optString(temp, Constants.TAG_TYPE));
                            map.put(Constants.TAG_ITEM_ID, DefensiveClass.optString(temp, Constants.TAG_ITEM_ID));
                            map.put(Constants.TAG_ITEM_TITLE, DefensiveClass.optString(temp, Constants.TAG_ITEM_TITLE));
                            map.put(Constants.TAG_ITEM_IMAGE, DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE));
                            map.put(Constants.TAG_OFFER_ID, DefensiveClass.optString(temp, Constants.TAG_OFFER_ID));
                            map.put(Constants.TAG_OFFER_PRICE, DefensiveClass.optString(temp, Constants.TAG_OFFER_PRICE));
                            map.put(Constants.TAG_OFFER_TYPE, DefensiveClass.optString(temp, Constants.TAG_OFFER_TYPE));
                            map.put(Constants.TAG_OFFER_STATUS, DefensiveClass.optString(temp, Constants.TAG_OFFER_STATUS));
                            map.put(Constants.TAG_BUYNOW_STATUS, DefensiveClass.optString(temp, Constants.TAG_BUYNOW_STATUS));
                            map.put(Constants.TAG_INSTANT_BUY, DefensiveClass.optString(temp, Constants.TAG_INSTANT_BUY));
                            map.put(Constants.TAG_ITEM_STATUS, DefensiveClass.optString(temp, Constants.TAG_ITEM_STATUS));
                            map.put(Constants.TAG_OFFER_CURRENCY_SYMBOL, DefensiveClass.optString(temp, Constants.TAG_OFFER_CURRENCY_SYMBOL));
                            map.put(Constants.TAG_CHAT_ID, DefensiveClass.optString(temp, Constants.TAG_CHAT_ID));

                            JSONObject msg = temp.getJSONObject(Constants.TAG_MESSAGE);
                            map.put(Constants.TAG_MESSAGE, DefensiveClass.optString(msg, Constants.TAG_MESSAGE));
                            map.put(Constants.DATE, DefensiveClass.optString(msg, Constants.TAG_CHATTIME));
                            map.put(Constants.TAG_USR_LATITUDE, DefensiveClass.optString(msg, Constants.TAG_USR_LATITUDE));
                            map.put(Constants.TAG_USR_LONGITUDE, DefensiveClass.optString(msg, Constants.TAG_USR_LONGITUDE));
                            map.put(Constants.TAG_UPLOADED_IMG_URL, DefensiveClass.optString(msg, Constants.TAG_UPLOADED_IMG_URL));

                            chats.add(map);
                            new DownloadAndStoreImg(DefensiveClass.optString(temp, Constants.TAG_TYPE), AppUtils.getValidUrl(DefensiveClass.optString(msg, Constants.TAG_UPLOADED_IMG_URL)), DefensiveClass.optString(msg, Constants.TAG_CHATTIME)).execute();
                        }

                        if (DefensiveClass.optString(json, Constants.TAG_BLOCK) != "" || DefensiveClass.optString(json, Constants.TAG_BLOCKED_My_ME) != "") {
                            isUsrBlocked = Boolean.valueOf(DefensiveClass.optString(json, Constants.TAG_BLOCK));
                            isUsrBlockedByMe = Boolean.valueOf(DefensiveClass.optString(json, Constants.TAG_BLOCKED_My_ME));
                        }

                        blockChat(isUsrBlocked, isUsrBlockedByMe);
                    }
                }
            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("false")) {
                if (DefensiveClass.optString(json, Constants.TAG_BLOCK) != "" || DefensiveClass.optString(json, Constants.TAG_BLOCKED_My_ME) != "") {
                    isUsrBlocked = Boolean.valueOf(DefensiveClass.optString(json, Constants.TAG_BLOCK));
                    isUsrBlockedByMe = Boolean.valueOf(DefensiveClass.optString(json, Constants.TAG_BLOCKED_My_ME));
                }
                //Toast.makeText(this, json.optString(Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                blockChat(isUsrBlocked, isUsrBlockedByMe);
            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                JoysaleApplication.disabledialog(ChatActivity.this, json.optString(Constants.TAG_MESSAGE), userId);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chats;
    }

    /**
     * Function for get the conversation between the selected user
     **/

    private void getChat(final int pageCount) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_CHAT, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    if (mSocket.connected()){
                        Log.v("mSocket", "connected");
                    } else {
                        Log.v("mSocket", "not connected");
                    }
                    Log.v(TAG, "getchatResponse=" + res);
                    tempAry.clear();
                    tempAry.addAll(parsing(res));
                    Collections.reverse(tempAry);
                    ArrayList<HashMap<String, String>> backup = new ArrayList<HashMap<String, String>>();
                    backup.addAll(chats);
                    chats.clear();
                    chats.addAll(tempAry);
                    chats.addAll(backup);
                    if (tempAry.size() == 0) {
                        listView.setOnScrollListener(null);
                    } else {
                        listView.setOnScrollListener(ChatActivity.this);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (pulldown) {
                                pulldown = false;
                                listView.setSelection(chats.size() - 1);
                                chatAdapter.notifyDataSetChanged();
                                listView.setSelection(tempAry.size());

                            } else {
                                chatAdapter.notifyDataSetChanged();
                                if (chats.size() > 0) {
                                    listView.setSelection(chats.size() - 1);
                                }
                            }
                        }
                    });

                    if (chats.size() == 0) {
                        dateTxt.setVisibility(View.GONE);
                    } else {
                        dateTxt.setVisibility(View.VISIBLE);
                    }

                    if (chats.size() > 18) {
                        if (tempAry.size() == 0) {
                            nullText.setVisibility(View.GONE);
                            topProgress.setVisibility(View.GONE);
                            dateTxt.setVisibility(View.GONE);
                        }
                    }

                    loading = false;
                    bottom.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    topProgress.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.v(TAG, "Error=" + e.toString());
                }
                Log.v(TAG, "getchatresponse=" + res);
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
                map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                map.put(Constants.TAG_RECEIVER_ID, userId);
                map.put(Constants.TAG_TYPE, "normal");
                map.put(Constants.TAG_OFFSET, Integer.toString(offset));
                map.put(Constants.TAG_LIMIT, "20");
                map.put(Constants.TAG_SOURCE_ID, "0");
                Log.v(TAG, "getchatParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for Initialize chat
     **/

    private void initializeChat() {
        loading = true;
        if (pulldown) {
            listView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            bottom.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * Function for Offer Accept or Declined
     **/

    private void offerAcceptOrDeclined(final String offerId, final String offerStatus, final HashMap<String, String> tempMap, final String chatId) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_OFFER_STATUS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                String offerStatus="",offerType="",currencySymbol="",offerPrice="",itemImg="";
                Log.v(TAG, "OfferAcceptOrDeclinedResp=" + res);
                if (dialog.isShowing())
                    dialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(res);
                    if (jsonObject.getString(Constants.TAG_STATUS).equals("true")) {
                        final JSONObject jsonObject1 = jsonObject.getJSONObject(Constants.TAG_RESULT);
                        offerStatus=jsonObject1.getString(Constants.TAG_OFFER_STATUS);
                        offerType=jsonObject1.getString(Constants.TAG_OFFER_TYPE);
                        currencySymbol=jsonObject1.getString(Constants.TAG_OFFER_CURRENCY_SYMBOL);
                        offerPrice=jsonObject1.getString(Constants.TAG_OFFER_PRICE);
                        itemImg=jsonObject1.getString(Constants.TAG_ITEMIMAGE);
                        callOfferSocket(System.currentTimeMillis() / 1000L, jsonObject1.getString(Constants.TAG_OFFER_ID),
                                jsonObject1.getString(Constants.TAG_OFFER_TYPE), jsonObject1.getString(Constants.TAG_OFFER_PRICE),
                                jsonObject1.getString(Constants.TAG_OFFER_CURRENCY_SYMBOL), jsonObject1.getString(Constants.TAG_OFFER_STATUS),
                                jsonObject1.getString(Constants.TAG_BUYNOW_STATUS), jsonObject1.getString(Constants.TAG_INSTANT_BUY),
                                jsonObject1.getString(Constants.SOCK_SOLD_ITEM), jsonObject1.getString(Constants.SOCK_SITE_BUYNOW_PAYMENT_MODE),
                                jsonObject1.getString(Constants.TAG_ITEMIMAGE), jsonObject1.getString(Constants.SOCK_BUYNOW_URL), jsonObject1.getString(Constants.TAG_ITEM_ID)
                        );

                        final String finalOfferStatus = offerStatus;
                        final String finalOfferType = offerType;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tempMap.put(Constants.TAG_OFFER_STATUS,finalOfferStatus);
                                HashMap<String, String> map = new HashMap<>();
                                map.putAll(tempMap);
                                map.put(Constants.TAG_OFFER_STATUS,finalOfferStatus);
                                map.put(Constants.TAG_OFFER_TYPE,finalOfferType);
                                chats.add(map);
                                chatAdapter.notifyDataSetChanged();
                                if (chats.size() > 0) {
                                    listView.setSelection(chats.size() - 1);
                                }
                            }
                        });
                    } else if (jsonObject.getString(Constants.TAG_MESSAGE).equals("block status unable to make process")) {
                        Toast.makeText(ChatActivity.this, getString(R.string.conversation_blocked), Toast.LENGTH_SHORT).show();
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
                map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                map.put(Constants.TAG_OFFER_ID, offerId);
                map.put(Constants.TAG_STATUS, offerStatus);
                map.put(Constants.TAG_CHAT_ID, chatId);
                Log.v(TAG, "OfferAcceptOrDeclinedParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    private void getItemDetails(final String itemId, final String itemPrice, final String offerId) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEARCH_ITEM, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonString) {
                try {
                    JSONArray items;

                    JSONObject json = new JSONObject(jsonString);
                    String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        JSONObject result = json
                                .optJSONObject(Constants.TAG_RESULT);
                        if (result != null) {
                            items = result.optJSONArray(Constants.TAG_ITEMS);
                            if (items != null) {
                                JSONObject temp = items.getJSONObject(0);
                                String currencysym = "";
                                if (DefensiveClass.optCurrency(temp, Constants.TAG_CURRENCY_CODE).contains("-")) {
                                    String cur[] = DefensiveClass.optCurrency(temp, Constants.TAG_CURRENCY_CODE).split("-");
                                    currencysym = cur[0];
                                } else {
                                    currencysym = DefensiveClass.optCurrency(temp, Constants.TAG_CURRENCY_CODE);
                                }
                                JSONArray shipdetail = temp.optJSONArray(Constants.TAG_SHIPPING_DETAIL);
                                if (shipdetail == null) {
                                    offerProdMap.put(Constants.TAG_SHIPPING_DETAIL, "");
                                } else {
                                    offerProdMap.put(Constants.TAG_SHIPPING_DETAIL, shipdetail.toString());
                                }
                                JSONArray size = temp.optJSONArray(Constants.TAG_SIZE);
                                if (size == null) {
                                    offerProdMap.put(Constants.TAG_SIZE, "");
                                } else {
                                    offerProdMap.put(Constants.TAG_SIZE, size.toString());
                                }

                                JSONArray photos = temp.optJSONArray(Constants.TAG_PHOTOS);
                                if (photos == null) {
                                    offerProdMap.put(Constants.TAG_PHOTOS, "");
                                } else {
                                    offerProdMap.put(Constants.TAG_PHOTOS, photos.toString());
                                    for (int j = 0; j < photos.length(); j++) {
                                        JSONObject jph = photos.optJSONObject(j);
                                        if (j == 0) {
                                            offerProdMap.put(Constants.TAG_WIDTH, DefensiveClass.optString(jph, Constants.TAG_WIDTH));
                                            offerProdMap.put(Constants.TAG_HEIGHT, DefensiveClass.optString(jph, Constants.TAG_HEIGHT));
                                            offerProdMap.put(Constants.TAG_ITEM_URL_350, DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_350));
                                            offerProdMap.put(Constants.TAG_ITEM_URL_ORG, DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_ORG));
                                        }
                                    }
                                }
                                offerProdMap.put(Constants.TAG_ID, DefensiveClass.optInt(temp, Constants.TAG_ID));
                                offerProdMap.put(Constants.TAG_TITLE, String.valueOf(Html.fromHtml(DefensiveClass.optString(temp, Constants.TAG_TITLE))));
                                offerProdMap.put(Constants.TAG_ITEM_DES, DefensiveClass.optString(temp, Constants.TAG_ITEM_DES));
                                offerProdMap.put(Constants.TAG_ITEM_CONDITION, DefensiveClass.optString(temp, Constants.TAG_ITEM_CONDITION));
                                offerProdMap.put(Constants.TAG_PRICE, itemPrice);
                                offerProdMap.put(Constants.TAG_QUANTITY, DefensiveClass.optInt(temp, Constants.TAG_QUANTITY));
                                offerProdMap.put(Constants.TAG_ITEM_STATUS, DefensiveClass.optString(temp, Constants.TAG_ITEM_STATUS));
                                offerProdMap.put(Constants.TAG_SELLERID, DefensiveClass.optInt(temp, Constants.TAG_SELLERID));
                                offerProdMap.put(Constants.TAG_SELLERNAME, DefensiveClass.optString(temp, Constants.TAG_SELLERNAME));
                                offerProdMap.put(Constants.TAG_SELLERIMG, DefensiveClass.optString(temp, Constants.TAG_SELLERIMG));
                                offerProdMap.put(Constants.TAG_CURRENCY_CODE, DefensiveClass.optCurrency(temp, Constants.TAG_CURRENCY_CODE));
                                offerProdMap.put(Constants.TAG_PROURL, DefensiveClass.optString(temp, Constants.TAG_PROURL));
                                offerProdMap.put(Constants.TAG_LIKECOUNT, DefensiveClass.optInt(temp, Constants.TAG_LIKECOUNT));
                                offerProdMap.put(Constants.TAG_COMMENTCOUNT, DefensiveClass.optInt(temp, Constants.TAG_COMMENTCOUNT));
                                offerProdMap.put(Constants.TAG_VIEWCOUNT, DefensiveClass.optInt(temp, Constants.TAG_VIEWCOUNT));
                                offerProdMap.put(Constants.TAG_LIKED, DefensiveClass.optString(temp, Constants.TAG_LIKED));
                                offerProdMap.put(Constants.TAG_POSTED_TIME, DefensiveClass.optString(temp, Constants.TAG_POSTED_TIME));
                                offerProdMap.put(Constants.TAG_LATITUDE, DefensiveClass.optString(temp, Constants.TAG_LATITUDE));
                                offerProdMap.put(Constants.TAG_LONGITUDE, DefensiveClass.optString(temp, Constants.TAG_LONGITUDE));
                                offerProdMap.put(Constants.TAG_LOCATION, DefensiveClass.optString(temp, Constants.TAG_LOCATION));
                                offerProdMap.put(Constants.TAG_BEST_OFFER, DefensiveClass.optString(temp, Constants.TAG_BEST_OFFER));
                                offerProdMap.put(Constants.TAG_BUYTYPE, DefensiveClass.optString(temp, Constants.TAG_BUYTYPE));
                                offerProdMap.put(Constants.TAG_CATEGORYNAME, DefensiveClass.optString(temp, Constants.TAG_CATEGORYNAME));
                                offerProdMap.put(Constants.TAG_CATEGORYID, DefensiveClass.optString(temp, Constants.TAG_CATEGORYID));
                                offerProdMap.put(Constants.TAG_SUBCATEGORYNAME, DefensiveClass.optString(temp, Constants.TAG_SUBCATEGORYNAME));
                                offerProdMap.put(Constants.TAG_SUBCATEGORYID, DefensiveClass.optString(temp, Constants.TAG_SUBCATEGORYID));
                                offerProdMap.put(Constants.TAG_PAYPALID, DefensiveClass.optString(temp, Constants.TAG_PAYPALID));
                                offerProdMap.put(Constants.TAG_SHIPPING_TIME, DefensiveClass.optString(temp, Constants.TAG_SHIPPING_TIME));
                                offerProdMap.put(Constants.TAG_REPORT, DefensiveClass.optString(temp, Constants.TAG_REPORT));
                                offerProdMap.put(Constants.TAG_PROMOTION_TYPE, DefensiveClass.optString(temp, Constants.TAG_PROMOTION_TYPE));
                                offerProdMap.put(Constants.TAG_EXCHANGE_BUY, DefensiveClass.optString(temp, Constants.TAG_EXCHANGE_BUY));
                                offerProdMap.put(Constants.TAG_MAKE_OFFER, DefensiveClass.optString(temp, Constants.TAG_MAKE_OFFER));
                                offerProdMap.put(Constants.TAG_SELLER_USERNAME, DefensiveClass.optString(temp, Constants.TAG_SELLER_USERNAME));
                                offerProdMap.put(Constants.TAG_FACEBOOK_VERIFICATION, DefensiveClass.optString(temp, Constants.TAG_FACEBOOK_VERIFICATION));
                                offerProdMap.put(Constants.TAG_MOBILE_VERIFICATION, DefensiveClass.optString(temp, Constants.TAG_MOBILE_VERIFICATION));
                                offerProdMap.put(Constants.TAG_EMAIL_VERIFICATION, DefensiveClass.optString(temp, Constants.TAG_EMAIL_VERIFICATION));
                                offerProdMap.put(Constants.TAG_COUNTRYID, DefensiveClass.optString(temp, Constants.TAG_COUNTRYID));
                                offerProdMap.put(Constants.TAG_INSTANT_BUY, DefensiveClass.optString(temp, Constants.TAG_INSTANT_BUY));
                                offerProdMap.put(Constants.TAG_SHIPPING_COST, DefensiveClass.optInt(temp, Constants.TAG_SHIPPING_COST));
                                offerProdMap.put(Constants.TAG_CURRENCY_SYM, currencysym);
                                offerProdMap.put(Constants.TAG_MOBILE_NO, DefensiveClass.optString(temp, Constants.TAG_MOBILE_NO));
                                offerProdMap.put(Constants.TAG_SHOW_SELLER_MOB, DefensiveClass.optString(temp, Constants.TAG_SHOW_SELLER_MOB));
                                offerProdMap.put(Constants.TAG_ITEM_APPROVE, DefensiveClass.optString(temp, Constants.TAG_ITEM_APPROVE));
                                offerProdMap.put(Constants.TAG_SELLER_RATING, DefensiveClass.optInt(temp, Constants.TAG_SELLER_RATING));
                                offerProdMap.put(Constants.TAG_GIVING_AWAY, DefensiveClass.optString(temp, Constants.TAG_GIVING_AWAY));
                            }
                            getAddress(itemId, offerId);
                        }
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
                map.put(Constants.TAG_ITEM_ID, itemId);
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                Log.v(TAG, "getItemsParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);

    }


    private void getAddress(final String itemId, final String offerId) {
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
                        if (dialog.isShowing()) {
                            dialog.dismiss();
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
                                Intent l = new Intent(ChatActivity.this, Addresses.class);
                                l.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                l.putExtra(Constants.FROM, "checkout");
                                l.putExtra("shippingId", Integer.toString(haveDefaultAddress));
                                l.putExtra(Constants.TAG_OFFER_ID, offerId);
                                l.putExtra("itemData", offerProdMap);
                                startActivity(l);
                            } else {
                                Intent i = new Intent(ChatActivity.this, Checkout.class);
                                i.putExtra("itemData", offerProdMap);
                                i.putExtra(Constants.TAG_OFFER_ID, offerId);
                                i.putExtra("shippingData", addressAry.get(haveDefaultAddress));
                                startActivity(i);
                            }*/
                        }
                    } else if (response.equalsIgnoreCase("false") && DefensiveClass.optString(json, Constants.TAG_MESSAGE).equals("Product already sold out")) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(ChatActivity.this, getString(R.string.product_already_sold), Toast.LENGTH_LONG).show();
                    } else if (response.equalsIgnoreCase("false") && DefensiveClass.optString(json, Constants.TAG_MESSAGE).equals("Product in disabled status.")) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(ChatActivity.this, getString(R.string.product_disabled_message), Toast.LENGTH_LONG).show();
                    }
                    else if (response.equalsIgnoreCase("false") && DefensiveClass.optString(json, Constants.TAG_MESSAGE).equals("conversation blocked.")) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(ChatActivity.this, getString(R.string.conversation_blocked), Toast.LENGTH_LONG).show();
                    }
                    else if (response.equalsIgnoreCase("error")) {
                        JoysaleApplication.disabledialog(ChatActivity.this, json.optString(Constants.TAG_MESSAGE), GetSet.getUserId());
                    } else {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        /*Intent i = new Intent(ChatActivity.this, AddAddress.class);
                        i.putExtra(Constants.FROM, "checkout");
                        i.putExtra("to", "add");
                        i.putExtra("itemData", offerProdMap);
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
                map.put(Constants.TAG_ITEM_ID, itemId);
                Log.v(TAG, "getAddressesParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for Sent a Offer Status to Socket
     */

    private void callOfferSocket(long time, String offerId, String offerType, String offerPrice, String offerCurrency, String offerStatus, String buyNowStatus, String instantBuy, String soldItem, String siteBuyNowMode, String itemImage, String buyNowUrl, String itemId) {
        try {
            JSONObject jobj = new JSONObject();
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_CHATTIME, Long.toString(time));
            message.put(Constants.SOCK_USERIMAGE, GetSet.getImageUrl().replace("/150/", "/40/"));
            message.put(Constants.SOCK_USERNAME, GetSet.getUserName());
            message.put(Constants.TAG_MESSAGE, "");
            message.put(Constants.SOCK_VIEW_URL, "");
            message.put(Constants.TAG_TYPE, "offer");
            message.put(Constants.TAG_LAT, "");
            message.put(Constants.TAG_LON, "");
            message.put(Constants.SOCK_MESSAGE_CONTENT, "1");

            message.put(Constants.TAG_OFFER_ID, offerId);
            message.put(Constants.TAG_OFFER_TYPE, offerType);
            message.put(Constants.TAG_OFFER_PRICE, offerPrice);
            message.put(Constants.TAG_OFFER_CURRENCY_SYMBOL, offerCurrency);
            message.put(Constants.TAG_OFFER_STATUS, offerStatus);
            message.put(Constants.TAG_BUYNOW_STATUS, buyNowStatus);
            message.put(Constants.TAG_INSTANT_BUY, instantBuy);
            message.put(Constants.SOCK_SOLD_ITEM, soldItem);
            message.put(Constants.SOCK_SITE_BUYNOW_PAYMENT_MODE, siteBuyNowMode);
            message.put(Constants.TAG_ITEMIMAGE, itemImage);
            message.put(Constants.TAG_ITEM_ID, itemId);
            message.put(Constants.SOCK_BUYNOW_URL, buyNowUrl);
            message.put(Constants.TAG_ITEM_STATUS,"1");
            message.put(Constants.TAG_SELLERNAME, GetSet.getUserName());

            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
            jobj.put(Constants.SOCK_SENDERID, userName);
            jobj.put(Constants.TAG_MESSAGE, message);
            jobj.put("offerId", offerId);
            Log.v(TAG, "sendofferjsoninsocket=" + jobj);
            mSocket.emit("message", jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function for send message through socket and API
     */
    private void sendMessage(String msg) {
        long unixTime = System.currentTimeMillis() / 1000L;
        callSocket(unixTime, "text", msg);

        try {
            HashMap<String, String> hmap = new HashMap<String, String>();
            if (from.equals("detail") && !aboutMessageSent) {
                //	aboutMessageSent = true;
                hmap.put(Constants.TAG_MESSAGE, msg);
                hmap.put(Constants.TAG_SENDER, GetSet.getUserName());
                hmap.put(Constants.DATE, Long.toString(unixTime));
                hmap.put(Constants.TAG_TYPE, "about");
                hmap.put(Constants.TAG_ITEM_ID, data.get(Constants.TAG_ID));
                hmap.put(Constants.TAG_ITEM_TITLE, data.get(Constants.TAG_ITEM_TITLE));
                hmap.put(Constants.TAG_ITEM_IMAGE, data.get(Constants.TAG_ITEM_URL_350));
                hmap.put(Constants.TAG_OFFER_PRICE, data.get(Constants.TAG_OFFER_PRICE));
                hmap.put(Constants.TAG_ITEM_STATUS,"1");
            } else {
                hmap.put(Constants.TAG_MESSAGE, msg);
                hmap.put(Constants.TAG_SENDER, GetSet.getUserName());
                hmap.put(Constants.DATE, Long.toString(unixTime));
                hmap.put(Constants.TAG_TYPE, "message");
            }

            chats.add(hmap);
            Log.v(TAG, "checkdatachat" + chats);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatAdapter.notifyDataSetChanged();
                }
            });

            sendChat("normal", msg, "", 0.0, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function for send the message
     **/

    private void sendChat(final String type, final String message, final String imageName, double lat, double lon) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEND_CHAT, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                Log.v(TAG, "sendchatResp=" + res);
                try {
                    JSONObject jsonObject = new JSONObject(res);
                    if (jsonObject.getString(Constants.TAG_STATUS).equals("true")) {
                        aboutMessageSent = true;
                        send.setOnClickListener(ChatActivity.this);
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    } else {
                        blockChat(true, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                send.setOnClickListener(ChatActivity.this);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                long unixTime = System.currentTimeMillis() / 1000L;
                try {
                    map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                    map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                    map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                    map.put(Constants.TAG_CHAT_ID, chatId);
                    if (from.equals("detail") && !aboutMessageSent) {
                        map.put(Constants.TAG_SOURCE_ID, data.get(Constants.TAG_ID));
                    } else {
                        map.put(Constants.TAG_SOURCE_ID, "0");
                    }
                    map.put(Constants.TAG_TYPE, type);
                    map.put(Constants.TAG_CHAT_TYPE, "normal");
                    map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
                    map.put(Constants.TAG_MESSAGE, message);
                    map.put(Constants.TAG_IMAGE_URL, imageName);
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            chats.remove(chats.size() - 1);
                            chatAdapter.notifyDataSetChanged();
                            try {
                                JoysaleApplication.dialog(ChatActivity.this, getString(R.string.alert), getString(R.string.symbols_not_supported));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    e.printStackTrace();
                }
                Log.v(TAG, "sendchatParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult-Execute");
        if (resultCode == -1 && requestCode == 234) {
            Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
            ImageStorage imageStorage = new ImageStorage(ChatActivity.this, ChatActivity.this);
            final String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
            String imageStatus = imageStorage.saveToSdCard(bitmap, "sent", timestamp + ".jpg", timestamp);
            if (imageStatus.equals("success")) {
                final File file = imageStorage.getImage("sent", timestamp + ".jpg");
                Log.v(TAG, "selectedImageFile=" + file.getAbsolutePath());
                ImageCompression imageCompression = new ImageCompression(ChatActivity.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        long unixTime = System.currentTimeMillis() / 1000L;
                        HashMap<String, String> hmap = new HashMap<String, String>();
                        hmap.put(Constants.TAG_SENDER, GetSet.getUserName());
                        hmap.put(Constants.DATE, Long.toString(unixTime));
                        hmap.put(Constants.TAG_TYPE, "image");
                        hmap.put("localpath", imagePath);
                        chats.add(hmap);
                        chatAdapter.notifyDataSetChanged();
                        new UploadImage(ChatActivity.this, "Chat", userImg, unixTime).execute(imagePath);
                    }
                };
                imageCompression.execute(file.getAbsolutePath());
            } else {
                Toast.makeText(this, getString(R.string.profile_problem), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_FETCH_ACTION) {
            Log.v(TAG, "onActivityResult-Location-Execute");
            HashMap<String, String> hmap = new HashMap<String, String>();
            if (data != null) {
                hmap.put(Constants.TAG_LATITUDE, data.getStringExtra("current_latitude"));
                hmap.put(Constants.TAG_LONGITUDE, data.getStringExtra("current_longitude"));
                hmap.put(Constants.TAG_SENDER, GetSet.getUserName());
                hmap.put(Constants.DATE, String.valueOf(System.currentTimeMillis() / 1000L));
                hmap.put(Constants.TAG_TYPE, "share_location");
                Log.v(TAG, "onActivityResult-LocationMap=" + hmap);
                String jsonStr = data.getStringExtra("jsonObject");
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    mSocket.emit("message", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                chats.add(hmap);
                chatAdapter.notifyDataSetChanged();
                listView.setSelection(chats.size() - 1);
            }
        }
    }

    /**
     * Function for Sent a message to Socket
     */

    private void callSocket(long time, String type, String data) {
        try {
            JSONObject jobj = new JSONObject();
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_CHATTIME, Long.toString(time));
            message.put(Constants.SOCK_USERIMAGE, GetSet.getImageUrl().replace("/150/", "/40/"));
            message.put(Constants.SOCK_USERNAME, GetSet.getUserName());
            if (type.equals("text")) {
                message.put(Constants.TAG_MESSAGE, data);
                message.put(Constants.SOCK_VIEW_URL, "");
                message.put(Constants.TAG_TYPE, "normal");
                message.put(Constants.TAG_LAT, "");
                message.put(Constants.TAG_LON, "");
                message.put(Constants.SOCK_MESSAGE_CONTENT, "1");

            } else if (type.equals("image")) {
                message.put(Constants.TAG_MESSAGE, "");
                message.put(Constants.SOCK_VIEW_URL, data);
                message.put(Constants.TAG_TYPE, "image");
                message.put(Constants.TAG_LAT, "");
                message.put(Constants.TAG_LON, "");
                message.put(Constants.SOCK_MESSAGE_CONTENT, "2");
            }
            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
            jobj.put(Constants.SOCK_SENDERID, userName);
            jobj.put("offerId", "0");
            jobj.put(Constants.TAG_MESSAGE, message);
            Log.v(TAG, "sendjsoninsocket=" + jobj);
            mSocket.emit("message", jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function for Open a particular Screen based on a selected choice
     */

    public void openAction(String from) {
        Log.v(TAG, "openAction-from=" + from);
        if (from.equals(getString(R.string.make_an_offer))) {

        } else if (from.equals(getString(R.string.safety_tips))) {
            dialog.setMessage(getString(R.string.pleasewait));
            dialog.show();
            showSafetyTips();
        } else if (from.equals(getString(R.string.block_user))) {
            final Dialog alertDialog = new Dialog(ChatActivity.this, R.style.AlertDialog);
            Display display = getWindowManager().getDefaultDisplay();
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.setContentView(R.layout.default_dialog);
            alertDialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
            alertDialog.setCancelable(true);
            alertDialog.setCanceledOnTouchOutside(false);

            TextView alertMsg = (TextView) alertDialog.findViewById(R.id.alert_msg);
            TextView alertOk = (TextView) alertDialog.findViewById(R.id.alert_button);
            TextView alertCancel = (TextView) alertDialog.findViewById(R.id.cancel_button);

            alertMsg.setText(getString(R.string.reallyBlock));
            alertOk.setText(getString(R.string.block));
            alertCancel.setText(getString(R.string.cancel));

            alertCancel.setVisibility(View.VISIBLE);

            alertOk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.setMessage(getString(R.string.pleasewait));
                    dialog.show();
                    blockorUnblockUser(userId, "block");
                }
            });

            alertCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        } else if (from.equals(getString(R.string.unblock_user))) {
            dialog.setMessage(getString(R.string.pleasewait));
            dialog.show();
            blockorUnblockUser(userId, "unblock");
        }
    }

    /**
     * Function for Block or Unblock User
     */

    private void blockorUnblockUser(final String blockUserId, final String value) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_BLOCK_UNBLOCK_USR, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    Log.v(TAG, "blockuserResponse=" + res);
                    if (dialog.isShowing())
                        dialog.dismiss();
                    JSONObject jsonObject = new JSONObject(res);
                    if (DefensiveClass.optString(jsonObject, Constants.TAG_STATUS).equals("true")) {
                        if (DefensiveClass.optString(jsonObject, Constants.TAG_MESSAGE).equals("Blocked Successfully")) {
                            blockChat(false, true);
                            finish();
                        } else {
                            blockChat(false, false);
                        }
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
                map.put(Constants.TAG_ACTION_ID, blockUserId);
                map.put(Constants.TAG_ACTION_VALUE, value);
                Log.v(TAG, "blockuserParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for Show Safety Tips
     */

    private void showSafetyTips() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SAFETY_TIPS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    Log.v(TAG, "safetyTipsResponse=" + res);
                    JSONObject jsonObject = new JSONObject(res);
                    if (DefensiveClass.optString(jsonObject, Constants.TAG_STATUS).equals("true")) {
                        Intent i = new Intent(ChatActivity.this, AboutUs.class);
                        i.putExtra(Constants.TAG_TITLE_M, getString(R.string.safety_tips));
                        i.putExtra(Constants.CONTENT, DefensiveClass.optString(jsonObject, Constants.TAG_MESSAGE));
                        startActivity(i);
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
                Log.v(TAG, "safetyTipsParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for block a Chat
     */

    private void blockChat(boolean isBlocked, boolean isUsrBlockedByMe) {
        if (isUsrBlockedByMe) {
            isuserBlocked=true;
            blockUserLay.setVisibility(View.VISIBLE);
            values.set(1, getString(R.string.unblock_user));
            sendMsgLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else if (isBlocked) {
            isuserBlocked=true;
            blockUserLay.setVisibility(View.VISIBLE);
            blockMsg.setText(getString(R.string.block_user_msg_receiver));
            values.remove(1);
            sendMsgLay.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            isuserBlocked=false;
            blockUserLay.setVisibility(View.GONE);
            values.set(1, getString(R.string.block_user));
            sendMsgLay.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        Log.v(TAG, "beforeTextChanged");
    }

    /**
     * Method for send typing status to other end user
     **/

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.v(TAG, "on typing");
        if (runnable != null)
            handler.removeCallbacks(runnable);
        if (!meTyping) {
            meTyping = true;
            JSONObject jobj = new JSONObject();
            try {
                jobj.put(Constants.SOCK_SENDERID, userName);
                jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
                jobj.put(Constants.TAG_MESSAGE, "type");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("messageTyping", jobj);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.v(TAG, "after");
        runnable = new Runnable() {
            public void run() {
                Log.v(TAG, "stop typing");
                meTyping = false;
                JSONObject jobj = new JSONObject();
                try {
                    jobj.put(Constants.SOCK_SENDERID, userName);
                    jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
                    jobj.put(Constants.TAG_MESSAGE, "untype");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("messageTyping", jobj);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    /**
     * adapter for list the conversation in listview
     **/

    public class ChatAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> Items;
        ViewHolder holder = null;
        String lastDate = "";
        private Context mContext;

        public ChatAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            Items = data;
        }


        @Override
        public int getCount() {
            return Items.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.chat_item, parent, false);//layout

                holder = new ViewHolder();
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.leftMsg = (TextView) convertView.findViewById(R.id.leftMsg);
                holder.rightMsg = (TextView) convertView.findViewById(R.id.rightMsg);
                holder.leftTime = (TextView) convertView.findViewById(R.id.leftTime);
                holder.rightTime = (TextView) convertView.findViewById(R.id.rightTime);
                holder.left_makeoffer_name = (TextView) convertView.findViewById(R.id.left_makeoffer_name);
                holder.right_makeoffer_name = (TextView) convertView.findViewById(R.id.right_makeoffer_name);
                holder.left_makeoffer_duration = (TextView) convertView.findViewById(R.id.left_makeoffer_duration);
                holder.right_makeoffer_duration = (TextView) convertView.findViewById(R.id.right_makeoffer_duration);
                holder.left_makeoffer_price = (TextView) convertView.findViewById(R.id.left_makeoffer_price);
                holder.right_makeoffer_price = (TextView) convertView.findViewById(R.id.right_makeoffer_price);
                holder.left_makeoffer_msg = (TextView) convertView.findViewById(R.id.left_makeoffer_msg);
                holder.right_makeoffer_msg = (TextView) convertView.findViewById(R.id.right_makeoffer_msg);
                holder.dateLay = (RelativeLayout) convertView.findViewById(R.id.dateLay);
                holder.leftLay = (RelativeLayout) convertView.findViewById(R.id.leftLay);
                holder.rightLay = (RelativeLayout) convertView.findViewById(R.id.rightLay);
                holder.itemLay = (RelativeLayout) convertView.findViewById(R.id.itemLay);
                holder.itemName = (TextView) convertView.findViewById(R.id.itemName);
                holder.aboutDate = (TextView) convertView.findViewById(R.id.aboutDate);
                holder.aboutMsg = (TextView) convertView.findViewById(R.id.aboutMsg);
                holder.itemImage = (ImageView) convertView.findViewById(R.id.itemImage);
                holder.price = (TextView) convertView.findViewById(R.id.price);
                holder.rightImage = (ImageView) convertView.findViewById(R.id.right_image);
                holder.leftImage = (ImageView) convertView.findViewById(R.id.left_image);
                holder.leftOfferImg = (ImageView) convertView.findViewById(R.id.leftOfferImg);
                holder.rightOfferImg = (ImageView) convertView.findViewById(R.id.rightOfferImg);
                holder.offerResultImg = (ImageView) convertView.findViewById(R.id.offerResultImg);

                holder.left_image_lay = (RelativeLayout) convertView.findViewById(R.id.left_image_lay);
                holder.right_image_lay = (RelativeLayout) convertView.findViewById(R.id.right_image_lay);
                holder.left_msg_layout = (RelativeLayout) convertView.findViewById(R.id.left_msg_layout);
                holder.right_msg_layout = (RelativeLayout) convertView.findViewById(R.id.right_msg_layout);
                holder.left_makeoffer_lay = (RelativeLayout) convertView.findViewById(R.id.left_makeoffer_lay);
                holder.right_makeoffer_lay = (RelativeLayout) convertView.findViewById(R.id.right_makeoffer_lay);
                holder.offer_result_lay = (RelativeLayout) convertView.findViewById(R.id.offer_result_lay);
                holder.leftImgTime = (TextView) convertView.findViewById(R.id.leftImgTime);
                holder.rightImgTime = (TextView) convertView.findViewById(R.id.rightImgTime);
                holder.buyOfferPrd = (TextView) convertView.findViewById(R.id.buyOfferPrd);
                holder.offerAccept = (TextView) convertView.findViewById(R.id.offerAccept);
                holder.offerDeclined = (TextView) convertView.findViewById(R.id.offerDeclined);
                holder.offerResultPrice = (TextView) convertView.findViewById(R.id.offerResultPrice);
                holder.offerResultTime = (TextView) convertView.findViewById(R.id.offerResultTime);
                holder.offerResultMsg = (TextView) convertView.findViewById(R.id.offerResultMsg);
                holder.offerstatusLay = (LinearLayout) convertView.findViewById(R.id.offerstatusLay);
                holder.leftDelete = (ImageView) convertView.findViewById(R.id.leftDelete);
                holder.rightDelete = (ImageView) convertView.findViewById(R.id.rightDelete);
                holder.offerResultIcon = (ImageView) convertView.findViewById(R.id.offerResultIcon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final HashMap<String, String> tempMap = Items.get(position);
            holder.leftLay.setVisibility(View.GONE);
            holder.rightLay.setVisibility(View.GONE);
            holder.dateLay.setVisibility(View.GONE);
            holder.offer_result_lay.setVisibility(View.GONE);
            holder.itemLay.setVisibility(View.GONE);

            try {
                long date = Long.parseLong(tempMap.get(Constants.DATE)) * 1000;
                String chatDate = JoysaleApplication.getDate(date);
                switch (tempMap.get(Constants.TAG_TYPE)) {
                    case "message":
                    case "normal":
                        if (tempMap.get(Constants.TAG_SENDER).equals(GetSet.getUserName())) {
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.VISIBLE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);
                            holder.rightDelete.setVisibility(View.GONE);

                            holder.rightMsg.setText(tempMap.get(Constants.TAG_MESSAGE));
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));

                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.VISIBLE);
                            holder.left_image_lay.setVisibility(View.GONE);
                            holder.left_makeoffer_lay.setVisibility(View.GONE);
                            holder.leftDelete.setVisibility(View.GONE);

                            holder.leftMsg.setText(tempMap.get(Constants.TAG_MESSAGE));
                            holder.leftTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));
                        }
                        break;

                    case "image":
                        if (tempMap.get(Constants.TAG_SENDER).equals(GetSet.getUserName())) {//Right Side
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_image_lay.setVisibility(View.VISIBLE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);

                            String imageName = "";
                            File file = null;
                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            ImageStorage imageStorage = new ImageStorage(ChatActivity.this, mContext);
                            if (tempMap.containsKey("localpath")) {
                                imageName = AppUtils.getImageName(tempMap.get("localpath"));
                                file = new File(String.valueOf(imageStorage.getImage("sent", imageName)));
                            } else {
                                imageName = AppUtils.getImageName(tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                file = new File(String.valueOf(imageStorage.getImage("", imageName)));
                            }
                            Picasso.with(mContext).load(file).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.rightImage);
                        } else {
                            //left
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.left_image_lay.setVisibility(View.VISIBLE);
                            holder.left_makeoffer_lay.setVisibility(View.GONE);

                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            if (tempMap.get(Constants.TAG_UPLOADED_IMG_URL) != null) {
                                String imgSplit = AppUtils.getImageName(tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                ImageStorage imageStorage = new ImageStorage(ChatActivity.this, mContext);
                                if (imageStorage.checkifImageExists("", imgSplit)) {
                                    File file = imageStorage.getImage("", imgSplit);
                                    if (file != null) {
                                        Picasso.with(mContext).load(file).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.leftImage);
                                    }
                                    holder.leftImgTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));
                                } else {
                                    imageType = "remote";
                                    //Instant Image Received
                                    new DownloadAndStoreImg("image", AppUtils.getValidUrl(tempMap.get(Constants.TAG_UPLOADED_IMG_URL)),
                                            tempMap.get(Constants.DATE)).execute();
                                    Picasso.with(mContext).load(AppUtils.getValidUrl(tempMap.get(Constants.TAG_UPLOADED_IMG_URL))).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.leftImage);
                                }
                            }
                        }
                        holder.rightImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                } else {
                                    ImageStorage imageStorage = new ImageStorage(ChatActivity.this, mContext);
                                    if (tempMap.containsKey("localpath")) {
                                        String imgSplit = AppUtils.getImageName(tempMap.get("localpath"));
                                        if (imageStorage.checkifImageExists("sent", imgSplit)) {
                                            Log.v(TAG, "Already Downloaded");
                                            File file = imageStorage.getImage("sent", imgSplit);
                                            Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                            intent.putExtra(Constants.IMAGETYPE, "local");
                                            intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                            Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                            ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                        }
                                    } else {
                                        Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "remote");
                                        intent.putExtra(Constants.KEY_IMAGE, tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                        Pair<View, String> bodyPair = Pair.create(view, tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                        ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                    }
                                }
                            }
                        });

                        holder.leftImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                } else {
                                    ImageStorage imageStorage = new ImageStorage(ChatActivity.this, mContext);
                                    if (imageType.equals("local")) {
                                        if (tempMap.get(Constants.TAG_UPLOADED_IMG_URL) != null) {
                                            String imgSplit = AppUtils.getImageName(tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                            if (imageStorage.checkifImageExists("", imgSplit)) {
                                                Log.v(TAG, "Already Downloaded");
                                                File file = imageStorage.getImage("", imgSplit);
                                                Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                                intent.putExtra(Constants.IMAGETYPE, "local");
                                                intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                                Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                                ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                            }
                                        }
                                    } else {
                                        imageType = "local";
                                        //Instant Received Image
                                        Intent intent = new Intent(ChatActivity.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "remote");
                                        intent.putExtra(Constants.KEY_IMAGE, tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                        Pair<View, String> bodyPair = Pair.create(view, tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ChatActivity.this, bodyPair);
                                        ActivityCompat.startActivity(ChatActivity.this, intent, options.toBundle());
                                    }
                                }
                            }
                        });
                        break;

                    case "share_location":
                        int imgSize = JoysaleApplication.dpToPx(ChatActivity.this, 150);
                        String latitude = tempMap.get(Constants.TAG_USR_LATITUDE).trim();
                        String longitude = tempMap.get(Constants.TAG_USR_LONGITUDE).trim();
                        int width = JoysaleApplication.dpToPx(ChatActivity.this, 280);
                        int height = JoysaleApplication.dpToPx(ChatActivity.this, 190);
                        String mapImgUrl = "http://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=15&size=" + width + "x" + height + "&sensor=false&markers=" + latitude + "," + longitude + "|color:red" + "&key=" + Constants.GOOGLE_API_KEY;

                        if (tempMap.get(Constants.TAG_SENDER).equals(GetSet.getUserName())) {//Right Side
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_image_lay.setVisibility(View.VISIBLE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);

                            Picasso.with(ChatActivity.this).load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(ChatActivity.this).into(holder.rightImage);
                            holder.rightImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AppUtils.callMap(tempMap.get(Constants.TAG_USR_LATITUDE), tempMap.get(Constants.TAG_USR_LONGITUDE), ChatActivity.this);
                                }
                            });
                            holder.rightImgTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));
                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.left_image_lay.setVisibility(View.VISIBLE);
                            holder.left_makeoffer_lay.setVisibility(View.GONE);

                            holder.leftImage.setTag("location");
                            Picasso.with(mContext).load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.leftImage);
                            holder.leftImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AppUtils.callMap(tempMap.get(Constants.TAG_USR_LATITUDE), tempMap.get(Constants.TAG_USR_LONGITUDE), ChatActivity.this);
                                }
                            });
                            holder.leftImgTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));
                        }
                        break;

                    case "about":
                        if(tempMap.get(Constants.TAG_ITEM_STATUS).equals("1")){
                            holder.itemLay.setVisibility(View.VISIBLE);
                            holder.aboutMsg.setVisibility(View.VISIBLE);

                            Picasso.with(ChatActivity.this).load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.itemImage);
                            String name = getString(R.string.about) + " " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + tempMap.get(Constants.TAG_ITEM_TITLE) + "</font>";
                            holder.itemName.setText(Html.fromHtml(name));
                            holder.aboutDate.setText(chatDate);
                            holder.aboutMsg.setText(JoysaleApplication.stripHtml(tempMap.get(Constants.TAG_MESSAGE)));
                            holder.itemName.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(dialog != null && !dialog.isShowing()) {
                                        dialog.show();
                                    }
                                    getItemData(tempMap.get(Constants.TAG_ITEM_ID));
                                }
                            });
                            holder.itemImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(dialog != null && !dialog.isShowing()) {
                                        dialog.show();
                                    }
                                    getItemData(tempMap.get(Constants.TAG_ITEM_ID));
                                }
                            });
                        } else{
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.VISIBLE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.right_makeoffer_lay.setVisibility(View.GONE);
                            holder.rightDelete.setVisibility(View.VISIBLE);

                            holder.rightMsg.setText(getString(R.string.product_removed_msg));
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));
                        }
                        break;

                    case "offer":
                        //Sender
                        if (tempMap.get(Constants.TAG_SENDER).equals(GetSet.getUserName())) {//Right Side
                            if(tempMap.get(Constants.TAG_ITEM_STATUS).equals("1")){
                                if (tempMap.get(Constants.TAG_OFFER_TYPE).equals("sendreceive")) {
                                    holder.rightLay.setVisibility(View.VISIBLE);
                                    holder.right_msg_layout.setVisibility(View.GONE);
                                    holder.right_image_lay.setVisibility(View.GONE);
                                    holder.right_makeoffer_lay.setVisibility(View.VISIBLE);

                                    Picasso.with(ChatActivity.this).load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.rightOfferImg);
                                    String name2 = getString(R.string.sent_offer_request_on) + " " + tempMap.get(Constants.TAG_ITEM_TITLE);
                                    holder.right_makeoffer_name.setText(Html.fromHtml(name2));
                                    holder.right_makeoffer_duration.setText(chatDate);
                                    holder.right_makeoffer_price.setText(tempMap.get(Constants.TAG_OFFER_PRICE) + " " + tempMap.get(Constants.TAG_OFFER_CURRENCY_SYMBOL));
                                    holder.right_makeoffer_msg.setText(JoysaleApplication.stripHtml(tempMap.get(Constants.TAG_MESSAGE)));

                                    holder.right_makeoffer_name.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(tempMap.get(Constants.TAG_ITEM_ID));
                                        }
                                    });

                                    holder.rightOfferImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(tempMap.get(Constants.TAG_ITEM_ID));
                                        }
                                    });
                                } else if (tempMap.get(Constants.TAG_OFFER_TYPE).equals("accept") && (tempMap.get(Constants.TAG_OFFER_STATUS).equals("1"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_accept_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.green_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_accept_icon);
                                    holder.offerResultPrice.setText(tempMap.get(Constants.TAG_OFFER_PRICE) + " " + tempMap.get(Constants.TAG_OFFER_CURRENCY_SYMBOL));
                                    holder.offerResultTime.setText(chatDate);
                                    Picasso.with(ChatActivity.this).load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(tempMap.get(Constants.TAG_ITEM_ID));
                                        }
                                    });
                                    if (tempMap.get(Constants.TAG_BUYNOW_STATUS).equals("0") && tempMap.get(Constants.TAG_INSTANT_BUY).equals("1")) {
                                        holder.buyOfferPrd.setVisibility(View.VISIBLE);
                                        holder.buyOfferPrd.setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.setMessage(getString(R.string.pleasewait));
                                                dialog.show();
                                                if(!isuserBlocked) {
//                                                    Log.e(TAG, "onClick: "+tempMap.get(Constants.TAG_OFFER_ID) );
                                                    getItemDetails(tempMap.get(Constants.TAG_ITEM_ID), tempMap.get(Constants.TAG_OFFER_PRICE), tempMap.get(Constants.TAG_OFFER_ID));
                                                }
                                                else{
                                                    dialog.dismiss();
                                                    Toast.makeText(mContext,getString(R.string.conversation_blocked), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        holder.buyOfferPrd.setVisibility(View.GONE);
                                    }
                                } else if (tempMap.get(Constants.TAG_OFFER_TYPE).equals("decline") && (tempMap.get(Constants.TAG_OFFER_STATUS).equals("2"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);
                                    holder.buyOfferPrd.setVisibility(View.GONE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_decline_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.red_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_decline_icon);
                                    holder.offerResultPrice.setText(tempMap.get(Constants.TAG_OFFER_PRICE) + " " + tempMap.get(Constants.TAG_OFFER_CURRENCY_SYMBOL));
                                    holder.offerResultTime.setText(chatDate);
                                    Picasso.with(ChatActivity.this).load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(tempMap.get(Constants.TAG_ITEM_ID));
                                        }
                                    });
                                }
                            }else {
                                holder.rightLay.setVisibility(View.VISIBLE);
                                holder.right_msg_layout.setVisibility(View.VISIBLE);
                                holder.right_image_lay.setVisibility(View.GONE);
                                holder.right_makeoffer_lay.setVisibility(View.GONE);
                                holder.rightDelete.setVisibility(View.VISIBLE);

                                holder.rightMsg.setText(getString(R.string.product_removed_msg));
                                holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));
                            }
                        } else {//Receiver
                            if(tempMap.get(Constants.TAG_ITEM_STATUS).equals("1")){
                                if (tempMap.get(Constants.TAG_OFFER_TYPE).equals("sendreceive")) {
                                    holder.leftLay.setVisibility(View.VISIBLE);
                                    holder.left_msg_layout.setVisibility(View.GONE);
                                    holder.left_image_lay.setVisibility(View.GONE);
                                    holder.left_makeoffer_lay.setVisibility(View.VISIBLE);

                                    Picasso.with(ChatActivity.this).load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.leftOfferImg);
                                    String name2 = getString(R.string.receive_offer_request_on) + " " + tempMap.get(Constants.TAG_ITEM_TITLE);
                                    holder.left_makeoffer_name.setText(Html.fromHtml(name2));
                                    holder.left_makeoffer_duration.setText(chatDate);
                                    holder.left_makeoffer_price.setText(tempMap.get(Constants.TAG_OFFER_PRICE) + " " + tempMap.get(Constants.TAG_OFFER_CURRENCY_SYMBOL));
                                    holder.left_makeoffer_msg.setText(JoysaleApplication.stripHtml(tempMap.get(Constants.TAG_MESSAGE)));
                                    if (tempMap.get(Constants.TAG_OFFER_STATUS).equals("0"))
                                        holder.offerstatusLay.setVisibility(View.VISIBLE);
                                    else {
                                        holder.offerstatusLay.setVisibility(View.GONE);
                                    }
                                    holder.offerAccept.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.setMessage(getString(R.string.pleasewait));
                                            dialog.show();
                                            offerAcceptOrDeclined(tempMap.get(Constants.TAG_OFFER_ID), "accept", tempMap, tempMap.get(Constants.TAG_CHAT_ID));
                                        }
                                    });
                                    holder.offerDeclined.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.setMessage(getString(R.string.pleasewait));
                                            dialog.show();
                                            offerAcceptOrDeclined(tempMap.get(Constants.TAG_OFFER_ID), "decline", tempMap, tempMap.get(Constants.TAG_CHAT_ID));
                                        }
                                    });

                                    holder.leftOfferImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(tempMap.get(Constants.TAG_ITEM_ID));
                                        }
                                    });
                                } else if (tempMap.get(Constants.TAG_OFFER_TYPE).equals("accept") && (tempMap.get(Constants.TAG_OFFER_STATUS).equals("1"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);
                                    holder.buyOfferPrd.setVisibility(View.GONE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_accept_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.green_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_accept_icon);
                                    holder.offerResultPrice.setText(tempMap.get(Constants.TAG_OFFER_PRICE) + " " + tempMap.get(Constants.TAG_OFFER_CURRENCY_SYMBOL));
                                    holder.offerResultTime.setText(chatDate);
                                    Picasso.with(ChatActivity.this).load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(tempMap.get(Constants.TAG_ITEM_ID));
                                        }
                                    });
                                } else if (tempMap.get(Constants.TAG_OFFER_TYPE).equals("decline") && (tempMap.get(Constants.TAG_OFFER_STATUS).equals("2"))) {
                                    holder.offer_result_lay.setVisibility(View.VISIBLE);
                                    holder.buyOfferPrd.setVisibility(View.GONE);

                                    holder.offerResultMsg.setText(getString(R.string.offer_decline_msg));
                                    holder.offerResultMsg.setTextColor(ContextCompat.getColor(mContext, R.color.red_color));
                                    holder.offerResultIcon.setImageResource(R.drawable.offer_decline_icon);
                                    holder.offerResultPrice.setText(tempMap.get(Constants.TAG_OFFER_PRICE) + " " + tempMap.get(Constants.TAG_OFFER_CURRENCY_SYMBOL));
                                    holder.offerResultTime.setText(chatDate);
                                    Picasso.with(ChatActivity.this).load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.offerResultImg);
                                    holder.offerResultImg.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(dialog != null && !dialog.isShowing()) {
                                                dialog.show();
                                            }
                                            getItemData(tempMap.get(Constants.TAG_ITEM_ID));
                                        }
                                    });
                                }
                            }else{
                                holder.leftLay.setVisibility(View.VISIBLE);
                                holder.left_msg_layout.setVisibility(View.VISIBLE);
                                holder.left_image_lay.setVisibility(View.GONE);
                                holder.left_makeoffer_lay.setVisibility(View.GONE);
                                holder.leftDelete.setVisibility(View.VISIBLE);

                                holder.leftMsg.setText(getString(R.string.product_removed_msg));
                                holder.leftTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));
                            }
                        }
                        break;
                }

                if (position == 0) {
                    holder.dateLay.setVisibility(View.VISIBLE);
                    holder.date.setText(chatDate);
                } else {
                    String ldate = JoysaleApplication.getDate(Long.parseLong(Items.get(position - 1).get(Constants.DATE)) * 1000);
                    if (ldate.equals(chatDate)) {
                        holder.dateLay.setVisibility(View.GONE);
                    } else {
                        holder.dateLay.setVisibility(View.VISIBLE);
                        holder.date.setText(chatDate);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception in adapter=>" + e);
                e.printStackTrace();
            }
            return convertView;
        }


        class ViewHolder {
            LinearLayout main, offerstatusLay;
            TextView message, itemName, left_makeoffer_name, right_makeoffer_name, aboutDate, price, aboutMsg, date, leftMsg, rightMsg, leftTime,
                    rightTime, left_makeoffer_duration, left_makeoffer_msg, right_makeoffer_msg, right_makeoffer_duration, left_makeoffer_price,
                    right_makeoffer_price, leftImgTime, rightImgTime, buyOfferPrd, offerAccept, offerDeclined, offerResultPrice, offerResultTime,
                    offerResultMsg;
            ImageView leftImage, rightImage, itemImage, leftOfferImg, rightOfferImg, offerResultImg, leftDelete, rightDelete, offerResultIcon;
            RelativeLayout dateLay, itemLay, leftLay, rightLay, left_makeoffer_lay, right_makeoffer_lay, left_image_lay, left_msg_layout, right_msg_layout, right_image_lay, offer_result_lay;
        }
    }

    private void getItemData(String itemId) {
            final ArrayList<HashMap<String, String>> HomeItems = new ArrayList<HashMap<String, String>>();
            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEARCH_ITEM, new Response.Listener<String>() {
                @Override
                public void onResponse(String json) {
                    ItemsParsing parse = new ItemsParsing(ChatActivity.this, GetSet.getUserId());
                    HomeItems.addAll(parse.parsing(json));

                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (HomeItems.size() == 0) {
                        Toast.makeText(ChatActivity.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent i = new Intent(ChatActivity.this, DetailActivity.class);
                        i.putExtra(Constants.DATA, HomeItems.get(0));
                        i.putExtra(Constants.POSITION, 0);
                        i.putExtra(Constants.FROM, "chat");
                        startActivity(i);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
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

    /**
     * Adapter for Chat Template
     */

    private class RecyclerAdapter extends RecyclerView.Adapter<Viewholder> {
        ArrayList<HashMap<String, String>> items;

        public RecyclerAdapter(ArrayList<HashMap<String, String>> templatMsgAry) {
            this.items = templatMsgAry;
        }

        @Override
        public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_template_text, parent, false);
            return new Viewholder(view);
        }

        @Override
        public void onBindViewHolder(Viewholder holder, final int position) {
            holder.templateMsg.setText(items.get(position).get(Constants.NAME));
            holder.templateMsg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage(items.get(position).get(Constants.NAME));
                    chatAdapter.notifyDataSetChanged();
                    if (chats.size() > 0) {
                        listView.setSelection(chats.size() - 1);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private class Viewholder extends RecyclerView.ViewHolder {
        TextView templateMsg;

        public Viewholder(View itemView) {
            super(itemView);
            templateMsg = (TextView) itemView.findViewById(R.id.templateMsg);
        }

    }

    /**
     * class for upload Image to Server
     */

    class UploadImage extends AsyncTask<String, String, String> {
        JSONObject jsonobject = null;
        String jsonResponse = "", status, imageName = "", mFrom;
        ProgressDialog pd;
        Context mContext;
        ImageView mUserImage;
        long mUnixTimeStamp;

        public UploadImage(Context context, String from, ImageView userImage, long unixTimeStamp) {
            mContext = context;
            mFrom = from;
            mUserImage = userImage;
            mUnixTimeStamp = unixTimeStamp;
        }

        @Override
        protected String doInBackground(String... imgpath) {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            StringBuilder builder = new StringBuilder();
            String lineEnd = "\r\n", twoHyphens = "--", boundary = "*****", urlString = Constants.API_UPLOAD_IMAGE;
            int bytesRead, bytesAvailable, bufferSize, maxBufferSize = 1 * 1024 * 1024;
            byte[] buffer;

            try {
                existingFileName = imgpath[0];
                Log.v(TAG, " existingFileName=" + existingFileName);
                FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
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
                dos.writeBytes("chat");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + existingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                Log.e(TAG, "MediaPlayer-Headers are written");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                Log.v(TAG, "buffer" + buffer);

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    Log.v(TAG, "bytesRead" + bytesRead);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                Log.v(TAG, "in=" + in);
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);

                Log.e(TAG, "MediaPlayer-File is written");
                fileInputStream.close();
                jsonResponse = builder.toString();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e(TAG, "MediaPlayer-error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e(TAG, "MediaPlayer-error: " + ioe.getMessage(), ioe);
            }
            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;
                while ((str = inStream.readLine()) != null) {
                    Log.e(TAG, "MediaPlayer-Server Response" + str);
                }
                inStream.close();
            } catch (IOException ioex) {
                Log.e(TAG, "MediaPlayer-error: " + ioex.getMessage(), ioex);
            }
            Log.v(TAG, "uploadresponsefromserver" + jsonResponse);
            try {
                jsonobject = new JSONObject(jsonResponse);
                status = jsonobject.getString(Constants.TAG_STATUS);
                if (status.equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    imageName = image.getString("Name");

                    callSocket(mUnixTimeStamp, "image", image.getString("View_url"));
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
            return jsonResponse;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(mContext,R.style.AppCompatAlertDialogStyle);
            pd.setMessage(mContext.getString(R.string.loading));
            pd.setCanceledOnTouchOutside(false);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Drawable drawable = new ProgressBar(ChatActivity.this).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(ChatActivity.this, R.color.progressColor),
                        PorterDuff.Mode.SRC_IN);
                pd.setIndeterminateDrawable(drawable);
            }
            pd.show();
        }

        @Override
        protected void onPostExecute(String res) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            try {
                Log.v(TAG, "imagedownloadedResponseinUI" + res);
                JSONObject jsonobject = new JSONObject(res);
                if (jsonobject.getString(Constants.TAG_STATUS).equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    imageName = DefensiveClass.optString(image, "Name");
                }
                sendChat("image", "", imageName, 0.0, 0.0);

                runOnUiThread(new Runnable() {
                    public void run() {
                        chatAdapter.notifyDataSetChanged();
                        if (chats.size() > 0) {
                            listView.setSelection(chats.size() - 1);
                        }
                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Function for Download Image from Server and Store in External Storage
     **/

    class DownloadAndStoreImg extends AsyncTask<Void, Void, Void> {
        String mType, mImageUrl, mTimeStamp;

        public DownloadAndStoreImg(String type, String imageUrl, String timeStamp) {
            mType = type;
            mImageUrl = imageUrl;
            mTimeStamp = timeStamp;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mType.equals("image")) {
                Bitmap image = AppUtils.downloadImage(mImageUrl);
                if (image != null) {
                    final ImageStorage imageStorage = new ImageStorage(ChatActivity.this, ChatActivity.this);
                    String imageName = AppUtils.getImageName(mImageUrl);
                    //Store Images outside a Sent Folder
                    if (!imageStorage.checkifImageExists("", imageName)) {
                        imageStorage.saveToSdCard(image, "", imageName, mTimeStamp);
                    }
                }
            }
            return null;
        }
    }

    /**
     * Function for Onclick Events
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                fullName = "";
                disconnectSocket();
                if (from.equals(Constants.TAG_MESSAGE)) {
                    initializeChat();
                    getChat(0);
                    MessageActivity.fromChat = true;
                }
                JoysaleApplication.hideSoftKeyboard(ChatActivity.this);
                finish();
                break;
                /*Location Share*/
            case R.id.sharelocation:
                Intent in = new Intent(ChatActivity.this, LocationActivity.class);
                in.putExtra(Constants.FROM, "chat");
                in.putExtra(Constants.CHATID, chatId);
                in.putExtra(Constants.TAG_USERNAME, userName);
                in.putExtra(Constants.TAG_USER_ID, userId);
                in.putExtra(Constants.TAG_USERIMAGE_M, userImage);
                in.putExtra(Constants.TAG_FULL_NAME, fullName);
                startActivityForResult(in, LOCATION_FETCH_ACTION);
                break;
                /*Image Share*/
            case R.id.shareImg:
                if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA,WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    ImagePicker.pickImage(this, "Select your image:");
                }
                break;
            case R.id.send:
                if (editText.getText().toString().trim().length() > 0) {
                    String j = editText.getText().toString();
                    send.setOnClickListener(null);
                    if (j.length() > 0) {
                        if (j.contains("/>") || j.contains("</")) {
                            Toast.makeText(getApplicationContext(), "hai!! null", Toast.LENGTH_LONG).show();
                            editText.setText("");

                        } else if (j.contains(">") && j.contains("<")) {
                            editText.setText("");
                        }
                    }

                    sendMessage(editText.getText().toString());

                    runOnUiThread(new Runnable() {
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });

                    editText.setText("");

                } else {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.please_enter_message),Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.settingbtn:
                Display display = this.getWindowManager().getDefaultDisplay();

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.share_new, android.R.id.text1, values);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.share, null);
                layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
                final PopupWindow popup = new PopupWindow(ChatActivity.this);
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
                        popup.dismiss();
                        openAction(values.get(position));
                    }
                });
                break;
            case R.id.userImg:
                Intent u = new Intent(ChatActivity.this, Profile.class);
                u.putExtra(Constants.TAG_USER_ID, userId);
                startActivity(u);
                break;
        }
    }

}