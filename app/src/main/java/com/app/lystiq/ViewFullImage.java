package com.app.lystiq;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.external.TouchImageView;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by hitasoft.
 * <p>
 * This class is for View a Downloaded Image in Chat and Exchange Chat.
 */

public class ViewFullImage extends AppCompatActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    TouchImageView imageView;
    ImageView back;
    TextView title;

    /**
     * Declare Variables
     **/
    String imgPath = "", imgType = "";
    static final String TAG = "ViewFullImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_image);

        imageView = (TouchImageView) findViewById(R.id.imageView);
        back = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);

        back.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.photos));

        back.setOnClickListener(this);

        imgPath = getIntent().getExtras().getString(Constants.KEY_IMAGE);
        imgType = getIntent().getExtras().getString(Constants.IMAGETYPE);

        if (imgType.equals("local")) {
            File file = new File(imgPath);
            Log.v(TAG, "imgPathLocal=" + imgPath);

            if (!file.exists()) {
                showImageErrorDialog();
            }

            ViewCompat.setTransitionName(imageView, imgPath);
            Picasso.with(this).load(file).into(imageView);

        } else {//Remote Path
            Log.v(TAG, "imgPathRemote=" + imgPath);
            ViewCompat.setTransitionName(imageView, imgPath);
            Picasso.with(this).load(AppUtils.getValidUrl(imgPath)).into(imageView);
        }

        JoysaleApplication.registerReceiver(this);

    }

    private void showImageErrorDialog() {
        final Dialog dialog = new Dialog(ViewFullImage.this);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView subTxt = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView yes = (TextView) dialog.findViewById(R.id.alert_button);
        TextView no = (TextView) dialog.findViewById(R.id.cancel_button);

        subTxt.setText(getString(R.string.sorry_media_file_doesnt_exit));
        no.setText(getString(R.string.ok));

        yes.setVisibility(View.GONE);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        JoysaleApplication.registerReceiver(ViewFullImage.this);
    }

    @Override
    public void onPause() {
        JoysaleApplication.unregisterReceiver(ViewFullImage.this);
        super.onPause();
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
