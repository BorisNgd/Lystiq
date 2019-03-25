package com.app.lystiq;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.app.lystiq.ChatActivity.LOCATION_FETCH_ACTION;

/**
 * Created by hitasoft.
 * <p>
 * This class is for Exchange Chat
 */

public class ExchangeView extends Activity implements OnClickListener, TextWatcher, OnScrollListener {

    /**
     * Declare Layout Elements
     **/
    TextView title, itemName, myitemName, time, failed, success, username, nullText;
    ListView listView;
    ImageView userImage, myitemImage, exchangeImage, backBtn;
    AVLoadingIndicatorView progress, topProgress, typing;
    EditText editText;
    ViewGroup header, footer;
    LinearLayout send, shareImg, sharelocation;
    ProgressDialog pd;

    private Socket mSocket;
    ChatAdapter chatAdapter;
    InputMethodManager imm;
    Handler handler = new Handler();
    Runnable runnable;

    /**
     * Declare Variables
     **/
    static final String TAG = "ExchangeView";
    public static String fullName = "";
    String userName = "", imageType = "local", clickedBtn, chatId, type, existingFileName,
            exchangeItemId, myItemId, exchangerId;
    boolean pulldown = false, loading = false, meTyping, receiverTyping;
    int black, currentPage = 0, position;

    ArrayList<HashMap<String, String>> chats = new ArrayList<HashMap<String, String>>(), tempAry = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> datas = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exchange_view);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        shareImg = (LinearLayout) findViewById(R.id.shareImg);
        sharelocation = (LinearLayout) findViewById(R.id.sharelocation);
        title = (TextView) findViewById(R.id.title);
        listView = (ListView) findViewById(R.id.listView);
        send = (LinearLayout) findViewById(R.id.send);
        editText = (EditText) findViewById(R.id.editText);
        userImage = (ImageView) findViewById(R.id.userImage);
        exchangeImage = (ImageView) findViewById(R.id.exitemImage);
        myitemImage = (ImageView) findViewById(R.id.myitemImage);
        itemName = (TextView) findViewById(R.id.exitemName);
        myitemName = (TextView) findViewById(R.id.myitemName);
        time = (TextView) findViewById(R.id.time);
        failed = (TextView) findViewById(R.id.failed);
        success = (TextView) findViewById(R.id.success);
        username = (TextView) findViewById(R.id.userName);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        LayoutInflater inflater = getLayoutInflater();
        header = (ViewGroup) inflater.inflate(R.layout.chat_header, null, false);
        listView.addHeaderView(header, null, false);

        footer = (ViewGroup) getLayoutInflater().inflate(R.layout.chat_footer, null);
        listView.addFooterView(footer);

        listView.setSmoothScrollbarEnabled(true);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        topProgress = (AVLoadingIndicatorView) header.findViewById(R.id.topProgress);
        nullText = (TextView) header.findViewById(R.id.nulltext);
        typing = (AVLoadingIndicatorView) footer.findViewById(R.id.typing);

        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        black = getResources().getColor(R.color.black);
        title.setText(getString(R.string.myexchange));
        datas = (HashMap<String, String>) getIntent().getExtras().get(Constants.DATA);
        Log.e(TAG, "onCreate: " + datas);
        exchangeItemId = datas.get("eitem_id");
        myItemId = datas.get("mitem_id");
        exchangerId = datas.get("exchanger_id");
        if (datas.get(Constants.TAG_EXCHANGERNAME) != null)
            fullName = datas.get(Constants.TAG_EXCHANGERNAME);
        position = (int) getIntent().getExtras().get(Constants.POSITION);
        type = (String) getIntent().getExtras().get(Constants.TAG_TYPE);

        // initialize dialog
        pd = new ProgressDialog(ExchangeView.this,R.style.AppCompatAlertDialogStyle);
        pd.setMessage(ExchangeView.this.getString(R.string.loading));
        pd.setCancelable(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            pd.setIndeterminateDrawable(drawable);
        }
        pd.show();

        Picasso.with(ExchangeView.this).load(datas.get(Constants.TAG_EXCHANGERIMG)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImage);
        Picasso.with(ExchangeView.this).load(datas.get("e" + Constants.TAG_ITEMIMAGE)).into(exchangeImage);
        Picasso.with(ExchangeView.this).load(datas.get("m" + Constants.TAG_ITEMIMAGE)).into(myitemImage);

        itemName.setText(datas.get("e" + Constants.TAG_ITEM_NAME));
        myitemName.setText(datas.get("m" + Constants.TAG_ITEM_NAME));
        username.setText(datas.get(Constants.TAG_EXCHANGERNAME));
        time.setText(datas.get(Constants.TAG_EXCHANGETIME));

        userName = datas.get(Constants.TAG_EXCHANGERUSERNAME);
        String status = datas.get(Constants.TAG_STATUS);
        Log.v(TAG, "userName=" + userName);

        /** Method for join the user to chat **/

        JSONObject jobj = new JSONObject();
        try {
            jobj.put("joinid", GetSet.getUserName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JoysaleApplication app = (JoysaleApplication) getApplication();
        mSocket = app.getSocket();
        mSocket.on("exmessage", onMessage);
        mSocket.on("exmessageTyping", onTyping);
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
        });
        mSocket.connect();

        mSocket.emit("exchangejoin", jobj);

        if (datas.get(Constants.TAG_REQUEST_BY_ME).equals("true")) {
            if (status.equals("Pending")) {
                success.setText(getString(R.string.cancel));
                failed.setVisibility(View.GONE);
            } else if (status.equals("Accepted")) {
                failed.setText(getString(R.string.failed));
                success.setText(getString(R.string.success));
            }

        } else {
            if (status.equals("Pending")) {
                failed.setText(getString(R.string.decline));
                success.setText(getString(R.string.accept));
            } else if (status.equals("Accepted")) {
                failed.setText(getString(R.string.failed));
                success.setText(getString(R.string.success));
            }
        }

        backBtn.setOnClickListener(this);
        send.setOnClickListener(this);
        editText.addTextChangedListener(this);
        failed.setOnClickListener(this);
        success.setOnClickListener(this);
        shareImg.setOnClickListener(this);
        sharelocation.setOnClickListener(this);
        editText.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER});
        myitemImage.setOnClickListener(this);
        exchangeImage.setOnClickListener(this);
        userImage.setOnClickListener(this);

        // initialize Adapter class
        chatAdapter = new ChatAdapter(ExchangeView.this, chats);
        listView.setAdapter(chatAdapter);

        try {
            initializeChatUI();
            getChat(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for receiving the instant messages & typing status
     **/
    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Log.v("onTyping", "onTyping=" + args[0]);
                        JSONObject data = (JSONObject) args[0];
                        if (data.getString(Constants.SOCK_RECEIVER).equals(userName) && data.getString(Constants.TAG_MESSAGE).equals(Constants.TAG_TYPE)) {
                            if (!receiverTyping) {
                                receiverTyping = true;
                                typing.setVisibility(View.VISIBLE);
                                if (chats.size() > 0) {
                                    listView.setSelection(chats.size() - 1);
                                }
                                typing.startAnimation(AnimationUtils.loadAnimation(ExchangeView.this, R.anim.abc_slide_in_bottom));
                            }
                        } else {
                            receiverTyping = false;
                            typing.setVisibility(View.GONE);
                            typing.startAnimation(AnimationUtils.loadAnimation(ExchangeView.this, R.anim.abc_slide_out_bottom));
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
            Log.v("onMessage", "onMessage=" + args);
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
                }

                Log.v(TAG, "chatdatafromsocket=" + hmap);
                chats.add(hmap);
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
            }
        }
    };

    public void dialog(Context ctx, String title, String content) {
        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        Display display = ((Activity) ctx).getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 80 / 100, LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(title);
        alertMsg.setText(content);
        alertIcon.setImageResource(R.drawable.success_icon);

        alertOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //	doActionOnClick();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                doActionOnClick();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void doActionOnClick() {
        if (failed.getText().toString().equals(getString(R.string.decline)) && success.getText().toString().equals(getString(R.string.accept))) {
            if (clickedBtn.equals("success")) {
                failed.setText(getString(R.string.failed));
                success.setText(getString(R.string.success));
                if (type.equals("incoming")) {
                    Log.v(TAG, "checkstatus" + success.isEnabled());
                    IncomeExchange.incomingAry.get(position).put(Constants.TAG_STATUS, "Accepted");
                    IncomeExchange.exchangeAdapter.notifyDataSetChanged();
                } else if (type.equals("outgoing")) {
                    OutgoingExchange.outgoingAry.get(position).put(Constants.TAG_STATUS, "Accepted");
                    OutgoingExchange.exchangeAdapter.notifyDataSetChanged();
                }
            } else {
                ExchangeActivity.type = "failed";
                ExchangeActivity.statusChanged = true;
                finish();
            }

        } else if (failed.getText().toString().equals(getString(R.string.failed)) && success.getText().toString().equals(getString(R.string.success))) {
            if (clickedBtn.equals("success")) {
                ExchangeActivity.type = "success";
                ExchangeActivity.statusChanged = true;
                finish();
            } else {
                ExchangeActivity.type = "failed";
                ExchangeActivity.statusChanged = true;
                finish();
            }

        } else if (success.getText().toString().equals(getString(R.string.cancel))) {
            ExchangeActivity.type = "failed";
            ExchangeActivity.statusChanged = true;
            finish();
        }
    }

    private void disconnectSocket() {
        if (mSocket != null) {
            mSocket.off("exmessage");
            mSocket.off("exmessageTyping");
            mSocket.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnectSocket();
        fullName = "";
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0 && !(loading)) {
            loading = true;
            topProgress.setVisibility(View.VISIBLE);
            nullText.setVisibility(View.GONE);
            currentPage++;
            pulldown = true;
            if (JoysaleApplication.isNetworkAvailable(ExchangeView.this)) {
                initializeChatUI();
                getChat(currentPage);
            }
        }
    }

    /**
     * Function to Call a Socket to update a message
     **/

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
            jobj.put(Constants.SOCK_SOURCE_ID, datas.get(Constants.TAG_EXCHANGEID));
            jobj.put(Constants.TAG_MESSAGE, message);
            Log.v(TAG, "sendDataSocketjson=" + jobj);
            mSocket.emit("exmessage", jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(ExchangeView.this);
        super.onPause();
        fullName = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(ExchangeView.this);
        if (datas.get(Constants.TAG_EXCHANGERNAME) != null)
            fullName = datas.get(Constants.TAG_EXCHANGERNAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult");
        if (resultCode == -1 && requestCode == 234) {
            Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
            ImageStorage imageStorage = new ImageStorage(ExchangeView.this, ExchangeView.this);
            final String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
            String imageStatus = imageStorage.saveToSdCard(bitmap, "sent", timestamp + ".jpg", timestamp);
            if (imageStatus.equals("success")) {
                final File file = imageStorage.getImage("sent", timestamp + ".jpg");
                String filepath = file.getAbsolutePath();
                Log.v(TAG, "selectedImageFile: " + filepath);
                ImageCompression imageCompression = new ImageCompression(ExchangeView.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        long unixTime = System.currentTimeMillis() / 1000L;
                        HashMap<String, String> hmap = new HashMap<String, String>();
                        hmap.put(Constants.TAG_SENDER, GetSet.getUserName());
                        hmap.put(Constants.DATE, Long.toString(unixTime));
                        hmap.put(Constants.TAG_TYPE, "image");
                        hmap.put("localpath", imagePath);
                        chats.add(hmap);
                        https://www.dropbox.com/sh/hu9l6i4kg40aoby/AADGCIvF3mEe1sDSPVNPedhia?dl=0.add(hmap);
                        Log.v(TAG, "checkdatachat" + chats);
                        chatAdapter.notifyDataSetChanged();
                        new UploadImage(ExchangeView.this, "Chat", userImage, unixTime).execute(imagePath);
                    }
                };
                imageCompression.execute(filepath);
            } else {
                Toast.makeText(this, getString(R.string.profile_problem), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_FETCH_ACTION) {
            Log.v(TAG, "onActivityResult-Execute");
            HashMap<String, String> hmap = new HashMap<String, String>();
            if (data != null) {
                String latitude = data.getStringExtra("current_latitude");
                String longitude = data.getStringExtra("current_longitude");
                hmap.put(Constants.TAG_LATITUDE, latitude);
                hmap.put(Constants.TAG_LONGITUDE, longitude);
                hmap.put(Constants.TAG_SENDER, GetSet.getUserName());
                hmap.put(Constants.DATE, String.valueOf(System.currentTimeMillis() / 1000L));
                hmap.put(Constants.TAG_TYPE, "share_location");
                Log.v(TAG, "onActivityResultLocation=" + hmap);
                String jsonStr = data.getStringExtra("jsonObject");
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    mSocket.emit("exmessage", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                chats.add(hmap);
                chatAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * class for upload Image to Server
     */

    class UploadImage extends AsyncTask<String, String, String> {
        JSONObject jsonobject = null;
        String jsonResponse = "", imageName = "", mFrom;

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
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            try {
                existingFileName = imgpath[0];
                Log.v(TAG, "existingFileName=" + existingFileName);
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

                Log.v(TAG, "buffer=" + buffer);

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    Log.v(TAG, "bytesRead=" + bytesRead);
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
            Log.d(TAG, "uploadresponse=" + jsonResponse);
            try {
                jsonobject = new JSONObject(jsonResponse);
                if (jsonobject.getString(Constants.TAG_STATUS).equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    imageName = image.getString("Name");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return jsonResponse;
        }

        @Override
        protected void onPreExecute() {
            pd.show();
        }

        @Override
        protected void onPostExecute(String res) {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            try {
                Log.v(TAG, "uploadimgjson=" + res);

                JSONObject jsonobject = new JSONObject(res);
                if (jsonobject.getString(Constants.TAG_STATUS).equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    imageName = DefensiveClass.optString(image, "Name");
                    imageName = image.getString("Name");
                    callSocket(mUnixTimeStamp, "image", image.getString("View_url"));
                }

                sendChat("image", "", imageName);

                runOnUiThread(new Runnable() {
                    public void run() {
                        //	nullLay.setVisibility(View.GONE);
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
     * Class for Download Image from Server and Store in External Storage
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
                    ImageStorage imageStorage = new ImageStorage(ExchangeView.this, ExchangeView.this);
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
     * Function for get the chatid between two users
     **/

    private void getChatId() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_CHAT_ID, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                try {
                    Log.v(TAG, "getchatidresponse=" + json);
                    JSONObject jobj = new JSONObject(json);
                    if (jobj.getString(Constants.TAG_STATUS).equals("true")) {
                        chatId = jobj.getString(Constants.TAG_CHAT_ID);
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
                map.put(Constants.TAG_RECEIVER_ID, datas.get(Constants.TAG_EXCHANGERID));
                Log.v(TAG, "getchatidparams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
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

    /**
     * Function for Json Parsing
     */

    private ArrayList<HashMap<String, String>> parsing(String url) {
        ArrayList<HashMap<String, String>> chats = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject json = new JSONObject(url);
            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
            if (response.equalsIgnoreCase("true")) {
                chatId = DefensiveClass.optString(json, Constants.TAG_CHAT_ID);
                JSONObject chobj = json.optJSONObject(Constants.TAG_CHATS);
                if (chobj != null) {
                    JSONArray chat = chobj.optJSONArray(Constants.TAG_CHATS);
                    if (chat != null) {
                        for (int i = 0; i < chat.length(); i++) {
                            HashMap<String, String> map = new HashMap<String, String>();
                            JSONObject temp = chat.getJSONObject(i);
                            map.put(Constants.TAG_TYPE, DefensiveClass.optString(temp, Constants.TAG_TYPE));
                            map.put(Constants.TAG_SENDER, DefensiveClass.optString(temp, Constants.TAG_SENDER));

                            JSONObject msg = temp.getJSONObject(Constants.TAG_MESSAGE);
                            map.put(Constants.TAG_MESSAGE, DefensiveClass.optString(msg, Constants.TAG_MESSAGE));
                            map.put(Constants.DATE, DefensiveClass.optString(msg, Constants.TAG_CHATTIME));
                            map.put(Constants.TAG_USR_LATITUDE, DefensiveClass.optString(msg, Constants.TAG_USR_LATITUDE));
                            map.put(Constants.TAG_USR_LONGITUDE, DefensiveClass.optString(msg, Constants.TAG_USR_LONGITUDE));
                            map.put(Constants.TAG_UPLOADED_IMG_URL, DefensiveClass.optString(msg, Constants.TAG_UPLOADED_IMG_URL));
                            chats.add(map);

                            new DownloadAndStoreImg(DefensiveClass.optString(temp, Constants.TAG_TYPE), AppUtils.getValidUrl(DefensiveClass.optString(msg, Constants.TAG_UPLOADED_IMG_URL)), DefensiveClass.optString(msg, Constants.TAG_CHATTIME)).execute();
                        }
                    }
                }
            } else if (response.equalsIgnoreCase("error")) {
                JoysaleApplication.disabledialog(ExchangeView.this, json.optString(Constants.TAG_MESSAGE), GetSet.getUserId());
            } else {
                getChatId();
            }
        } catch (JSONException e) {
            getChatId();
            e.printStackTrace();
        } catch (NullPointerException e) {
            getChatId();
            e.printStackTrace();
        } catch (Exception e) {
            getChatId();
            e.printStackTrace();
        }
        return chats;
    }

    /**
     * Function for get the last conversation
     **/

    private void getChat(final int pageCount) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_CHAT, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                Log.v(TAG, "getchatresponse=" + json);
                tempAry.clear();
                tempAry.addAll(parsing(json));
                Collections.reverse(tempAry);
                ArrayList<HashMap<String, String>> backup = new ArrayList<HashMap<String, String>>();
                backup.addAll(chats);
                chats.clear();
                chats.addAll(tempAry);
                chats.addAll(backup);
                try {
                    if (chats.size() == 0) {
                        listView.setOnScrollListener(null);
                    } else {
                        listView.setOnScrollListener(ExchangeView.this);
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
                            if (chats.size() > 18) {
                                if (tempAry.size() == 0) {
                                    nullText.setVisibility(View.VISIBLE);
                                    topProgress.setVisibility(View.GONE);
                                }
                            }
                            loading = false;
                            topProgress.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.GONE);
                            chatAdapter.notifyDataSetChanged();
                            if (pd != null && pd.isShowing()) {
                                pd.dismiss();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    if (pd != null && pd.isShowing()) {
                        pd.dismiss();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                int offset = (pageCount * 20);
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                map.put(Constants.TAG_RECEIVER_ID, datas.get(Constants.TAG_EXCHANGERID));
                map.put(Constants.TAG_TYPE, "exchange");
                map.put(Constants.TAG_OFFSET, Integer.toString(offset));
                map.put(Constants.TAG_LIMIT, "20");
                map.put(Constants.TAG_SOURCE_ID, datas.get(Constants.TAG_EXCHANGEID));
                Log.v(TAG, "getchatparams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);

    }

    /**
     * Function for Initialize chat
     **/

    private void initializeChatUI() {
        loading = true;
        if (pulldown) {
            listView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            pulldown = false;
            topProgress.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * adapter for list the conversation in listview
     **/

    public class ChatAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> Items;
        ViewHolder holder = null;
        Context mContext;

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
                holder.left_image_lay = (RelativeLayout) convertView.findViewById(R.id.left_image_lay);
                holder.right_image_lay = (RelativeLayout) convertView.findViewById(R.id.right_image_lay);
                holder.left_msg_layout = (RelativeLayout) convertView.findViewById(R.id.left_msg_layout);
                holder.right_msg_layout = (RelativeLayout) convertView.findViewById(R.id.right_msg_layout);
                holder.leftImgTime = (TextView) convertView.findViewById(R.id.leftImgTime);
                holder.rightImgTime = (TextView) convertView.findViewById(R.id.rightImgTime);
                holder.leftDelete = (ImageView) convertView.findViewById(R.id.leftDelete);
                holder.rightDelete = (ImageView) convertView.findViewById(R.id.rightDelete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final HashMap<String, String> tempMap = Items.get(position);
            holder.leftLay.setVisibility(View.GONE);
            holder.rightLay.setVisibility(View.GONE);
            holder.dateLay.setVisibility(View.GONE);
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
                            holder.rightDelete.setVisibility(View.GONE);

                            holder.rightMsg.setText(tempMap.get(Constants.TAG_MESSAGE));
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));

                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.VISIBLE);
                            holder.left_image_lay.setVisibility(View.GONE);
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

                            String imageName = "";
                            File file = null;
                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            ImageStorage imageStorage = new ImageStorage(ExchangeView.this, mContext);
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

                            int imgSize = JoysaleApplication.dpToPx(mContext, 150);
                            if (tempMap.get(Constants.TAG_UPLOADED_IMG_URL) != null) {
                                String imgSplit = AppUtils.getImageName(tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                ImageStorage imageStorage = new ImageStorage(ExchangeView.this, mContext);
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
                                if (ContextCompat.checkSelfPermission(ExchangeView.this, WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ExchangeView.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                } else {
                                    ImageStorage imageStorage = new ImageStorage(ExchangeView.this, mContext);
                                    if (tempMap.containsKey("localpath")) {
                                        String imgSplit = AppUtils.getImageName(tempMap.get("localpath"));
                                        if (imageStorage.checkifImageExists("sent", imgSplit)) {
                                            Log.v(TAG, "Already Downloaded");
                                            File file = imageStorage.getImage("sent", imgSplit);
                                            Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                            intent.putExtra(Constants.IMAGETYPE, "local");
                                            intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                            Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                            ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                        }
                                    } else {
                                        Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "remote");
                                        intent.putExtra(Constants.KEY_IMAGE, tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                        Pair<View, String> bodyPair = Pair.create(view, tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                        ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                    }
                                }
                            }
                        });

                        holder.leftImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (ContextCompat.checkSelfPermission(ExchangeView.this, WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ExchangeView.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                                } else {
                                    ImageStorage imageStorage = new ImageStorage(ExchangeView.this, mContext);
                                    if (imageType.equals("local")) {
                                        if (tempMap.get(Constants.TAG_UPLOADED_IMG_URL) != null) {
                                            String imgSplit = AppUtils.getImageName(tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                            if (imageStorage.checkifImageExists("", imgSplit)) {
                                                Log.v(TAG, "Already Downloaded");
                                                File file = imageStorage.getImage("", imgSplit);
                                                Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                                intent.putExtra(Constants.IMAGETYPE, "local");
                                                intent.putExtra(Constants.KEY_IMAGE, file.getAbsolutePath());
                                                Pair<View, String> bodyPair = Pair.create(view, file.getAbsolutePath());
                                                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                                ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                            }
                                        }
                                    } else {
                                        imageType = "local";
                                        //Instant Received Image
                                        Intent intent = new Intent(ExchangeView.this, ViewFullImage.class);
                                        intent.putExtra(Constants.IMAGETYPE, "remote");
                                        intent.putExtra(Constants.KEY_IMAGE, tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                        Pair<View, String> bodyPair = Pair.create(view, tempMap.get(Constants.TAG_UPLOADED_IMG_URL));
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ExchangeView.this, bodyPair);
                                        ActivityCompat.startActivity(ExchangeView.this, intent, options.toBundle());
                                    }
                                }
                            }
                        });
                        break;

                    case "share_location":
                        int imgSize = JoysaleApplication.dpToPx(ExchangeView.this, 150);
                        String latitude = tempMap.get(Constants.TAG_USR_LATITUDE).trim();
                        String longitude = tempMap.get(Constants.TAG_USR_LONGITUDE).trim();
                        int width = JoysaleApplication.dpToPx(ExchangeView.this, 280);
                        int height = JoysaleApplication.dpToPx(ExchangeView.this, 190);
                        String mapImgUrl = "http://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=15&size=" + width + "x" + height + "&sensor=false&markers=" + latitude + "," + longitude + "|color:red" + "&key=" + Constants.GOOGLE_API_KEY;

                        if (tempMap.get(Constants.TAG_SENDER).equals(GetSet.getUserName())) {//Right Side
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.GONE);
                            holder.right_image_lay.setVisibility(View.VISIBLE);

                            Picasso.with(ExchangeView.this).load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(ExchangeView.this).into(holder.rightImage);
                            holder.rightImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AppUtils.callMap(tempMap.get(Constants.TAG_USR_LATITUDE), tempMap.get(Constants.TAG_USR_LONGITUDE), ExchangeView.this);
                                }
                            });
                            holder.rightImgTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));
                        } else {
                            holder.leftLay.setVisibility(View.VISIBLE);
                            holder.left_msg_layout.setVisibility(View.GONE);
                            holder.left_image_lay.setVisibility(View.VISIBLE);

                            holder.leftImage.setTag("location");
                            Picasso.with(mContext).load(mapImgUrl).resize(imgSize, imgSize).centerCrop().tag(mContext).into(holder.leftImage);
                            holder.leftImage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AppUtils.callMap(tempMap.get(Constants.TAG_USR_LATITUDE), tempMap.get(Constants.TAG_USR_LONGITUDE), ExchangeView.this);
                                }
                            });
                            holder.leftImgTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));
                        }
                        break;

                    case "about":
                        if (tempMap.get(Constants.TAG_ITEM_STATUS).equals("1")) {
                            holder.itemLay.setVisibility(View.VISIBLE);
                            holder.aboutMsg.setVisibility(View.VISIBLE);

                            Picasso.with(ExchangeView.this).load(tempMap.get(Constants.TAG_ITEM_IMAGE)).into(holder.itemImage);
                            String name = getString(R.string.about) + " " + "<font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + tempMap.get(Constants.TAG_ITEM_TITLE) + "</font>";
                            holder.itemName.setText(Html.fromHtml(name));
                            holder.aboutDate.setText(chatDate);
                            holder.aboutMsg.setText(JoysaleApplication.stripHtml(tempMap.get(Constants.TAG_MESSAGE)));
                        } else {
                            holder.rightLay.setVisibility(View.VISIBLE);
                            holder.right_msg_layout.setVisibility(View.VISIBLE);
                            holder.right_image_lay.setVisibility(View.GONE);
                            holder.rightDelete.setVisibility(View.VISIBLE);

                            holder.rightMsg.setText(getString(R.string.product_removed_msg));
                            holder.rightTime.setText(getTime(Long.parseLong(Items.get(position).get(Constants.DATE)) * 1000));
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

        private class ViewHolder {
            LinearLayout main;
            TextView message, itemName, aboutDate, price, aboutMsg, date, leftMsg, rightMsg, leftTime,
                    rightTime, leftImgTime, rightImgTime;
            RelativeLayout leftLay, rightLay, dateLay, itemLay, left_image_lay, right_image_lay, left_msg_layout, right_msg_layout;
            ImageView itemImage, leftImage, rightImage, leftDelete, rightDelete;
        }
    }

    /**
     * Fucntion for send the message to user
     **/

    private void sendChat(final String type, final String params, final String imageName) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEND_CHAT, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                Log.v(TAG, "exchangesendResponse=" + res);
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
                try {
                    map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                    map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                    map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                    map.put(Constants.TAG_CHAT_ID, chatId);
                    map.put(Constants.TAG_SOURCE_ID, datas.get(Constants.TAG_EXCHANGEID));
                    map.put(Constants.TAG_TYPE, type);
                    map.put(Constants.TAG_CHAT_TYPE, "exchange");
                    map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
                    map.put(Constants.TAG_MESSAGE, params);
                    map.put(Constants.TAG_IMAGE_URL, imageName);
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            chats.remove(chats.size() - 1);
                            chatAdapter.notifyDataSetChanged();
                            JoysaleApplication.dialog(ExchangeView.this, getString(R.string.alert), getString(R.string.symbols_not_supported));
                        }
                    });
                }
                Log.v(TAG, "exchangesendparams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for change the status of exchanges
     **/

    private void changeStatus(final String status) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_CHANGE_EXCHANGE, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                try {
                    Log.v(TAG, "changestatusresponse=" + result);
                    JSONObject jobj = new JSONObject(result);
                    if (jobj.getString(Constants.TAG_STATUS).equals("true")) {
                        dialog(ExchangeView.this, getString(R.string.success), getString(R.string.exchange_status_chngd));
                        failed.setEnabled(true);
                        success.setEnabled(true);
                        success.setOnClickListener(ExchangeView.this);
                        failed.setOnClickListener(ExchangeView.this);
                    } else {
                        JoysaleApplication.dialog(ExchangeView.this, getString(R.string.alert), jobj.getString(Constants.TAG_MESSAGE));
                        failed.setEnabled(true);
                        success.setEnabled(true);
                        success.setOnClickListener(ExchangeView.this);
                        failed.setOnClickListener(ExchangeView.this);
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
                success.setOnClickListener(ExchangeView.this);
                failed.setOnClickListener(ExchangeView.this);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_USERID, GetSet.getUserId());
                map.put(Constants.TAG_EXCHANGEID, datas.get(Constants.TAG_EXCHANGEID));
                map.put(Constants.TAG_STATUS, status);
                Log.v(TAG, "changestatusparams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

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
                jobj.put(Constants.SOCK_SOURCE_ID, datas.get(Constants.TAG_EXCHANGEID));
                jobj.put(Constants.TAG_MESSAGE, Constants.TAG_TYPE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("exmessageTyping", jobj);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.v(TAG, "afterTextChanged");
        runnable = new Runnable() {

            public void run() {
                Log.v(TAG, "stop typing");
                meTyping = false;
                JSONObject jobj = new JSONObject();
                try {
                    jobj.put(Constants.SOCK_SENDERID, userName);
                    jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
                    jobj.put(Constants.SOCK_SOURCE_ID, datas.get(Constants.TAG_EXCHANGEID));
                    jobj.put(Constants.TAG_MESSAGE, "untype");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("exmessageTyping", jobj);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    /**
     * On click Events
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                fullName = "";
                disconnectSocket();
                finish();
                break;
            /*Location Share*/
            case R.id.sharelocation:
                Intent in = new Intent(ExchangeView.this, LocationActivity.class);
                in.putExtra(Constants.FROM, "chat");
                in.putExtra(Constants.TAG_USERNAME, userName);
                in.putExtra(Constants.TAG_USER_ID, datas.get(Constants.TAG_EXCHANGERID));
                in.putExtra(Constants.TAG_SOURCE_ID, datas.get(Constants.TAG_EXCHANGEID));
                in.putExtra(Constants.TAG_USERIMAGE_M, datas.get(Constants.TAG_EXCHANGERIMG));
                in.putExtra(Constants.TAG_FULL_NAME, datas.get(Constants.TAG_EXCHANGERNAME));
                in.putExtra(Constants.CHATID, chatId);
                in.putExtra(Constants.TAG_CHAT_TYPE, "exchange");
                in.putExtra(Constants.TAG_EXCHANGEID, datas.get(Constants.TAG_EXCHANGEID));
                startActivityForResult(in, LOCATION_FETCH_ACTION);
                break;
            /*Image Share*/
            case R.id.shareImg:
                if (ContextCompat.checkSelfPermission(ExchangeView.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ExchangeView.this, new String[]{WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    ImagePicker.pickImage(this, "Select your image:");
                }
                break;
            case R.id.send:
                if (editText.getText().toString().trim().length() > 0) {
                    long unixTime = System.currentTimeMillis() / 1000L;
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    callSocket(unixTime, "text", editText.getText().toString().trim());

                    HashMap<String, String> hmap = new HashMap<String, String>();
                    hmap.put(Constants.TAG_MESSAGE, editText.getText().toString().trim());
                    hmap.put(Constants.TAG_SENDER, GetSet.getUserName());
                    hmap.put(Constants.DATE, Long.toString(unixTime));
                    hmap.put(Constants.TAG_TYPE, "message");
                    chats.add(hmap);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                        }
                    });
                    try {
                        sendChat("normal", editText.getText().toString().trim(), "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    editText.setText("");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            if (chats.size() > 0) {
                                listView.setSelection(chats.size() - 1);
                            }
                        }
                    });
                } else {
                    editText.setError(getResources().getString(R.string.please_enter_message));
                }

                break;
            case R.id.failed:
                failed.setEnabled(false);
                failed.setOnClickListener(null);
                clickedBtn = "failed";

                String status = failed.getText().toString();
                if (status.equals(getString(R.string.failed))) {
                    changeStatus("failed");
                } else if (status.equals(getString(R.string.decline))) {
                    changeStatus("decline");
                }
                Log.v(TAG, "clicked");
                break;
            case R.id.success:
                success.setEnabled(false);
                success.setOnClickListener(null);

                clickedBtn = "success";

                String stat = success.getText().toString();
                Log.v(TAG, "clickedsucces" + stat);
                if (stat.equals(getString(R.string.success))) {
                    changeStatus("success");
                } else if (stat.equals(getString(R.string.accept))) {
                    changeStatus("accept");
                } else if (stat.equals(getString(R.string.cancel))) {
                    changeStatus("cancel");
                } else {
                    Log.v(TAG, "checkstatus" + stat);
                }
                break;
            case R.id.myitemImage:
                if (pd != null && !pd.isShowing()) {
                    pd.show();
                }
                getItemData(myItemId);
                break;
            case R.id.exitemImage:
                if (pd != null && !pd.isShowing()) {
                    pd.show();
                }
                getItemData(exchangeItemId);
                break;
            case R.id.userImage:
                Intent u = new Intent(ExchangeView.this, Profile.class);
                u.putExtra(Constants.TAG_USER_ID, exchangerId);
                startActivity(u);
                break;
        }
    }

    // get Data from Api
    private void getItemData(String itemId) {
        final ArrayList<HashMap<String, String>> HomeItems = new ArrayList<HashMap<String, String>>();
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEARCH_ITEM, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                Log.i(TAG, "getItemData onResponse: " + json);
                ItemsParsing parse = new ItemsParsing(ExchangeView.this, GetSet.getUserId());
                HomeItems.addAll(parse.parsing(json));

                if (pd.isShowing()) {
                    pd.dismiss();
                }
                if (HomeItems.size() == 0) {
                    Toast.makeText(ExchangeView.this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(ExchangeView.this, DetailActivity.class);
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
                if (pd.isShowing()) {
                    pd.dismiss();
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
                Log.i(TAG, "getItemData: " + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

}