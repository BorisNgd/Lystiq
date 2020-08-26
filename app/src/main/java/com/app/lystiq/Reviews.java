package com.app.lystiq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.RatingBar;
import android.widget.TextView;

import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Reviews#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Reviews extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    TextView userName;
    LinearLayout nullLay;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeLayout;

    public static RecyclerViewAdapter itemAdapter;
    ArrayList<HashMap<String , String>> reviews = new ArrayList<>();
    LinearLayoutManager LayoutManager;

    final String TAG = "Reviews";
    static final String ARG_POSITION = "position";
    static final String userId = "";
    public static Context context;
    public static boolean flag = true;
    int screenWidth, screenHeight, mPosition, padding,currentPage = 0,previousTotal = 0,visibleThreshold = 5,firstVisibleItem, visibleItemCount, totalItemCount;
    private boolean loading = true, pulldown = false;

    private void initializeReviewUI(){
        nullLay.setVisibility(View.GONE);
        swipeRefresh();
    }


    public Reviews() {
        // Required empty public constructor
    }


    public static Reviews newInstance(int position, String userId) {
        Reviews fragment = new Reviews();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        userId = userId;
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       mPosition = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.reviews, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        nullLay = (LinearLayout) view.findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeLayout);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nullLay.setVisibility(View.GONE);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = LayoutManager.findFirstCompletelyVisibleItemPosition();

                if (loading){
                    if (totalItemCount > previousTotal){
                        loading = false;
                        previousTotal = totalItemCount;
                        currentPage++;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)){
                    initializeReviewUI();

                    //Todo get reviews
                    getReviews(currentPage);
                    loading = true;
                }
            }
        });

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        screenWidth = width / 2;
        screenWidth = width * 35 / 100;

        padding = JoysaleApplication.dpToPx(getActivity() , 10);
        context = getActivity();

        //Todo  load data
        loadData();
    }

    private void setAdapter(){
        recyclerView.setHasFixedSize(true);
        LayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(LayoutManager);

        itemAdapter = new RecyclerViewAdapter(reviews);
        recyclerView.setAdapter(itemAdapter);
    }

    private void swipeRefresh(){
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    private void loadData(){
        setAdapter();
        try {
            if (reviews.size() == 0){
                try {
                    if (JoysaleApplication.isNetworkAvailable(context)) {

                        if (flag){
                            initializeReviewUI();
                            //Todo get reviews
                            getReviews(0);
                            flag = false;
                        }
                    }
                    setAdapter();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else {
                setAdapter();
                nullLay.setVisibility(View.GONE);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //To parse from jsonObject to arrayList
    private ArrayList<HashMap<String, String>> parsing(String json) {
        ArrayList<HashMap<String, String>> reviews = new ArrayList<HashMap<String, String>>();
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

                    reviews.add(map);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return reviews;
    }

    @Override
    public void onRefresh() {
        if (!pulldown){
            currentPage = 0;
            previousTotal = 0;
            pulldown = true;
            initializeReviewUI();
            getReviews(0);
        }else {
            swipeLayout.setRefreshing(false);
        }

    }

    private void getReviews(final int pageCount){

        if (pulldown){
            reviews.clear();
        }

        HashMap<String , String> object  = new HashMap<>();
        object.put("full_name" , "Lystiq1");
        object.put("user_image" , "Lystiq1");
        object.put("review_comment" , "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolorefff");
        object.put("review_date" , "13/08/2020");
        object.put("rating" , "4");

        HashMap<String , String> object2  = new HashMap<>();
        object2.put("full_name" , "Lystiq2");
        object2.put("user_image" , "Lystiq2");
        object2.put("review_comment" , "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum doloreddddw");
        object2.put("review_date" , "11/08/2020");
        object2.put("rating" , "2");

        HashMap<String , String> object3  = new HashMap<>();
        object3.put("full_name" , "Lystiq3");
        object3.put("user_image" , "Lystiq3");
        object3.put("review_comment" , "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum doloreefefefrgr");
        object3.put("review_date" , "15/08/2020");
        object3.put("rating" , "3");

        reviews.add(object);
        reviews.add(object2);
        reviews.add(object3);

        if (pulldown){
            pulldown = false;
            loading = true;
        }
        recyclerView.setVisibility(View.VISIBLE);
        swipeLayout.setRefreshing(false);
        itemAdapter.notifyDataSetChanged();
        if (reviews.size() == 0){
            nullLay.setVisibility(View.VISIBLE);
        }else {
            Log.d(TAG, "getReviews: "+reviews);
            nullLay.setVisibility(View.GONE);
        }
        flag = true;
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<Reviews.RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> reviewsList;

        public RecyclerViewAdapter(ArrayList<HashMap<String, String>> reviewsList) {
            this.reviewsList = reviewsList;
        }

        @Override
        public Reviews.RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.reviews_list, parent, false);

            return new Reviews.RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(Reviews.RecyclerViewAdapter.MyViewHolder holder, int position) {
            final HashMap<String, String> tempMap = reviewsList.get(position);

            Picasso.with(context).load(tempMap.get(Constants.TAG_USERIMAGE)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);
            holder.userName.setText(tempMap.get(Constants.TAG_FULL_NAME));
            holder.reviewDate.setText(tempMap.get("review_date"));
            holder.reviewComment.setText(tempMap.get("review_comment"));

            try {
                holder.ratingBar.setRating(Float.parseFloat(tempMap.get("rating")));
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
            return reviewsList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView userImage;
            RatingBar ratingBar;
            TextView userName, reviewDate , reviewComment;

            public MyViewHolder(View view) {
                super(view);
                userImage = (ImageView) view.findViewById(R.id.userImage);
                ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
                userName = (TextView) view.findViewById(R.id.userName);
                reviewDate = (TextView) view.findViewById(R.id.review_date);
                reviewComment = (TextView) view.findViewById(R.id.review_comment);

                userImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {

            }
        }
    }
}