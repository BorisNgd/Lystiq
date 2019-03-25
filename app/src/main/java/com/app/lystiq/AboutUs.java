package com.app.lystiq;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.utils.Constants;

/**
 * Created by hitasoft on 10/6/16.
 **/
public class AboutUs extends AppCompatActivity implements View.OnClickListener {

    // Widget Declaration
    ImageView backbtn;
    TextView title, content;
    WebView webView;

    // Variable Declaration
    String pageTitle = "", pageContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutus);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        webView = (WebView) findViewById(R.id.webView);

        backbtn.setOnClickListener(this);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);

        pageTitle = (String) getIntent().getExtras().get(Constants.TAG_TITLE_M);
        pageContent = (String) getIntent().getExtras().get(Constants.CONTENT);

        title.setText(pageTitle);
        webView.loadData(pageContent, "text/html", "UTF-8");

    }


    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(AboutUs.this);
    }


    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(AboutUs.this);
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
