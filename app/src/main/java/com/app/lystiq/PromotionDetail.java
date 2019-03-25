package com.app.lystiq;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.utils.Constants;
import com.app.utils.GetSet;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by hitasoft on 24/6/16.
 * <p>
 * This class is for Display Details of a Promotion.
 */

public class PromotionDetail extends Activity implements View.OnClickListener {
    /**
     * Declare Layout Elements
     **/
    ImageView backBtn, userImg, itemImage;
    TextView username, itemName, promotionType, paidAmount, transactionId, upto, status, repromote;
    LinearLayout dateLay;

    HashMap<String, String> data = new HashMap<>();

    /**
     * Declare Variable
     **/
    static final String TAG = "PromotionDetail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promotion_detail);

        backBtn = (ImageView) findViewById(R.id.backbtn);
        userImg = (ImageView) findViewById(R.id.userImg);
        username = (TextView) findViewById(R.id.username);
        itemImage = (ImageView) findViewById(R.id.imageView);
        itemName = (TextView) findViewById(R.id.itemtitle);
        promotionType = (TextView) findViewById(R.id.addvr);
        paidAmount = (TextView) findViewById(R.id.amount);
        transactionId = (TextView) findViewById(R.id.transid);
        upto = (TextView) findViewById(R.id.date);
        status = (TextView) findViewById(R.id.status);
        repromote = (TextView) findViewById(R.id.promote);
        dateLay = (LinearLayout) findViewById(R.id.dateLay);

        data = (HashMap<String, String>) getIntent().getExtras().get("data");

        backBtn.setVisibility(View.VISIBLE);
        userImg.setVisibility(View.VISIBLE);
        username.setVisibility(View.VISIBLE);
        repromote.setVisibility(View.GONE);

        repromote.setOnClickListener(this);

        Picasso.with(PromotionDetail.this).load(GetSet.getImageUrl()).into(userImg);
        username.setText(GetSet.getUserName());

        setData();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * set promotion details to elements
     **/
    private void setData() {
        try {
            Picasso.with(PromotionDetail.this).load(Constants.url + "media/item/" + data.get(Constants.TAG_ITEM_ID) + "/" + data.get(Constants.TAG_ITEM_IMAGE)).into(itemImage);
            itemName.setText(data.get(Constants.TAG_ITEM_NAME));
            paidAmount.setText(data.get(Constants.TAG_PAID_AMOUNT) + data.get(Constants.TAG_CURRENCY_SYM));
            transactionId.setText(data.get(Constants.TAG_TRANSACTION_ID));

            if (data.get(Constants.TAG_PROMOTION_NAME).equalsIgnoreCase("urgent")) {
                promotionType.setText(getString(R.string.urgent));
            } else {
                promotionType.setText(getString(R.string.ad));
            }

            if (data.get(Constants.TAG_STATUS).equalsIgnoreCase("Live")) {
                status.setText(getString(R.string.live));
            } else {
                status.setText(getString(R.string.expired));
            }

            if (data.get(Constants.TAG_PROMOTION_NAME).equals("urgent")) {
                dateLay.setVisibility(View.GONE);
            } else {
                dateLay.setVisibility(View.VISIBLE);
            }

            if (JoysaleApplication.isRTL(PromotionDetail.this)) {
                itemName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            } else {
                itemName.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            }

            if (data.get(Constants.TAG_UPTO).contains("-")) {
                String[] date = data.get(Constants.TAG_UPTO).split(" - ");
                long timestamp0 = 0, timestamp1 = 0;
                if (date[0] != null && date[1] != null) {
                    timestamp0 = Long.parseLong(date[0]);
                    timestamp1 = Long.parseLong(date[1]);

                    upto.setText(JoysaleApplication.getDate(PromotionDetail.this,timestamp0) + " - " + JoysaleApplication.getDate(PromotionDetail.this,timestamp1));
                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(PromotionDetail.this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(PromotionDetail.this);
    }

    /**
     * Function for Onclick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.promote:
                Intent i = new Intent(PromotionDetail.this, CreatePromote.class);
                i.putExtra("itemId", data.get(Constants.TAG_ITEM_ID));
                startActivity(i);
                break;
        }
    }
}