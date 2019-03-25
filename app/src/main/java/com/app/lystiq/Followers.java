package com.app.lystiq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
 * Created by hitasoft on 29/6/16.
 * <p>
 * This class is for Display a List of Followers
 */

public class Followers extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * Declare Layout Elements
     **/
    TextView userName;
    LinearLayout nullLay;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;

    public static RecyclerViewAdapter itemAdapter;
    ArrayList<HashMap<String, String>> followers = new ArrayList<HashMap<String, String>>();
    LinearLayoutManager LayoutManager;

    /**
     * Declare Variables
     **/
    final String TAG = "Followers";
    private static final String ARG_POSITION = "position";
    public static String userId = "";
    public static Context context;
    public static boolean flag = true;
    int screenWidth, screenHeight, mPosition, padding,currentPage = 0,firstVisibleItem, visibleItemCount, totalItemCount,previousTotal = 0,visibleThreshold = 5;
    private boolean loading = true, pulldown = false;

    // when scroll recyclerview, Initialize the Swipe Layout
    private void initializeFollowersUI() {
        nullLay.setVisibility(View.GONE);
        swipeRefresh();
    }

    // To create Instance of Followers fragment
    public static Followers newInstance(int position, String userrId) {
        Followers fragment = new Followers();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        userId = userrId;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.followers, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        nullLay = (LinearLayout) v.findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nullLay.setVisibility(View.GONE);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                // code
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = LayoutManager.getItemCount();
                firstVisibleItem = LayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                        currentPage++;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached
                    initializeFollowersUI();
                    getFollowers(currentPage);
                    loading = true;
                }
            }
        });
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        screenWidth = width / 2;
        screenHeight = width * 35 / 100;

        padding = JoysaleApplication.dpToPx(getActivity(), 10);
        context = getActivity();

        loadData();

    }

    /**
     * for set adapter to recycler view
     **/
    private void setAdapter() {
        recyclerView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(LayoutManager);

        // Initialize Adapter class
        itemAdapter = new RecyclerViewAdapter(followers);
        recyclerView.setAdapter(itemAdapter);
    }

    //To refresh swipelayout
    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    //To get Followersdetails
    private void loadData() {
        try {
            if (followers.size() == 0) {
                try {
                    if (JoysaleApplication.isNetworkAvailable(context)) {
                        if (flag) {
                            initializeFollowersUI();
                            getFollowers(0);
                            flag = false;
                        }
                    }
                    setAdapter();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                setAdapter();
                nullLay.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<HashMap<String, String>> parsing(String json) {
        ArrayList<HashMap<String, String>> followers = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject jobj = new JSONObject(json);

            JSONArray result = jobj.getJSONArray(Constants.TAG_RESULT);

            if (DefensiveClass.optString(jobj, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                for (int i = 0; i < result.length(); i++) {
                    JSONObject temp = result.getJSONObject(i);
                    HashMap<String, String> map = new HashMap<String, String>();

                    map.put(Constants.TAG_USERID, DefensiveClass.optString(temp, Constants.TAG_USERID));
                    map.put(Constants.TAG_USERNAME, DefensiveClass.optString(temp, Constants.TAG_USERNAME));
                    map.put(Constants.TAG_FULL_NAME, DefensiveClass.optString(temp, Constants.TAG_FULL_NAME));
                    map.put(Constants.TAG_STATUS, DefensiveClass.optString(temp, Constants.TAG_STATUS));
                    map.put(Constants.TAG_USERIMAGE, DefensiveClass.optString(temp, Constants.TAG_USERIMAGE));

                    followers.add(map);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return followers;
    }

    @Override
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
            getFollowingId();
            initializeFollowersUI();
            getFollowers(0);
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Function for get followers list of user
     **/

    private void getFollowers(final int pageCount) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_FOLLOWERS, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {

                Log.v(TAG,"getfollowersresponse="+json);

                if (pulldown) {
                    followers.clear();
                }

                ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                temp.addAll(parsing(json));

                if (!followers.contains(temp)) {
                    followers.addAll(temp);
                }

                Log.v(TAG, "followers" + followers);
                if (pulldown) {
                    pulldown = false;
                    loading = true;
                }

                recyclerView.setVisibility(View.VISIBLE);
                swipeLayout.setRefreshing(false);
                itemAdapter.notifyDataSetChanged();

                if (followers.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    nullLay.setVisibility(View.GONE);
                }
                flag = true;

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
                map.put(Constants.TAG_USERID, userId);
                map.put(Constants.TAG_OFFSET, Integer.toString(offset));
                map.put(Constants.TAG_LIMIT, "20");
                Log.v(TAG,"getfollowersparams="+map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for getting following userid's
     **/

    private void getFollowingId() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_FOLLOWER_ID, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        JSONArray result = json.optJSONArray("result");
                        Profile.followingId.clear();
                        for (int i = 0; i < result.length(); i++) {
                            Profile.followingId.add(result.getString(i));
                        }
                    }
                    Log.v("followingId", "followingId=" + Profile.followingId);
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
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Adapter for Followers
     **/

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> followersList;

        public RecyclerViewAdapter(ArrayList<HashMap<String, String>> followersList) {
            this.followersList = followersList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.followers_list, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final HashMap<String, String> tempMap = followersList.get(position);

            Picasso.with(context).load(tempMap.get(Constants.TAG_USERIMAGE)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);
            holder.userName.setText(tempMap.get(Constants.TAG_FULL_NAME));
            holder.location.setText(tempMap.get(Constants.TAG_USERNAME));

            if (tempMap.get(Constants.TAG_USERID).equals(GetSet.getUserId())) {
                holder.followStatus.setVisibility(View.GONE);
            } else {
                holder.followStatus.setVisibility(View.VISIBLE);
                if (Profile.followingId.contains(tempMap.get(Constants.TAG_USERID))) {
                    holder.followStatus.setImageResource(R.drawable.unfollow);
                    holder.followStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.unfollow_bg));
                } else {
                    holder.followStatus.setImageResource(R.drawable.follow);
                    holder.followStatus.setBackgroundDrawable(getResources().getDrawable(R.drawable.follow_bg));
                }
            }

            holder.followStatus.setPadding(padding, padding, padding, padding);
        }

        @Override
        public int getItemCount() {
            return followersList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView userImage, followStatus;
            TextView userName, location;

            public MyViewHolder(View view) {
                super(view);
                userImage = (ImageView) view.findViewById(R.id.userImage);
                followStatus = (ImageView) view.findViewById(R.id.followStatus);
                userName = (TextView) view.findViewById(R.id.userName);
                location = (TextView) view.findViewById(R.id.location);

                followStatus.setOnClickListener(this);
                userImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.userImage:
                        Intent u = new Intent(context, Profile.class);
                        u.putExtra(Constants.TAG_USER_ID, followersList.get(getAdapterPosition()).get(Constants.TAG_USERID));
                        startActivity(u);
                        break;
                    case R.id.followStatus:
                        if (GetSet.isLogged()) {
                            String userId = followersList.get(getAdapterPosition()).get(Constants.TAG_USERID);
                            if (Profile.followingId.contains(userId)) {
                                Profile.followingId.remove(userId);
                                notifyDataSetChanged();
                                unFollowUser(userId);
                            } else {
                                Profile.followingId.add(userId);
                                notifyDataSetChanged();
                                followUser(userId);
                            }
                        } else {
                            Intent k = new Intent(context, WelcomeActivity.class);
                            startActivity(k);
                        }
                        break;
                }
            }
        }
    }

    /**
     * Function for follow the user
     **/

    private void followUser(final String followUserId) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_FOLLOW, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jobj = new JSONObject(response);
                    if (DefensiveClass.optString(jobj, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        if (!Profile.followingId.contains(userId))
                            Profile.followingId.add(userId);
                        itemAdapter.notifyDataSetChanged();
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
                map.put(Constants.TAG_FOLLOW_ID, followUserId);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for unfollow the user
     **/

    private void unFollowUser(final String unFollowUserId) {
        StringRequest req = new StringRequest(Request.Method.POST,Constants.API_UNFOLLOW, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jobj = new JSONObject(response);
                    Log.v(TAG,"unfollowresponse="+response);
                    if (DefensiveClass.optString(jobj, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        if (Profile.followingId.contains(userId))
                            Profile.followingId.remove(userId);
                        itemAdapter.notifyDataSetChanged();
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
                map.put(Constants.TAG_FOLLOW_ID, unFollowUserId);
                Log.v(TAG,"unfollowparams="+map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

}

