package com.app.lystiq;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hitasoft on 1/7/16.
 * <p>
 * This class is for Provide a Single Product Image from a List of Images in a Product.
 */

public class SingleView extends Activity {

    /**
     * Declare Layout Elements
     **/
    ImageView back;
    ViewPager viewPager;
    TextView title;


    //Declare variables
    String TAG = "SingleView";

    ViewPagerAdapter pagerAdapter;
    ArrayList<String> newary;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_view);

        viewPager = (ViewPager) findViewById(R.id.pager);
        back = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);

        newary = (ArrayList<String>) getIntent().getExtras().get("mimages");

        back.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.photos));
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SingleView.this.finish();
            }
        });

        //To initialize and set Adapter
        pagerAdapter = new ViewPagerAdapter(getBaseContext(), newary);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(getIntent().getExtras().getInt("position"));
        viewPager.setPageMargin(dpToPx(1));

    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = SingleView.this.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(SingleView.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(SingleView.this);
    }

    /**
     * Adapter for swiping the images
     **/

    class ViewPagerAdapter extends PagerAdapter {

        Context context;
        LayoutInflater inflater;
        ArrayList<String> temp;

        public ViewPagerAdapter(Context act, ArrayList<String> newary) {
            this.temp = newary;
            this.context = act;
        }

        public int getCount() {
            return temp.size();
        }

        public Object instantiateItem(ViewGroup collection, int position) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.single_image,
                    collection, false);

            final ImageView image = (ImageView) itemView
                    .findViewById(R.id.imgDisplay);

            Log.v(TAG, "position" + position);

            Picasso.with(SingleView.this).load(temp.get(position)).into(image);
            ((ViewPager) collection).addView(itemView, 0);

            return itemView;

        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}
