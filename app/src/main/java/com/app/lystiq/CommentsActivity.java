package com.app.lystiq;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.external.TimeAgo;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.squareup.picasso.Picasso;
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
 * This class is for User's Comment
 */

public class CommentsActivity extends AppCompatActivity implements OnClickListener {

    /**
     * Declare Layout Elements
     **/
    ListView listView;
    EditText commentText;
    ImageView back, productImg;
    AVLoadingIndicatorView progress;
    LinearLayout nullLay, send;
    TextView title, productTitle;
    InputMethodManager imm;

    /**
     * Declare Variables
     **/
    static final String TAG = "CommentsActivity";
    String from, itemId, productName, productImage;
    int position;

    CommentsAdapter commentsAdapter;
    ArrayList<HashMap<String, String>> commentsList = null;
    Display display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_page);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        listView = (ListView) findViewById(R.id.comments_list);
        commentText = (EditText) findViewById(R.id.commentEditText);
        nullLay = (LinearLayout) findViewById(R.id.nullLay);
        back = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.cornerTitle);
        send = (LinearLayout) findViewById(R.id.send);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        productTitle = (TextView) findViewById(R.id.productTitle);
        productImg = (ImageView) findViewById(R.id.productImg);

        title.setText(getResources().getString(R.string.comments));
        imm.hideSoftInputFromWindow(commentText.getWindowToken(), 0);

        commentsList = new ArrayList<HashMap<String, String>>();
        from = getIntent().getExtras().getString(Constants.FROM);
        itemId = getIntent().getExtras().getString("itemId");
        position = getIntent().getExtras().getInt("position");
        productName = getIntent().getExtras().getString("productName");
        productImage = getIntent().getExtras().getString("productImage");

        back.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        productTitle.setVisibility(View.VISIBLE);
        productImg.setVisibility(View.VISIBLE);
        commentText.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(120)});

        display = this.getWindowManager().getDefaultDisplay();

        productTitle.setText(productName);
        if (productImage != null && !productImage.equals(null)){
            Picasso.with(CommentsActivity.this).load(productImage.replace("350", "70")).into(productImg);
        }

        back.setOnClickListener(this);
        send.setOnClickListener(this);

        if (JoysaleApplication.isNetworkAvailable(CommentsActivity.this)) {
            listView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            getComments();
            commentsAdapter = new CommentsAdapter(CommentsActivity.this, commentsList);
            listView.setAdapter(commentsAdapter);
        } else {
            //JoysaleApplication.dialog(CommentsActivity.this, "Error!", getResources().getString(R.string.checkconnection));
        }

    }


    /**
     * This Function is to update the comments count in previous pages
     **/

    private void commentNotify(String addRdelete) {
        int count = 0;
        String comment_count = "";
        int pos = getIntent().getExtras().getInt("position");
        if (DetailActivity.itemMap.size() > 0 && DetailActivity.commentCount != null) {
            count = Integer.parseInt(DetailActivity.itemMap.get(Constants.TAG_COMMENTCOUNT));
            if (addRdelete.equals("add")) {
                comment_count = Integer.toString((count + 1));
            } else {
                comment_count = Integer.toString((count - 1));
            }
            DetailActivity.itemMap.put(Constants.TAG_COMMENTCOUNT, comment_count);
            DetailActivity.commentCount.setText(comment_count + " " + getResources().getString(R.string.comments));
            switch (from) {
                case "home":
                    notifyPage(FragmentMainActivity.homeItemList, pos, comment_count);
                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                    break;
                case "mylisting":
                    notifyPage(MyListing.addedItems, pos, comment_count);
                    MyListing.itemAdapter.notifyDataSetChanged();
                    break;
                case "liked":
                    notifyPage(LikedItems.likedItems, pos, comment_count);
                    LikedItems.itemAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }


    private void notifyPage(ArrayList<HashMap<String, String>> data, int pos, String comment_count) {
        try {
            data.get(pos).put(Constants.TAG_COMMENTCOUNT, comment_count);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void confirmDialog(final String commentId, final int position) {
        final Dialog dialog = new Dialog(CommentsActivity.this);
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

        message.setText(getString(R.string.delete_comment));

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
                deleteComment(commentId, position);
                //new deleteComment().execute(commentId, position);
                dialog.dismiss();
            }
        });
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(CommentsActivity.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(CommentsActivity.this);
    }

    /**
     * Function for get the comments by product
     **/

    private void getComments() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_COMMENTS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        JSONObject result = json.optJSONObject(Constants.TAG_RESULT);
                        if (result != null) {
                            JSONArray commentsjson = result.optJSONArray(Constants.TAG_COMMENTS);
                            if (commentsjson != null) {
                                for (int k = 0; k < commentsjson.length(); k++) {
                                    JSONObject commentsTemp = commentsjson.getJSONObject(k);
                                    HashMap<String, String> tmpMap = new HashMap<String, String>();
                                    tmpMap.put(Constants.TAG_COMMENTID, DefensiveClass.optInt(commentsTemp, Constants.TAG_COMMENTID));
                                    tmpMap.put(Constants.TAG_COMMENT, DefensiveClass.optString(commentsTemp, Constants.TAG_COMMENT));
                                    tmpMap.put(Constants.TAG_USERID, DefensiveClass.optInt(commentsTemp, Constants.TAG_USERID));
                                    tmpMap.put(Constants.TAG_USERIMG, DefensiveClass.optString(commentsTemp, Constants.TAG_USERIMG));
                                    tmpMap.put(Constants.TAG_USERNAME, DefensiveClass.optString(commentsTemp, "user_name"));
                                    tmpMap.put(Constants.TAG_COMMENTTIME, DefensiveClass.optString(commentsTemp, Constants.TAG_COMMENTTIME));
                                    commentsList.add(tmpMap);
                                }
                            }
                        }
                    } else if (response.equalsIgnoreCase("error")) {
                        JoysaleApplication.disabledialog(CommentsActivity.this, json.optString(Constants.TAG_MESSAGE), GetSet.getUserId());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.INVISIBLE);
                commentsAdapter.notifyDataSetChanged();
                if (commentsList.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
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
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    public class CommentsAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> HomePageItems;
        ViewHolder holder = null;
        Context mContext;

        public CommentsAdapter(Context ctx,
                               ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            HomePageItems = data;
        }

        @Override
        public int getCount() {
            return HomePageItems.size();
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
                convertView = inflater.inflate(R.layout.comments_item, parent, false);// layout
                holder = new ViewHolder();

                holder.userImage = (ImageView) convertView.findViewById(R.id.userimg);
                holder.username = (TextView) convertView.findViewById(R.id.username);
                holder.comments = (TextView) convertView.findViewById(R.id.comments);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.options = (ImageView) convertView.findViewById(R.id.options);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                final HashMap<String, String> tempMap = HomePageItems.get(position);

                holder.username.setText(tempMap.get(Constants.TAG_USERNAME));
                holder.comments.setText(tempMap.get(Constants.TAG_COMMENT));

                if (tempMap.get(Constants.TAG_USERID).equals(GetSet.getUserId())) {
                    holder.options.setVisibility(View.VISIBLE);
                } else {
                    holder.options.setVisibility(View.GONE);
                }

                Picasso.with(CommentsActivity.this).load(tempMap.get(Constants.TAG_USERIMG)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);
                holder.userImage.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent u = new Intent(CommentsActivity.this, Profile.class);
                        u.putExtra(Constants.TAG_USER_ID, tempMap.get(Constants.TAG_USERID));
                        startActivity(u);
                    }
                });

                holder.username.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent u = new Intent(CommentsActivity.this, Profile.class);
                        u.putExtra(Constants.TAG_USER_ID, tempMap.get(Constants.TAG_USERID));
                        startActivity(u);
                    }
                });

                holder.options.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] values = new String[]{getString(R.string.delete)};

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                                R.layout.share_new, android.R.id.text1, values);
                        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = layoutInflater.inflate(R.layout.share, null);
                        if (JoysaleApplication.isRTL(mContext)) {
                            layout.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.grow_from_topleft_to_bottomright));
                        } else {
                            layout.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.grow_from_topright_to_bottomleft));
                        }
                        final PopupWindow popup = new PopupWindow(mContext);
                        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        popup.setContentView(layout);
                        popup.setWidth(display.getWidth() * 50 / 100);
                        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                        popup.setFocusable(true);
                        //popup.showAtLocation(v, Gravity.TOP|Gravity.LEFT,0,v.getHeight());

                        final ListView lv = (ListView) layout.findViewById(R.id.lv);
                        lv.setAdapter(adapter);
                        popup.showAsDropDown(view, -((display.getWidth() * 45 / 100)), -60);

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int pos, long id) {
                                switch (pos) {
                                    case 0:
                                        confirmDialog(tempMap.get(Constants.TAG_COMMENTID), position);
                                        popup.dismiss();
                                        break;
                                }
                            }
                        });
                    }
                });

                long timestamp = 0;
                String time = tempMap.get(Constants.TAG_COMMENTTIME);
                if (time.equals("ago")) {
                    holder.date.setText(getString(R.string.time_ago_seconds));
                } else {
                    if (time != null) {
                        timestamp = Long.parseLong(time) * 1000;
                        TimeAgo timeAgo = new TimeAgo(mContext);
                        holder.date.setText(timeAgo.timeAgo(timestamp));
                    }
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        public class ViewHolder {
            ImageView userImage, options;
            TextView username, date, comments;
        }
    }

    /**
     * Function for send comments to product
     **/

    private void sendComment(final String comment) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_POST_COMMENTS, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                try {
                    send.setOnClickListener(CommentsActivity.this);
                    JSONObject json = new JSONObject(result);
                    String response = json.getString(Constants.TAG_STATUS);
                    Log.v("comment", "status" + response);
                    if (response.equalsIgnoreCase("true")) {
                        HashMap<String, String> tempmap = new HashMap<String, String>();
                        //String commentTime = json.getString(Constants.TAG_COMMENTTIME);
                        tempmap.put(Constants.TAG_COMMENTID, json.getString(Constants.TAG_COMMENTID));
                        tempmap.put(Constants.TAG_COMMENT, json.getString(Constants.TAG_COMMENT));
                        tempmap.put(Constants.TAG_USERID, json.getString(Constants.TAG_USERID));
                        tempmap.put(Constants.TAG_USERIMG, json.getString(Constants.TAG_USERIMG));
                        tempmap.put(Constants.TAG_USERNAME, json.getString("user_name"));
                        tempmap.put(Constants.TAG_COMMENTTIME, "ago");

                        commentsList.add(tempmap);
                        commentText.setText("");
                        commentNotify("add");
                        commentsAdapter.notifyDataSetChanged();
                        nullLay.setVisibility(View.GONE);
                    } else {
                        JoysaleApplication.dialog(CommentsActivity.this, getResources().getString(R.string.alert), json.getString(Constants.TAG_MESSAGE));
                    }
                } catch (JSONException e) {
                    if (!comment.equals("")) {

                        HashMap<String, String> tempmap = new HashMap<String, String>();
                        tempmap.put(Constants.TAG_COMMENTID, "");
                        tempmap.put(Constants.TAG_COMMENT, comment);
                        tempmap.put(Constants.TAG_USERID, GetSet.getUserId());
                        tempmap.put(Constants.TAG_USERIMG, GetSet.getImageUrl());
                        tempmap.put(Constants.TAG_USERNAME, GetSet.getUserName());
                        tempmap.put(Constants.TAG_COMMENTTIME, "");

                        commentsList.add(tempmap);
                        commentText.setText("");
                        commentsAdapter.notifyDataSetChanged();
                        commentNotify("add");
                        nullLay.setVisibility(View.GONE);
                    }
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
                try {
                    map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                    map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                    map.put(Constants.TAG_COMMENT, comment);
                    map.put(Constants.TAG_USERID, GetSet.getUserId());
                    map.put(Constants.TAG_ITEM_ID, itemId);
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            commentText.setText("");
                            JoysaleApplication.dialog(CommentsActivity.this, getString(R.string.alert), getString(R.string.symbols_not_supported));
                        }
                    });
                }
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);

    }

    /**
     * Function for remove the comment
     **/
    private void deleteComment(final String commentId, final int position) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_DELETE_COMMENT, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        commentsList.remove(position);
                        commentsAdapter.notifyDataSetChanged();
                        if (commentsList.size() == 0) {
                            nullLay.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(CommentsActivity.this, DefensiveClass.optString(json, Constants.TAG_MESSAGE), Toast.LENGTH_LONG).show();
                        commentNotify("delete");
                    } else {
                        Toast.makeText(CommentsActivity.this, DefensiveClass.optString(json, Constants.TAG_MESSAGE), Toast.LENGTH_SHORT).show();
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
                map.put(Constants.TAG_COMMENTID, commentId);
                map.put(Constants.TAG_ITEM_ID, itemId);
                Log.i(TAG, "deleteCommentParams: "+map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                JoysaleApplication.hideSoftKeyboard(CommentsActivity.this);
                finish();
                break;
            case R.id.send:
                if (GetSet.isLogged()) {
                    if (commentText.getText().toString().trim().length() == 0) {
                        commentText.setError(getResources().getString(R.string.please_give_comments));
                    } else {
                        if (JoysaleApplication.isNetworkAvailable(CommentsActivity.this)) {
                            send.setOnClickListener(null);
                            JoysaleApplication.hideSoftKeyboard(CommentsActivity.this);
                            sendComment(commentText.getText().toString());
                        }
                    }
                } else {
                    Intent i = new Intent(CommentsActivity.this, WelcomeActivity.class);
                    startActivity(i);
                }
                break;
        }
    }
}
