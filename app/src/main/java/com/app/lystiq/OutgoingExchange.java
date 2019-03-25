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
import android.widget.AdapterView;
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
 * This class is for Provide a List of Outgoing Exchanges.
 */
public class OutgoingExchange extends Fragment implements FragmentChangeListener, SwipeRefreshLayout.OnRefreshListener {
    /**
     * Declare Layout Elements
     **/
    ListView mListView;
    LinearLayout nullLay;
    AVLoadingIndicatorView progress;
    SwipeRefreshLayout swipeLayout;

    public static ExchangeAdapter exchangeAdapter;
    public static ArrayList<HashMap<String, String>> outgoingAry = new ArrayList<HashMap<String, String>>();

    /**
     * Declare Variables
     **/
    String TAG = "OutgoingExchange";
    public static String type = "outgoing";
    public static Context context;
    boolean pulldown = false, loadmore = false;

    public OutgoingExchange() {
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

        outgoingAry.clear();
        initializeUI();
        getOutgoingExchanges();

        // To initialize the Adapter class
        exchangeAdapter = new ExchangeAdapter(getActivity(), outgoingAry);
        mListView.setAdapter(exchangeAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (outgoingAry.size() > 0) {
                    Intent i = new Intent(getActivity(), ExchangeView.class);
                    i.putExtra(Constants.DATA, outgoingAry.get(position));
                    i.putExtra(Constants.POSITION, position);
                    i.putExtra(Constants.TAG_TYPE, outgoingAry.get(position).get(Constants.TAG_TYPE));
                    startActivity(i);
                }
            }
        });

    }

    /**
     * Function for get the outgoing exchanges
     **/
    private void getOutgoingExchanges() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_MY_EXCHANGE, new Response.Listener<String>() {
            @Override
            public void onResponse(final String json) {
                if (pulldown) {
                    outgoingAry.clear();
                }
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<HashMap<String, String>> temp = new ArrayList<HashMap<String, String>>();
                        ExchangeParsing parse = new ExchangeParsing(context);
                        temp.addAll(parse.parsing(json));
                        if (!outgoingAry.contains(temp)) {
                            outgoingAry.addAll(temp);
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

                if (type.equals("outgoing")) {
                    if (outgoingAry.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.INVISIBLE);
                    }
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
                map.put(Constants.TAG_TYPE, "outgoing");
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
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
    public void onCentered() {

    }

    @Override
    public void onRefresh() {
        if (!pulldown && loadmore) {
            pulldown = true;
            initializeUI();
            getOutgoingExchanges();
        } else {
            swipeLayout.setRefreshing(false);
        }
    }

    private void initializeUI() {
        loadmore = false;
        nullLay.setVisibility(View.INVISIBLE);

        if (pulldown) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
        } else if (outgoingAry.size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            swipeRefresh();
        } else {
            mListView.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Adapter for Outgoing Exchanges
     **/

    public class ExchangeAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> outgoingExchangeList;
        ViewHolder holder = null;
        Context mContext;

        public ExchangeAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            outgoingExchangeList = data;
        }

        @Override
        public int getCount() {
            return outgoingExchangeList.size();
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
                holder.status2 = (TextView) convertView.findViewById(R.id.status2);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.userImage = (ImageView) convertView.findViewById(R.id.userImage);
                holder.userName = (TextView) convertView.findViewById(R.id.userName);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                final HashMap<String, String> tempMap = outgoingExchangeList.get(position);
                if (tempMap.get(Constants.TAG_TYPE).equals("success") || tempMap.get(Constants.TAG_TYPE).equals("failed")) {
                    holder.view.setVisibility(View.GONE);
                } else {
                    holder.view.setVisibility(View.VISIBLE);
                }

                Picasso.with(getActivity()).load(tempMap.get("e" + Constants.TAG_ITEMIMAGE)).into(holder.exitemImage);
                Picasso.with(getActivity()).load(tempMap.get("m" + Constants.TAG_ITEMIMAGE)).into(holder.myitemImage);
                Picasso.with(getActivity()).load(tempMap.get(Constants.TAG_EXCHANGERIMG)).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(holder.userImage);

                holder.myitemName.setText(tempMap.get("m" + Constants.TAG_ITEM_NAME));
                holder.exitemName.setText(tempMap.get("e" + Constants.TAG_ITEM_NAME));
                holder.userName.setText(tempMap.get(Constants.TAG_EXCHANGERNAME));
                holder.time.setText(tempMap.get(Constants.TAG_EXCHANGETIME));

                if (tempMap.get(Constants.TAG_STATUS).equals("Pending")) {
                    holder.status2.setVisibility(View.VISIBLE);
                    holder.status.setVisibility(View.GONE);
                    holder.status2.setText(getString(R.string.pending));
                } else {
                    holder.status.setVisibility(View.VISIBLE);
                    holder.status2.setVisibility(View.GONE);
                    holder.status.setText(getString(R.string.accepted));
                }

                holder.view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), ExchangeView.class);
                        i.putExtra(Constants.DATA, outgoingExchangeList.get(position));
                        i.putExtra(Constants.POSITION, position);
                        i.putExtra(Constants.TAG_TYPE, tempMap.get(Constants.TAG_TYPE));
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
            TextView exitemName, myitemName, view, status, status2, time, userName;
        }
    }
}



