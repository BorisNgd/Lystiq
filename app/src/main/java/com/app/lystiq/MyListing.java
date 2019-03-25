package com.app.lystiq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.GetSet;
import com.app.utils.ItemsParsing;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 6/6/16.
 * <p>
 * This class is for Provide a List of Added Product.
 */

public class MyListing extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    /**
     * Declare Layout Elements
     **/
    TextView userName;
    LinearLayout nullLay;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;

    public static RecyclerViewAdapter itemAdapter;
    public static ArrayList<HashMap<String, String>> addedItems = new ArrayList<HashMap<String, String>>();
    public static Context context;
    GridLayoutManager LayoutManager;

    /**
     * Declare Variables
     **/
    final String TAG = "MyListing";
    static final String ARG_POSITION = "position";
    public static String userId = "";
    public boolean flag = true;
    int screenWidth, screenHeight, previousTotal = 0, visibleThreshold = 5, mPosition, currentPage = 0, firstVisibleItem, visibleItemCount, totalItemCount;
    boolean loading = true, pulldown = false;
    private boolean isTouched = false;
    int width, height;

    RecyclerView.OnItemTouchListener touchListener = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            isTouched = true;
            Log.e(TAG, "onInterceptTouchEvent: " + isTouched);
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    };

    //To create an instance of MyListing fragment
    public static MyListing newInstance(int position, String userrId) {
        MyListing fragment = new MyListing();
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

    private void initializeUI() {
        nullLay.setVisibility(View.GONE);
        swipeRefresh();
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

//        recyclerView.addOnItemTouchListener(touchListener);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                // code
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isTouched = true;
                    Log.e(TAG, "onScrollStateChanged: " + isTouched);

                }
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                if (isTouched) {
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
                        initializeUI();
                        getAddedItems(currentPage);
                        loading = true;
                    }
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
        addedItems.clear();
        isTouched = false;

        // getItems from Api
        loadData();

    }

    //To initialize the adapter class
    private void setAdapter() {
        recyclerView.setHasFixedSize(true);
        LayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(LayoutManager);

        itemAdapter = new RecyclerViewAdapter(addedItems);
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
            if (addedItems.size() == 0) {
                try {
                    if (JoysaleApplication.isNetworkAvailable(context)) {
                        if (flag) {
                            initializeUI();
                            getAddedItems(0);
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
            initializeUI();
            getAddedItems(0);
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Function for get the list of items which is added by the user
     **/

    private void getAddedItems(final int pageCount) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_HOME, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                Log.v(TAG, "getAddedItemsRes" + json);
                if (pulldown) {
                    addedItems.clear();
                }
                ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                ItemsParsing parse = new ItemsParsing(context, userId);
                temp.addAll(parse.parsing(json));
                if (!addedItems.contains(temp)) {
                    addedItems.addAll(temp);
                }
                Log.v(TAG, "AddedItems" + addedItems);
                if (pulldown) {
                    pulldown = false;
                    loading = true;
                }
                recyclerView.setVisibility(View.VISIBLE);
                swipeLayout.setRefreshing(false);
                itemAdapter.notifyDataSetChanged();
                if (addedItems.size() == 0) {
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
                int offset = pageCount * 20;
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_TYPE, "moreitems");
                map.put(Constants.TAG_SELLERID, userId);
                map.put(Constants.TAG_USERID, GetSet.getUserId() != null ? GetSet.getUserId() : "");
                map.put(Constants.TAG_OFFSET, Integer.toString(offset));
                map.put(Constants.TAG_LIMIT, "20");
                map.put(Constants.LANG_TYPE, AppUtils.getCurrentLanguageCode(context));
                Log.v(TAG, "getAddedItemsParams=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Adapter for List
     **/

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> Items;

        public RecyclerViewAdapter(ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mylisting_list_items, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            HashMap<String, String> tempMap = Items.get(position);
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
            return Items.size();
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
                        Intent i = new Intent(context,
                                DetailActivity.class);
                        i.putExtra(Constants.DATA, Items.get(getAdapterPosition()));
                        i.putExtra(Constants.POSITION, getAdapterPosition());
                        i.putExtra(Constants.FROM, "mylisting");
                        startActivity(i);
                        break;
                }
            }
        }
    }
}
