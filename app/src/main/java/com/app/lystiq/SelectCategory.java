package com.app.lystiq;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 2/7/16.
 * <p>
 * This class is for Provide a Category List for Add new Product.
 */

public class SelectCategory extends AppCompatActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    TextView title;
    ImageView backbtn;
    ListView categoryList, subList;

    CategoryAdapter categoryAdapter;
    SubAdapter subAdapter;
    ArrayList<HashMap<String, String>> categAry = new ArrayList<HashMap<String, String>>();
    ArrayList<ArrayList<HashMap<String, String>>> subcategAry = new ArrayList<ArrayList<HashMap<String, String>>>();

    // Declaration of Varialbles
    int padding;
    String TAG = "SelectCategory";
    String from = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_category);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        categoryList = (ListView) findViewById(R.id.categoryList);
        subList = (ListView) findViewById(R.id.subList);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.categories));

        from = (String) getIntent().getExtras().get(Constants.FROM);
        categAry = (ArrayList<HashMap<String, String>>) getIntent().getExtras().get("categAry");
        subcategAry = (ArrayList<ArrayList<HashMap<String, String>>>) getIntent().getExtras().get("subcategAry");

        padding = JoysaleApplication.dpToPx(SelectCategory.this, 15);

        categoryAdapter = new CategoryAdapter(SelectCategory.this, categAry);
        categoryList.setAdapter(categoryAdapter);

        Log.v(TAG, "subcategAry" + subcategAry);

        if (AddProductDetail.categId.equals("")) {
            if (categAry.size() > 0 && subcategAry.size() > 0) {
                Log.v(TAG, "if");
                AddProductDetail.categId = categAry.get(0).get("id");
                if (AddProductDetail.category != null) {
                    AddProductDetail.category.setText(categAry.get(0).get("name"));
                    AddProductDetail.category.setTextColor(getResources().getColor(R.color.primaryText));
                    AddProductDetail.catArrow.setColorFilter(getResources().getColor(R.color.primaryText));

                    setCategoryConditions(categAry.get(0));
                }

                categoryAdapter.notifyDataSetChanged();

                subAdapter = new SubAdapter(SelectCategory.this, subcategAry.get(0));
                subList.setAdapter(subAdapter);
            }
        } else {
            categoryAdapter.notifyDataSetChanged();
            if (categAry.size() > 0 && subcategAry.size() > 0) {
                for (int i = 0; i < categAry.size(); i++) {
                    if (categAry.get(i).get("id").equals(AddProductDetail.categId)) {
                        Log.v(TAG, "else");
                        if (AddProductDetail.category != null) {
                            AddProductDetail.category.setText(categAry.get(i).get("name"));
                            AddProductDetail.category.setTextColor(getResources().getColor(R.color.primaryText));
                            AddProductDetail.catArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                        }
                        categoryAdapter.notifyDataSetChanged();

                        subAdapter = new SubAdapter(SelectCategory.this, subcategAry.get(i));
                        subList.setAdapter(subAdapter);
                        break;
                    }
                }
            }
        }
        backbtn.setOnClickListener(this);

    }

    /**
     * Function for set product condition by choosing the category
     **/

    private void setCategoryConditions(HashMap<String, String> map) {
        if (map.get("id").equals(AddProductDetail.categId)) {
            if (map.get("product_condition").equals("disable") && map.get("exchange_buy").equals("disable")
                    && map.get("make_offer").equals("disable")) {
                AddProductDetail.bottomLay.setVisibility(View.GONE);
                AddProductDetail.conditionLay.setVisibility(View.GONE);
                AddProductDetail.exchangeLay.setVisibility(View.GONE);
                AddProductDetail.offerLay.setVisibility(View.GONE);
            } else {
                AddProductDetail.bottomLay.setVisibility(View.VISIBLE);
                AddProductDetail.conditionLay.setVisibility(View.VISIBLE);
                AddProductDetail.exchangeLay.setVisibility(View.VISIBLE);
                if(AddProductDetail.offerLay.getVisibility()==View.GONE && !AddProductDetail.givingAwaySwitch.isChecked()){
                    AddProductDetail.offerLay.setVisibility(View.VISIBLE);
                }
                if (map.get("product_condition").equals("enable")) {
                    AddProductDetail.conditionLay.setVisibility(View.VISIBLE);
                } else {
                    AddProductDetail.conditionLay.setVisibility(View.GONE);
                }

                if (map.get("exchange_buy").equals("enable")) {
                    if (Constants.EXCHANGE) {
                        AddProductDetail.exchangeLay.setVisibility(View.VISIBLE);
                    } else {
                        AddProductDetail.exchangeLay.setVisibility(View.GONE);
                    }
                } else {
                    AddProductDetail.exchangeLay.setVisibility(View.GONE);
                }

            }

            if (map.get("instant_buy").equals("disable")) {
                AddProductDetail.instantLay.setVisibility(View.GONE);
            } else {
                if (Constants.BUYNOW) {
                    AddProductDetail.instantLay.setVisibility(View.VISIBLE);
                } else {
                    AddProductDetail.instantLay.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(SelectCategory.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(SelectCategory.this);
    }

    /**
     * Adapter for Category
     **/

    public class CategoryAdapter extends BaseAdapter {

        ArrayList<HashMap<String, String>> datas;
        ViewHolder holder = null;
        Context mContext;

        public CategoryAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            datas = data;
        }

        @Override
        public int getCount() {
            return datas.size();
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
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.category_list_item, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.selectIcon = (ImageView) convertView.findViewById(R.id.selectIcon);
                holder.selectView = (View) convertView.findViewById(R.id.selectView);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                holder.name.setText(datas.get(position).get("name"));
                Picasso.with(mContext).load(datas.get(position).get(Constants.KEY_IMAGE)).into(holder.icon);
                if (datas.get(position).get("id").equals(AddProductDetail.categId)) {
                    holder.selectIcon.setVisibility(View.VISIBLE);
                    if (JoysaleApplication.isRTL(SelectCategory.this)) {
                        holder.selectIcon.setImageDrawable(getResources().getDrawable(R.drawable.category_select_reverse));
                    } else {
                        holder.selectIcon.setImageDrawable(getResources().getDrawable(R.drawable.category_select_arrow));
                    }
                    holder.selectView.setVisibility(View.VISIBLE);
                } else {
                    holder.selectIcon.setVisibility(View.GONE);
                    holder.selectView.setVisibility(View.GONE);
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddProductDetail.categId = datas.get(position).get("id");
                        categoryAdapter.notifyDataSetChanged();

                        if (AddProductDetail.category != null) {
                            AddProductDetail.category.setText(datas.get(position).get("name"));
                            AddProductDetail.category.setTextColor(getResources().getColor(R.color.primaryText));
                            AddProductDetail.catArrow.setColorFilter(getResources().getColor(R.color.primaryText));

                            setCategoryConditions(datas.get(position));
                        }

                        if (subcategAry.get(position).size() > 0) {
                            subAdapter = new SubAdapter(mContext, subcategAry.get(position));
                            subList.setAdapter(subAdapter);
                        } else {
                            finish();
                        }
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
            ImageView icon, selectIcon;
            TextView name;
            View selectView;
            RelativeLayout mainLay;
        }
    }

    /**
     * Adapter for Sub Category
     **/

    public class SubAdapter extends BaseAdapter {

        ArrayList<HashMap<String, String>> datas;
        ViewHolder holder = null;
        Context mContext;

        public SubAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            datas = data;
        }

        @Override
        public int getCount() {
            return datas.size();
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
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_row_selection, parent, false);//layout
                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.tick = (ImageView) convertView.findViewById(R.id.tick);
                holder.mainLay = (RelativeLayout) convertView.findViewById(R.id.mainLay);

                holder.mainLay.setPadding(0, padding, padding, padding);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                holder.name.setText(datas.get(position).get("name"));
                holder.tick.setVisibility(View.INVISIBLE);
                if (datas.get(position).get("id").equals(AddProductDetail.subcategId)) {
                    holder.tick.setVisibility(View.VISIBLE);
                    holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.tick.setVisibility(View.INVISIBLE);
                    holder.name.setTextColor(getResources().getColor(R.color.primaryText));
                }

                holder.mainLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AddProductDetail.subcategId = datas.get(position).get("id");
                        subAdapter.notifyDataSetChanged();
                        finish();
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
            ImageView tick;
            TextView name;
            RelativeLayout mainLay;
        }

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
        }
    }
}
