package com.app.lystiq;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.app.external.PlacesAutoCompleteAdapter;
import com.app.utils.Constants;
import com.app.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.app.lystiq.ChatActivity.LOCATION_FETCH_ACTION;

/**
 * Created by hitasoft on 29/3/16.
 * <p>
 * This class is for Set a Location from Google Map by User.
 */

public class LocationActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, TextWatcher,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /**
     * Declare Layout Elements
     **/
    MapView mapView;
    GoogleMap googleMap;
    TextView title, setLoc, remLoc;
    ImageView backbtn, crossIcon, myLocation;
    AutoCompleteTextView address;
    Display display;
    RelativeLayout searchLay;
    ProgressDialog dialog;

    InputMethodManager imm;
    GoogleApiClient googleApiClient;
    LatLng center;
    List<Address> addresses;
    Location mylocation;

    /**
     * Declare Varaibles
     **/
    final String TAG = "LocationActivity";
    final static int REQUEST_CHECK_SETTINGS_GPS = 0x1, REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;
    public static boolean locationRemoved = false;
    public static double lat, lon;
    double tempLat, tempLng;
    public static String location = "World Wide";
    String tempLocation = "World Wide";
    public static SharedPreferences adminPref;
    String chatId, exchangeProdId, from = "";
    private boolean myLocationClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale.setDefault(getResources().getConfiguration().locale);
        setContentView(R.layout.location_activity);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        backbtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        setLoc = (TextView) findViewById(R.id.apply);
        remLoc = (TextView) findViewById(R.id.reset);
        address = (AutoCompleteTextView) findViewById(R.id.address);
        crossIcon = (ImageView) findViewById(R.id.cross_icon);
        myLocation = (ImageView) findViewById(R.id.my_location);
        searchLay = (RelativeLayout) findViewById(R.id.searchLayout);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Typeface font = Typeface.createFromAsset(getAssets(), "font_regular.ttf");
        address.setTypeface(font);

        Constants.filpref = getApplicationContext().getSharedPreferences("FilterPref",
                MODE_PRIVATE);
        Constants.fileditor = Constants.filpref.edit();

        dialog = new ProgressDialog(LocationActivity.this,R.style.AppCompatAlertDialogStyle);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        from = (String) getIntent().getExtras().get(Constants.FROM);
        chatId = getIntent().getExtras().getString(Constants.CHATID);

        switch (from) {
            case "add":
                remLoc.setVisibility(View.GONE);
                break;
            case "chat":
                searchLay.setVisibility(View.GONE);
                setLoc.setVisibility(View.GONE);
                remLoc.setText(getString(R.string.share));
                break;
            case "home":
                if (location.equalsIgnoreCase("World Wide")) {
                    address.setText("");
                    imm.hideSoftInputFromWindow(address.getWindowToken(), 0);
                    address.clearFocus();
                } else {
//                    address.setText(location);
                    address.setText("");
                    imm.hideSoftInputFromWindow(address.getWindowToken(), 0);
                    address.clearFocus();
                }
                break;
        }

        exchangeProdId = getIntent().getStringExtra(Constants.TAG_EXCHANGEID);
        if (exchangeProdId == null)
            exchangeProdId = "0";

        backbtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        crossIcon.setVisibility(View.GONE);

        title.setText(getString(R.string.location));
        backbtn.setOnClickListener(this);
        setLoc.setOnClickListener(this);
        remLoc.setOnClickListener(this);
        crossIcon.setOnClickListener(this);
        address.setOnItemClickListener(this);
        myLocation.setOnClickListener(this);
        address.addTextChangedListener(this);

        adminPref = getApplicationContext().getSharedPreferences("JoysaleAdminPref",
                MODE_PRIVATE);

        /** Function for join the user to chat **/
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                JSONObject jobj = new JSONObject();
                try {
                    jobj.put("joinid", GetSet.getUserName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 2000);

        //To initialize and set Adapter in address listiew
        address.setAdapter(new PlacesAutoCompleteAdapter(LocationActivity.this, R.layout.dropdown_layout));

        address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                imm.hideSoftInputFromWindow(address.getWindowToken(), 0);
                if (checkPermissions(Constants.LOCATION_PERMISSIONS, LocationActivity.this)) {
                    try {
                        Double latn[] = new Double[2];
                        if (address.getText().toString().trim().length() != 0) {
                            latn = getGeoCodeLatLng(address.getText().toString().trim());
                            double lat = latn[0];
                            double lon = latn[1];

                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15);
                            if (googleMap != null)
                                googleMap.animateCamera(cameraUpdate);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ActivityCompat.requestPermissions(LocationActivity.this, Constants.LOCATION_PERMISSIONS, Constants.LOCATION_PERMISSION_CODE);
                }
            }
        });

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);

        // Updates the location and zoom of the MapView

        display = this.getWindowManager().getDefaultDisplay();
        address.setDropDownWidth(display.getWidth() - JoysaleApplication.dpToPx(this, 30));

    }

    @Override
    public void onMapReady(final GoogleMap map) {
        Log.v(TAG, "map=" + map);
        googleMap = map;
        if (map != null) {
           // address.clearFocus();
            map.getUiSettings().setMyLocationButtonEnabled(false);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                map.setMyLocationEnabled(true);
            }
            if (from.equals("home") || from.equals("chat")) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15);
                map.animateCamera(cameraUpdate);
                if (location.equalsIgnoreCase("World Wide")) {
                    address.setText("");
                } else {
//                    address.setText(location);
                    address.setText("");
                }
            } else if (from.equals("add")) {
                if (AddProductDetail.lat == 0 && AddProductDetail.lon == 0) {
                    setMyLocation();
                } else {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(AddProductDetail.lat, AddProductDetail.lon), 15);
                    map.animateCamera(cameraUpdate);
                }
            }

            map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
             //       address.clearFocus();
                    center = map.getCameraPosition().target;
//                    Log.v(TAG, "center-latitude=" + center.latitude + " &center-longitude=" + center.longitude);
                    tempLat = center.latitude;
                    tempLng = center.longitude;
                    if (myLocationClicked)
                        new GetLocationAsync(tempLat, tempLng).execute();
                    map.clear();
                    setLoc.setOnClickListener(LocationActivity.this);
                    remLoc.setOnClickListener(LocationActivity.this);
                }
            });

            map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                @Override
                public void onCameraMove() {
             //       address.clearFocus();
                    setLoc.setOnClickListener(null);
                    if (!remLoc.getText().toString().equals(getString(R.string.world_wide))) {
                        remLoc.setOnClickListener(null);
                    }
                }
            });

        //    address.clearFocus();

            //To get Latlng
            loadData();

        }

        /*View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        locationButton.setBackgroundResource(R.drawable.my_location);
        // and next place it, for exemple, on bottom right (as Google Maps app)
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);*/

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Function for get the lat, lon from gps
     **/
    private void loadData() {
        if (from.equals("home") | from.equals("chat")) {
            if (lat == 0 && lon == 0) {
                if (googleApiClient == null) {
                    setUpGClient();
                } else if (mylocation == null) {
                    getMyLocation();
                } else {
                    tempLat = mylocation.getLatitude();
                    tempLng = mylocation.getLongitude();
                    Log.v("lat&lon", "lat = " + LocationActivity.lat + "&lon=" + LocationActivity.lon);
                }
            }
        } else if (from.equals("add")) {
            if (AddProductDetail.lat == 0 && AddProductDetail.lon == 0) {
                if (googleApiClient == null) {
                    setUpGClient();
                } else if (mylocation == null) {
                    getMyLocation();
                } else {
                    AddProductDetail.lat = mylocation.getLatitude();
                    AddProductDetail.lon = mylocation.getLongitude();
                    Log.v("lat&lon", "lat = " + AddProductDetail.lat + "&lon=" + AddProductDetail.lon);
                }
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            crossIcon.setVisibility(View.VISIBLE);
        } else {
            crossIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mylocation = location;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new
                LatLng(mylocation.getLatitude(), mylocation.getLongitude()), 15));
    //    address.clearFocus();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        getMyLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void getMyLocation() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(LocationActivity.this,
                        ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(3000);
                    locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(googleApiClient, locationRequest, LocationActivity.this);
                    PendingResult result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(LocationActivity.this,
                                                    ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        mylocation = LocationServices.FusedLocationApi
                                                .getLastLocation(googleApiClient);
                                        if (mylocation != null) {
                                            tempLat = mylocation.getLatitude();
                                            tempLng = mylocation.getLongitude();
                                            try {
                                                new GetLocationAsync(tempLat, tempLng).execute().get();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            } catch (ExecutionException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        Log.v("mylocation", "mylocation=" + mylocation);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult(LocationActivity.this,
                                                REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied. However, we have no way to fix the
                                    // settings so we won't show the dialog.
                                    //finish();
                                    break;
                            }
                        }
                    });
                }
            } else {
                googleApiClient.connect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        setMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        crossIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        // For Internet checking
        JoysaleApplication.registerReceiver(LocationActivity.this);
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(LocationActivity.this);
        // chat.disconnect();
        super.onPause();
    }

    /**
     * Function for send the location to server
     **/

    private void sendLocationToServer(final String chat_type, final String type, final String params, final double latitude, final double longitude) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_SEND_CHAT, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                if (dialog.isShowing() && dialog != null) {
                    dialog.dismiss();
                }
                long unixTime = System.currentTimeMillis() / 1000L;

                JSONObject jsonObject = callSocket(unixTime, "share_location", latitude, longitude);
                Intent i = new Intent(LocationActivity.this, ChatActivity.class);
                i.putExtra(Constants.TAG_CURRENT_LATITUDE, latitude + " ");
                i.putExtra(Constants.TAG_CURRENT_LONGITUDE, longitude + " ");
                i.putExtra("jsonObject", String.valueOf(jsonObject));
                setResult(LOCATION_FETCH_ACTION, i);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (dialog.isShowing() && dialog != null) {
                    dialog.dismiss();
                }
                JoysaleApplication.dialog(LocationActivity.this, getResources().getString(R.string.alert), getString(R.string.somethingwrong));
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                long unixTime = System.currentTimeMillis() / 1000L;
                double lat = latitude;
                double lon = longitude;
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                map.put(Constants.TAG_CHAT_ID, chatId);
                if (getIntent().getStringExtra(Constants.TAG_SOURCE_ID) != null)
                    map.put(Constants.TAG_SOURCE_ID, getIntent().getStringExtra(Constants.TAG_SOURCE_ID));
                else
                    map.put(Constants.TAG_SOURCE_ID, "0");
                map.put(Constants.TAG_IMAGE_URL, "");
                map.put(Constants.TAG_TYPE, type);
                map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
                map.put(Constants.TAG_MESSAGE, params);
                if (chat_type != null && chat_type.length() != 0) {
                    map.put(Constants.TAG_CHAT_TYPE, chat_type);
                } else {
                    map.put(Constants.TAG_CHAT_TYPE, "normal");
                }

                if (lat != 0.0 && longitude != 0.0) {
                    try {
                        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
                        DecimalFormat dFormat = new DecimalFormat("#.######");
                        if (JoysaleApplication.isRTL(getApplicationContext())) {
                            lat = Double.valueOf(nf.format(lat));
                            lon = Double.valueOf(nf.format(lon));
                        } else {
                    /*Latitude and Longitude contains 14 digit after decimal.It causes Serialization Exception in soap
                    so reduce it in to 6 digits after Decimal*/
                            lat = Double.valueOf(dFormat.format(lat));
                            lon = Double.valueOf(dFormat.format(lon));
                        }
                        map.put(Constants.TAG_CURRENT_LATITUDE, String.valueOf(lat));
                        map.put(Constants.TAG_CURRENT_LONGITUDE, String.valueOf(lon));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    map.put(Constants.TAG_CURRENT_LATITUDE, "");
                    map.put(Constants.TAG_CURRENT_LONGITUDE, "");
                }
                Log.v(TAG, "locationsendparams=" + map);

                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for get the address from lat, lon
     **/

    private class GetLocationAsync extends AsyncTask<String, Void, String> {
        double x, y;

        public GetLocationAsync(double latitude, double longitude) {
            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            addresses = JoysaleApplication.getLocationFromLatLng(LocationActivity.this, from, x, y);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
              //  address.clearFocus();
                if (addresses != null && !addresses.isEmpty()) {

                    Log.v(TAG, "part1=" + addresses.get(0).getAddressLine(0));
                    Log.v(TAG, "part2=" + addresses.get(0).getAddressLine(1));
                    Log.v(TAG, "part3=" + addresses.get(0).getCountryName());

                    if (from.equals("home")) {
                        if (addresses.get(0).getAddressLine(1) != null)
                            tempLocation = addresses.get(0).getAddressLine(0) + ", "
                                    + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                        else
                            tempLocation = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getCountryName();
//                        address.setText(tempLocation);
                        myLocationClicked = false;
                    } else if (from.equals("add")) {
                        if (addresses.get(0).getAddressLine(1) != null)
                            tempLocation = addresses.get(0).getAddressLine(0) + ", "
                                    + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                        else
                            tempLocation = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getCountryName();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    /**
     * Function to send a Location to Socket
     **/
    private JSONObject callSocket(long time, String type, double lat, double lon) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_CHATTIME, Long.toString(time));
            message.put(Constants.SOCK_USERIMAGE, GetSet.getImageUrl().replace("/150/", "/40/"));
            message.put(Constants.SOCK_USERNAME, GetSet.getUserName());
            message.put(Constants.TAG_MESSAGE, "");
            message.put(Constants.SOCK_VIEW_URL, "");
            message.put(Constants.TAG_TYPE, "share_location");
            message.put(Constants.TAG_LAT, lat);
            message.put(Constants.TAG_LON, lon);
            message.put(Constants.SOCK_MESSAGE_CONTENT, "3");
            jobj.put(Constants.TAG_MESSAGE, message);
            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
            jobj.put(Constants.SOCK_SENDERID, getIntent().getStringExtra(Constants.TAG_USERNAME));
            jobj.put("offerId", "0");
            Log.v(TAG, "source id=" + getIntent().getStringExtra(Constants.TAG_SOURCE_ID));
            if (getIntent().getStringExtra(Constants.TAG_SOURCE_ID) != null) {
                jobj.put(Constants.SOCK_SOURCE_ID, getIntent().getStringExtra(Constants.TAG_SOURCE_ID));
                //  mSocket.emit("exmessage", jobj);
            } else {
                jobj.put(Constants.SOCK_SOURCE_ID, "0");
                //  mSocket.emit("message", jobj);
            }
            Log.v(TAG, "sendLocationJSON=" + jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobj;
    }

    /**
     * Class for get the lat, lon from address
     **/

    /**
     * Function for Onclick Events
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.reset:
                switch (from) {
                    case "home":
                        lat = 0;
                        lon = 0;
                        location = getString(R.string.world_wide);
                        locationRemoved = true;
                        Constants.fileditor.putString("location", location);
                        Constants.fileditor.putString("lat", String.valueOf(lat));
                        Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lon));
                        Constants.fileditor.putBoolean("locationRemoved", locationRemoved);
                        Constants.fileditor.commit();

                        SearchAdvance.distance = "0";
                        FragmentMainActivity.currentPage = 0;
                        FragmentMainActivity.homeItemList.clear();
                        if (FragmentMainActivity.itemAdapter != null) {
                            FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                        }
                        finish();
                        Intent i = new Intent(LocationActivity.this, FragmentMainActivity.class);
                        startActivity(i);
                        break;
                    case "add":
                        if (checkPermissions(Constants.LOCATION_PERMISSIONS, LocationActivity.this)) {
                            AddProductDetail.lat = 0;
                            AddProductDetail.lon = 0;
                            AddProductDetail.loc = "";
                            if (AddProductDetail.location != null) {
                                AddProductDetail.location.setText(getString(R.string.world_wide));
                                AddProductDetail.location.setTextColor(getResources().getColor(R.color.secondaryText));
                                AddProductDetail.locArrow.setColorFilter(getResources().getColor(R.color.secondaryText));
                            }
                            finish();
                        } else {
                            ActivityCompat.requestPermissions(LocationActivity.this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, Constants.LOCATION_PERMISSION_CODE);
                        }
                        break;
                    case "chat":
                        if (checkPermissions(Constants.LOCATION_PERMISSIONS, LocationActivity.this)) {
                            if(tempLat == 0.0 || tempLng == 0.0) {
                             Toast.makeText(getApplicationContext(), R.string.choose_a_location,Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.setMessage(getString(R.string.pleasewait));
                                dialog.setCancelable(false);
                                dialog.setCanceledOnTouchOutside(false);
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                                    Drawable drawable = new ProgressBar(LocationActivity.this).getIndeterminateDrawable().mutate();
                                    drawable.setColorFilter(ContextCompat.getColor(LocationActivity.this, R.color.progressColor),
                                            PorterDuff.Mode.SRC_IN);
                                    dialog.setIndeterminateDrawable(drawable);
                                }
                                dialog.show();
                                sendLocationToServer(getIntent().getStringExtra("chat_type"), "share_location", "", tempLat, tempLng);
                            }
                        } else {
                            ActivityCompat.requestPermissions(LocationActivity.this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, Constants.LOCATION_PERMISSION_CODE);
                        }
                        break;
                }
                break;
            case R.id.apply:
                if (checkPermissions(Constants.LOCATION_PERMISSIONS, LocationActivity.this)) {
                    if (from.equals("home")) {
                        try {
                            if (address.getText().toString().trim().length() == 0) {
                                addresses = JoysaleApplication.getLocationFromLatLng(LocationActivity.this, from, center.latitude, center.longitude);

                                if (addresses.get(0).getAddressLine(1) != null)
                                    tempLocation = addresses.get(0).getAddressLine(0) + ", "
                                            + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                                else
                                    tempLocation = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getCountryName();

                                address.setText(tempLocation);
                                location = tempLocation;
                                lat = center.latitude;
                                lon = center.longitude;
                                locationRemoved = false;
                                Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                                Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                                Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lon));
                                Constants.fileditor.putBoolean("locationRemoved", locationRemoved);
                                Constants.fileditor.commit();
                            } else {
                                Double latn[] = new Double[2];
//                                latn = getGeoCodeLatLng(address.getText().toString().trim());
                                lat = center.latitude;
                                lon = center.longitude;
                                location = address.getText().toString().trim();
                                locationRemoved = false;
                                Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                                Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                                Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lon));
                                Constants.fileditor.putBoolean("locationRemoved", locationRemoved);
                                Constants.fileditor.commit();
                                if (FragmentMainActivity.locationTxt != null) {
                                    FragmentMainActivity.locationTxt.setText(location);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        FragmentMainActivity.currentPage = 0;
                        if (FragmentMainActivity.mScrollListener != null) {
                            FragmentMainActivity.mScrollListener.resetpagecount();
                        }
                        FragmentMainActivity.homeItemList.clear();
                        if (FragmentMainActivity.itemAdapter != null) {
                            FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                        }
                        finish();
                        Intent j = new Intent(LocationActivity.this, FragmentMainActivity.class);
                        j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(j);
                    } else {
                        try {
                            if (address.getText().toString().trim().length() == 0) {
                                addresses = JoysaleApplication.getLocationFromLatLng(LocationActivity.this, from, center.latitude, center.longitude);

                                if (addresses.get(0).getAddressLine(1) != null)
                                    tempLocation = addresses.get(0).getAddressLine(0) + ", "
                                            + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
                                else
                                    tempLocation = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getCountryName();

                                AddProductDetail.lat = center.latitude;
                                AddProductDetail.lon = center.longitude;
                                AddProductDetail.loc = tempLocation;
                                AddProductDetail.location.setText(AddProductDetail.loc);
                                AddProductDetail.location.setTextColor(getResources().getColor(R.color.primaryText));
                                AddProductDetail.locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                            } else {
                                Double latn[] = new Double[2];
//                                latn = getGeoCodeLatLng(address.getText().toString().trim());
                                AddProductDetail.lat = center.latitude;
                                AddProductDetail.lon = center.longitude;
                                AddProductDetail.loc = address.getText().toString().trim();
                                if (AddProductDetail.location != null) {
                                    AddProductDetail.location.setText(AddProductDetail.loc);
                                    AddProductDetail.location.setTextColor(getResources().getColor(R.color.primaryText));
                                    AddProductDetail.locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                } else {
                    ActivityCompat.requestPermissions(LocationActivity.this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, Constants.LOCATION_PERMISSION_CODE);
                }
                break;
            case R.id.cross_icon:
                address.setText("");
                crossIcon.setVisibility(View.GONE);
                break;
            case R.id.my_location:
                if (checkPermissions(Constants.LOCATION_PERMISSIONS, LocationActivity.this)) {
                    myLocationClicked = true;
                    setMyLocation();
                } else {
                    ActivityCompat.requestPermissions(LocationActivity.this, Constants.LOCATION_PERMISSIONS, Constants.LOCATION_PERMISSION_CODE);
                }
                break;
        }
    }

    private void setMyLocation() {
        if (googleApiClient == null) {
            setUpGClient();
        } else if (mylocation == null) {
            getMyLocation();
        } else {
            addresses = JoysaleApplication.getLocationFromLatLng(LocationActivity.this, from, mylocation.getLatitude(), mylocation.getLongitude());
            if (addresses.get(0).getAddressLine(1) != null)
                tempLocation = addresses.get(0).getAddressLine(0) + ", "
                        + addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getCountryName();
            else
                tempLocation = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getCountryName();
            address.setText(tempLocation);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new
                    LatLng(mylocation.getLatitude(), mylocation.getLongitude()), 15));
        }
    }

    private Double[] getGeoCodeLatLng(String params) {
        Double[] latlng = new Double[2];
        Geocoder gc = new Geocoder(getApplicationContext());
        if (Geocoder.isPresent()) {
            List<Address> list = new ArrayList<>();
            try {
                list = gc.getFromLocationName(params, 1);
                if (list.size() > 0) {
                    Log.e(TAG, "getGeoCodeLatLng: " + list.get(0));
                    Address address = list.get(0);
                    latlng[0] = address.getLatitude();
                    latlng[1] = address.getLongitude();

                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    latlng = new JoysaleApplication.GetLatLngFromString(this).execute(address.getText().toString().trim()).get();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (ExecutionException e1) {
                    e1.printStackTrace();
                }
            }

        } else {
            try {
                latlng = new JoysaleApplication.GetLatLngFromString(this).execute(address.getText().toString().trim()).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return latlng;
    }

    private boolean checkPermissions(String[] permissionList, LocationActivity activity) {
        boolean isPermissionsGranted = false;
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                isPermissionsGranted = true;
            } else {
                isPermissionsGranted = false;
                break;
            }
        }
        return isPermissionsGranted;
    }

    private boolean shouldShowRationale(String[] permissions, LocationActivity activity) {
        boolean showRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationale = true;
            } else {
                showRationale = false;
                break;
            }
        }
        return showRationale;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.LOCATION_PERMISSION_CODE) {
            if (checkPermissions(permissions, LocationActivity.this)) {
                setMyLocation();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRationale(permissions, LocationActivity.this)) {
                        ActivityCompat.requestPermissions(LocationActivity.this, permissions, Constants.LOCATION_PERMISSION_CODE);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivityForResult(intent, 100);
                    }
                }
            }
        }
    }

}
