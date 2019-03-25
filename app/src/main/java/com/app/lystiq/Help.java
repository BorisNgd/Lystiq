package com.app.lystiq;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hitasoft on 16/6/16.
 * <p>
 * This class is for Help Page.
 */

public class Help extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    /**
     * Declare Layout Elements
     **/
    ListView hlist;
    ImageView backbtn;
    TextView title;
    AVLoadingIndicatorView progress;

    /**
     * Declare Variables
     **/
    final String TAG = "Help";
    HelpAdapter helpadapter;
    ArrayList<HashMap<String, String>> helpAry = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        hlist = (ListView) findViewById(R.id.hlist);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.help));

        hlist.setOnItemClickListener(this);
        backbtn.setOnClickListener(this);

        getHelp();

        helpadapter = new HelpAdapter(Help.this, helpAry);
        hlist.setAdapter(helpadapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (helpAry.size() > 0) {
            Intent i = new Intent(Help.this, AboutUs.class);
            i.putExtra(Constants.TAG_TITLE_M, helpAry.get(position).get(Constants.TAG_PAGE_NAME));
            i.putExtra(Constants.CONTENT, helpAry.get(position).get(Constants.TAG_PAGE_CONTENT));
            startActivity(i);
        }
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(Help.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(Help.this);
    }

    /**
     * Function for get the help content from admin
     **/
    private void getHelp() {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_HELP, new Response.Listener<String>() {
            @Override
            public void onResponse(String json) {
                Log.v(TAG,"getHelpRes="+json);
                try {
                    JSONObject obj = new JSONObject(json);
                    String response = DefensiveClass.optString(obj, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        JSONArray result = obj.optJSONArray(Constants.TAG_RESULT);
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject jobj = result.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put(Constants.TAG_PAGE_NAME, DefensiveClass.optString(jobj, Constants.TAG_PAGE_NAME));
                            map.put(Constants.TAG_PAGE_CONTENT, DefensiveClass.optString(jobj, Constants.TAG_PAGE_CONTENT));
                            helpAry.add(map);
                        }
                        progress.setVisibility(View.GONE);
                        helpadapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progress.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                    progress.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progress.setVisibility(View.GONE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.LANG_TYPE,AppUtils.getCurrentLanguageCode(Help.this));
                Log.v(TAG,"getHelpParams="+map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Adapter for Help
     **/
    public class HelpAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> helpContentList;
        ViewHolder holder = null;
        Context mContext;

        public HelpAdapter(Context ctx, ArrayList<HashMap<String, String>> item) {
            mContext = ctx;
            helpContentList = item;
        }

        @Override
        public int getCount() {
            return helpContentList.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.language_list_items, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.next = (ImageView) convertView.findViewById(R.id.next);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);

                holder.next.setVisibility(View.VISIBLE);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (JoysaleApplication.isRTL(mContext)) {
                holder.next.setRotation(180);
                holder.name.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            } else {
                holder.next.setRotation(0);
                holder.name.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            }

            try {
                holder.name.setText(helpContentList.get(position).get("page_name"));
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            TextView name;
            ImageView next;
            RelativeLayout mainLay;
        }
    }

    /**
     * Function for OnClick Event
     **/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
        }
    }
}
