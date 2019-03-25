package com.app.lystiq;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.external.ObjectSerializer;

import java.util.ArrayList;

/**
 * Created by hitasoft on 22/6/16.
 * <p>
 * This class is for Search a Product.
 */

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    ImageView backbtn, resetbtn;
    EditText titleEdit;
    RelativeLayout searchLay;
    ListView listView;
    InputMethodManager imm;

    HistoryAdapter historyAdapter;
    ArrayList<String> searchAry = new ArrayList<String>();
    SharedPreferences pref;
    SharedPreferences.Editor edit;

    /**
     * Declare Variables
     **/
    String TAG = "SearchActivity";
    public static String searchQuery = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        resetbtn = (ImageView) findViewById(R.id.resetbtn);
        titleEdit = (EditText) findViewById(R.id.titleEdit);
        searchLay = (RelativeLayout) findViewById(R.id.searchLay);
        listView = (ListView) findViewById(R.id.listView);

        backbtn.setVisibility(View.VISIBLE);
        titleEdit.setVisibility(View.VISIBLE);
        resetbtn.setVisibility(View.INVISIBLE);
        searchLay.setVisibility(View.VISIBLE);

        ViewCompat.setElevation(searchLay, 6);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        backbtn.setOnClickListener(this);
        resetbtn.setOnClickListener(this);

        pref = getApplicationContext().getSharedPreferences("SearchHistory", MODE_PRIVATE);
        edit = pref.edit();

        //To initialize and set adapter
        historyAdapter = new HistoryAdapter(SearchActivity.this, searchAry);
        listView.setAdapter(historyAdapter);

        // to get searchAry list and notify adapter
        getHistory();

        titleEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.i(TAG, "Enter pressed");
                    if (titleEdit.getText().toString().trim().length() > 0) {
                        try {
                            searchQuery = titleEdit.getText().toString();
                            if (searchQuery.trim().length() != 0 && !queryContains(searchQuery)) {
                                Log.v(TAG, "Query Added");
                                if (searchAry.size() > 0) {
                                    searchAry.remove(searchAry.indexOf("clear-" + getString(R.string.clear_hitsory)));
                                }
                                searchAry.add(0, "name-" + searchQuery);
                                searchAry.add("clear-" + getString(R.string.clear_hitsory));
                                historyAdapter.notifyDataSetChanged();
                            }
                            FragmentMainActivity.filterAry.clear();
                            SearchAdvance.applyFilter = true;
                            finish();
                            Intent i = new Intent(SearchActivity.this, FragmentMainActivity.class);
                            startActivity(i);

                            storeHistory();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });

        titleEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count != 0) {
                    resetbtn.setVisibility(View.VISIBLE);
                } else {
                    resetbtn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean queryContains(String value) {
        boolean flag = false;
        for (int i = 0; i < searchAry.size(); i++) {
            String split[] = searchAry.get(i).split("-");
            String type = split[0];
            String query = split[1];
            if (type.equals("name")) {
                if (value.equalsIgnoreCase(query)) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    public void storeHistory() {
        try {
            ArrayList<String> tempAry = new ArrayList<String>();
            tempAry.addAll(searchAry);
            if (tempAry.size() > 0) {
                tempAry.remove(tempAry.indexOf("clear-" + getString(R.string.clear_hitsory)));
            }
            Log.v("tempAry", "tempAry=" + tempAry);
            edit.clear();
            edit.putString("history", ObjectSerializer.serialize(tempAry));
            edit.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //To get searchAry and notify adapter
    public void getHistory() {
        try {
            ArrayList<String> tempAry = (ArrayList<String>) ObjectSerializer.deserialize(pref.getString("history", ObjectSerializer.serialize(new ArrayList<String>())));
            searchAry.addAll(tempAry);
            if (searchAry.size() > 0) {
                searchAry.add("clear-" + getString(R.string.clear_hitsory));
            }
            Log.v(TAG, "search=" + searchAry);
            historyAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(SearchActivity.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(SearchActivity.this);
    }

    /**
     * Adapter for Search History
     **/

     class HistoryAdapter extends BaseAdapter {
        ArrayList<String> data;
        Context mContext;
        ViewHolder holder = null;

        public HistoryAdapter(Context context, ArrayList<String> list) {
            data = list;
            mContext = context;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.recent_search_item, null);//layout
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.layout = (RelativeLayout) convertView.findViewById(R.id.layout);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                String split[] = data.get(position).split("-");
                final String type = split[0];
                final String query = split[1];
                holder.name.setText(query);
                if (type.equals("name")) {
                    holder.image.setImageResource(R.drawable.search_gray);
                } else if (type.equals("clear")) {
                    holder.image.setImageResource(R.drawable.cancel);
                }

                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type.equals("clear")) {
                            searchAry.clear();
                            edit.clear();
                            edit.commit();
                            historyAdapter.notifyDataSetChanged();
                        } else {
                            searchQuery = query;
                            FragmentMainActivity.filterAry.clear();
                            SearchAdvance.applyFilter = true;
                            finish();
                            Intent i = new Intent(SearchActivity.this, FragmentMainActivity.class);
                            startActivity(i);
                        }
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            TextView name;
            ImageView image;
            RelativeLayout layout;
        }
    }

    /**
     * Function for OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                imm.hideSoftInputFromWindow(titleEdit.getWindowToken(), 0);
                titleEdit.clearFocus();
                finish();
                break;
            case R.id.resetbtn:
                titleEdit.setText("");
                resetbtn.setVisibility(View.INVISIBLE);
                break;
        }
    }
}