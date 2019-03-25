package com.app.lystiq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.external.FragmentChangeListener;
import com.app.utils.Constants;
import com.app.utils.ExchangeParsing;
import com.app.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 13/6/16.
 * <p>
 * This class is for Provide a List of Success Exchanges.
 */

public class SuccessExchange extends Fragment implements FragmentChangeListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * Declare Layout Elements
     **/
    ListView mListView;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    SwipeRefreshLayout swipeLayout;

    /**
     * Declare variables
     **/
    static final String TAG = "SuccessExchange";
    boolean pulldown = false, loadmore = false;

    public static ExchangeAdapter exchangeAdapter;
    public static ArrayList<HashMap<String, String>> successAry = new ArrayList<HashMap<String, String>>();
    public static Context context;


    public SuccessExchange() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.exchangefragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView = (ListView) getView().findViewById(R.id.listView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        nullLay = (LinearLayout) getView().findViewById(R.id.nullLay);
        swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeLayout);

        context = getActivity();

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.progressColor));
        swipeLayout.setOnRefreshListener(this);

        loadmore = false;

        nullLay.setVisibility(View.INVISIBLE);

        setUI(pulldown);

        successAry.clear();

        //To get myexchanges data from Api
        getSuccessExchanges();

        //To initialize and set Adapter
        exchangeAdapter = new ExchangeAdapter(getActivity(), successAry);
        mListView.setAdapter(exchangeAdapter);

    }

    private void setUI(boolean pulldown) {
        if (pulldown) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else if (successAry.size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            swipeRefresh();
        } else {
            mListView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Function for showing successful exchanges by user
     **/

    private void getSuccessExchanges() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_MY_EXCHANGE, new Response.Listener<String>() {
            @Override
            public void onResponse(final String json) {
                if (pulldown) {
                    successAry.clear();
                }
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                        ExchangeParsing parse = new ExchangeParsing(context);
                        temp.addAll(parse.parsing(json));
                        if (!successAry.contains(temp)) {
                            successAry.addAll(temp);
                        }
                    }
                });

                progress.setVisibility(View.GONE);
                if (pulldown) {
                    pulldown = false;
                }

                swipeLayout.setRefreshing(false);
                mListView.setVisibility(View.VISIBLE);

                exchangeAdapter.notifyDataSetChanged();

                if (successAry.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    nullLay.setVisibility(View.INVISIBLE);
                }

                loadmore = true;
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
                map.put(Constants.TAG_TYPE, "success");
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    public void onCentered() {

    }

    private void swipeRefresh() {
        swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });
    }

    @Override
    public void onRefresh() {
        if (!pulldown && loadmore) {
            pulldown = true;
            loadmore = false;
            nullLay.setVisibility(View.INVISIBLE);

            setUI(pulldown);

            getSuccessExchanges();
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    /**
     * Adapter for Success Exchange
     **/

    public class ExchangeAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> successExchangeList;
        ViewHolder holder = null;
        Context mContext;

        public ExchangeAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            successExchangeList = data;
        }

        @Override
        public int getCount() {
            return successExchangeList.size();
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
                convertView = inflater.inflate(R.layout.exchange_list_item, parent, false);//layout
                holder = new ViewHolder();

                holder.myitemImage = (ImageView) convertView.findViewById(R.id.myitemImage);
                holder.exitemImage = (ImageView) convertView.findViewById(R.id.exitemImage);
                holder.exitemName = (TextView) convertView.findViewById(R.id.exitemName);
                holder.myitemName = (TextView) convertView.findViewById(R.id.myitemName);
                holder.view = (TextView) convertView.findViewById(R.id.view);
                holder.status = (TextView) convertView.findViewById(R.id.status);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.userImage = (ImageView) convertView.findViewById(R.id.userImage);
                holder.userName = (TextView) convertView.findViewById(R.id.userName);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                if (successExchangeList.get(position).get(Constants.TAG_TYPE).equals("success") || successExchangeList.get(position).get(Constants.TAG_TYPE).equals("failed")) {
                    holder.view.setVisibility(View.GONE);
                } else {
                    holder.view.setVisibility(View.VISIBLE);
                }

                Picasso.with(getActivity()).load(successExchangeList.get(position).get("e" + Constants.TAG_ITEMIMAGE)).into(holder.exitemImage);
                Picasso.with(getActivity()).load(successExchangeList.get(position).get("m" + Constants.TAG_ITEMIMAGE)).into(holder.myitemImage);
                Picasso.with(getActivity()).load(successExchangeList.get(position).get(Constants.TAG_EXCHANGERIMG)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);

                holder.myitemName.setText(successExchangeList.get(position).get("m" + Constants.TAG_ITEM_NAME));
                holder.exitemName.setText(successExchangeList.get(position).get("e" + Constants.TAG_ITEM_NAME));
                holder.userName.setText(successExchangeList.get(position).get(Constants.TAG_EXCHANGERNAME));
                holder.time.setText(successExchangeList.get(position).get(Constants.TAG_EXCHANGETIME));
                holder.status.setText(getString(R.string.success));

                holder.status.setVisibility(View.VISIBLE);

                holder.view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), ExchangeView.class);
                        i.putExtra(Constants.DATA, successExchangeList.get(position));
                        i.putExtra(Constants.POSITION, position);
                        i.putExtra(Constants.TAG_TYPE, successExchangeList.get(position).get(Constants.TAG_TYPE));
                        startActivity(i);
                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            ImageView myitemImage, exitemImage, userImage;
            TextView exitemName, myitemName, view, status, time, userName;
        }
    }

}



