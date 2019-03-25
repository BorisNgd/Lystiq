package com.app.lystiq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
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
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.app.utils.ItemsParsing;
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
 * This class is for Provide a List of Liked Products.
 */

public class LikedItems extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * Declare Layout Elements
     **/
    TextView userName;
    LinearLayout nullLay;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;

    public static RecyclerViewAdapter itemAdapter;
    public static ArrayList<HashMap<String, String>> likedItems = new ArrayList<HashMap<String, String>>();
    GridLayoutManager LayoutManager;
    ArrayList<String> likedId = new ArrayList<String>();

    /**
     * Declare Variables
     **/
    final String TAG = "LikedItems";
    static final String ARG_POSITION = "position";
    public static String userId = "";
    public static Context context;
    public static boolean flag = true;
    int screenWidth, screenHeight,previousTotal = 0,visibleThreshold = 5, mPosition,currentPage = 0,firstVisibleItem, visibleItemCount, totalItemCount;
    boolean loading = true, pulldown = false;

    public static LikedItems newInstance(int position, String userrId) {
        LikedItems fragment = new LikedItems();
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
        View v = inflater.inflate(R.layout.my_listing, container, false);

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
                    nullLay.setVisibility(View.GONE);
                    swipeRefresh();
                    getLikedItems(currentPage);
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

        context = getActivity();
        likedItems.clear();

        //To get Liked Id from Api
        getLikedId();

        //To get Liked Items from Api
        loadData();

    }

    //To initialize the adapter
    private void setAdapter() {
        recyclerView.setHasFixedSize(true);
        LayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(LayoutManager);

        itemAdapter = new RecyclerViewAdapter(likedItems);
        recyclerView.setAdapter(itemAdapter);
    }

    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    private void loadData() {
        try {
            if (likedItems.size() == 0) {
                try {
                    if (JoysaleApplication.isNetworkAvailable(context)) {
                        if (flag) {
                            nullLay.setVisibility(View.GONE);
                            swipeRefresh();
                            getLikedItems(0);
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

    @Override
    public void onRefresh() {
        if (!pulldown) {
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
            getLikedId();
            nullLay.setVisibility(View.GONE);
            swipeRefresh();
            getLikedItems(0);
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Function for get the list of liked items
     **/
    private void getLikedItems(final int pageCount) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_HOME, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                Log.v(TAG,"likedItemsResp=" + json);
                if (pulldown) {
                    likedItems.clear();
                }
                ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                ItemsParsing parse = new ItemsParsing("liked", context, userId);
                temp.addAll(parse.parsing(json));
                if (!likedItems.contains(temp)) {
                    likedItems.addAll(temp);
                }
                if (pulldown) {
                    pulldown = false;
                    loading = true;
                }
                recyclerView.setVisibility(View.VISIBLE);
                swipeLayout.setRefreshing(false);
                itemAdapter.notifyDataSetChanged();
                if (likedItems.size() == 0) {
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
                map.put(Constants.TAG_TYPE, "liked");
                map.put(Constants.TAG_USERID, userId);
                map.put(Constants.TAG_OFFSET, Integer.toString(offset));
                map.put(Constants.TAG_LIMIT, "20");
                map.put(Constants.LANG_TYPE, AppUtils.getCurrentLanguageCode(context));
                Log.v(TAG,"likedItemsParams="+map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for the liked item ids
     **/
    private void getLikedId() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_GET_LIKED_ID, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    Log.v(TAG, "likedIdRes=" + res);
                    String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        JSONArray result = json.optJSONArray("result");
                        likedId.clear();
                        for (int i = 0; i < result.length(); i++) {
                            likedId.add(result.getString(i));
                        }
                        Log.v(TAG, "likedId=" + likedId);
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
                Log.v(TAG, "likedIdParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Adapter for Liked Product Adapter
    * */
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> likedItemsList;

        public RecyclerViewAdapter(ArrayList<HashMap<String, String>> likedItemsList) {
            this.likedItemsList = likedItemsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mylisting_list_items, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = likedItemsList.get(position);
            Picasso.with(context).load(tempMap.get(Constants.TAG_ITEM_URL_350)).into(holder.singleImage);
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
        }

        @Override
        public int getItemCount() {
            return likedItemsList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView singleImage;
            TextView productType;

            public MyViewHolder(View view) {
                super(view);
                singleImage = (ImageView) view.findViewById(R.id.singleImage);
                productType = (TextView) view.findViewById(R.id.productType);

                singleImage.getLayoutParams().width = screenWidth;
                /*Rectangle Image*/
//                singleImage.getLayoutParams().height = screenHeight;
                /*Square Image*/
                singleImage.getLayoutParams().height = screenWidth - JoysaleApplication.dpToPx(context,15);

                singleImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.singleImage:
//                        if (likedId.contains(likedItems.get(getAdapterPosition()).get(Constants.TAG_ID))) {
//                            likedItems.get(getAdapterPosition()).put(Constants.TAG_LIKED, "yes");
//                        } else {
//                            likedItems.get(getAdapterPosition()).put(Constants.TAG_LIKED, "no");
//                        }

                        Intent i = new Intent(context,
                                DetailActivity.class);
                        i.putExtra(Constants.DATA, likedItems.get(getAdapterPosition()));
                        i.putExtra(Constants.POSITION, getAdapterPosition());
                        i.putExtra(Constants.FROM, "liked");
                        startActivity(i);
                        break;
                }
            }
        }
    }
}
