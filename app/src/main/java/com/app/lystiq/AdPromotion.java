package com.app.lystiq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
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
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



/**
 * Created by hitasoft on 8/7/16.
 */
public class AdPromotion extends Fragment {

    // widget Declaration
    ListView mListView;
    AVLoadingIndicatorView progress;
    LinearLayout nullLay;

    AdAdapter adapter;
    ArrayList<HashMap<String, String>> adAry = new ArrayList<HashMap<String, String>>();

    private static final String TAG ="AdPromotion" ;

    public AdPromotion() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.urgent, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView = (ListView) getView().findViewById(R.id.listView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        nullLay = (LinearLayout) getView().findViewById(R.id.nullLay);

        nullLay.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        nullLay.setVisibility(View.GONE);

        getAdList();

        adapter = new AdAdapter(getActivity(), adAry);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent j = new Intent(getActivity(), PromotionDetail.class);
                j.putExtra("data", adAry.get(position));
                startActivity(j);
            }
        });

    }

    /**
     * Method for getting admin defined advertisement
     **/
    private void getAdList(){
        StringRequest req = new StringRequest(Request.Method.POST,Constants.API_MY_PROMOTIONS, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    JSONObject jobj = new JSONObject(res);
                    Log.v(TAG,"addpromotejson="+res);
                    String response = jobj.getString(Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        JSONArray result = jobj.optJSONArray(Constants.TAG_RESULT);
                        for (int i = 0; i < result.length(); i++) {
                            HashMap<String, String> map = new HashMap<String, String>();
                            JSONObject temp = result.getJSONObject(i);
                            map.put(Constants.TAG_ID, DefensiveClass.optString(temp, Constants.TAG_ID));
                            map.put(Constants.TAG_PROMOTION_NAME, DefensiveClass.optString(temp, Constants.TAG_PROMOTION_NAME));
                            map.put(Constants.TAG_PAID_AMOUNT, DefensiveClass.optString(temp, Constants.TAG_PAID_AMOUNT));
                            map.put(Constants.TAG_CURRENCY_SYM, DefensiveClass.optString(temp, Constants.TAG_CURRENCY_SYM));
                            map.put(Constants.TAG_CURRENCY_CODE, DefensiveClass.optString(temp, Constants.TAG_CURRENCY_CODE));
                            map.put(Constants.TAG_UPTO, DefensiveClass.optString(temp, Constants.TAG_UPTO));
                            map.put(Constants.TAG_TRANSACTION_ID, DefensiveClass.optString(temp, Constants.TAG_TRANSACTION_ID));
                            map.put(Constants.TAG_STATUS, DefensiveClass.optString(temp, Constants.TAG_STATUS));
                            map.put(Constants.TAG_ITEM_ID, DefensiveClass.optString(temp, Constants.TAG_ITEM_ID));
                            map.put(Constants.TAG_ITEM_NAME, DefensiveClass.optString(temp, Constants.TAG_ITEM_NAME));
                            map.put(Constants.TAG_ITEM_IMAGE, DefensiveClass.optString(temp, Constants.TAG_ITEM_IMAGE));
                            map.put(Constants.TAG_ITEM_APPROVE, DefensiveClass.optString(temp, Constants.TAG_ITEM_APPROVE));
                            adAry.add(map);
                        }
                    }
                    Log.v("adAry", "adAry==" + adAry);
                    progress.setVisibility(View.GONE);
                    if (adAry.size() > 0) {
                        adapter.notifyDataSetChanged();
                        nullLay.setVisibility(View.GONE);
                    } else {
                        nullLay.setVisibility(View.VISIBLE);
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
                map.put(Constants.TAG_TYPE, "ad");
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);

   }


    public class AdAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> Items;
        ViewHolder holder = null;
        private Context mContext;

        public AdAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
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

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.urgentlist_item, parent, false);//layout
                holder = new ViewHolder();

                holder.itemtitle = (TextView) convertView.findViewById(R.id.itemtitle);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                holder.view = (ImageView) convertView.findViewById(R.id.lnext);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {

                if (JoysaleApplication.isRTL(mContext)) {
                    holder.view.setRotation(180);
                    holder.itemtitle.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                } else {
                    holder.view.setRotation(0);
                    holder.itemtitle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                }

                final HashMap<String, String> tempMap = Items.get(position);

                holder.itemtitle.setText(tempMap.get(Constants.TAG_ITEM_NAME));

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent j = new Intent(getActivity(), PromotionDetail.class);
                        j.putExtra("data", tempMap);
                        startActivity(j);
                    }
                });

                String upto = tempMap.get(Constants.TAG_UPTO);
                if (upto.contains("-")) {
                    String[] date = upto.split(" - ");
                    long timestamp0 = 0, timestamp1 = 0;
                    if (date[0] != null && date[1] != null) {
                        timestamp0 = Long.parseLong(date[0]);
                        timestamp1 = Long.parseLong(date[1]);

                        holder.date.setText(JoysaleApplication.getDate(getActivity(),timestamp0) + " - " + JoysaleApplication.getDate(getActivity(),timestamp1));
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

        private class ViewHolder {

            TextView itemtitle, date;
            ImageView view;
        }

    }

}
