package com.app.lystiq;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.LineItem;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by hitasoft on 24/6/16.
 * <p>
 * This class is for Promote an Urgent Type Promotion.
 */

public class PromoteUrgent extends Fragment implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    static RelativeLayout ad, main;
    static AVLoadingIndicatorView progress;
    static ScrollView scrollView;
    public static TextView pay,adText;
    ImageView promote, tick1, tick2, tick3, tick4;
    TextView tagText, adText1, adText2, adText3, adText4, productType;
    View tagView;
    ProgressDialog dialog;

    /**
     * Declare Variables
     **/
    static final String TAG = "PromoteUrgent";
    private static final int DROP_IN_REQUEST = 100;


    public PromoteUrgent() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_promote, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        promote = (ImageView) getView().findViewById(R.id.imageView);
        ad = (RelativeLayout) getView().findViewById(R.id.promotead);
        pay = (TextView) getView().findViewById(R.id.promote);
        scrollView = (ScrollView) getView().findViewById(R.id.scrollView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        main = (RelativeLayout) getView().findViewById(R.id.main);
        adText = (TextView) getView().findViewById(R.id.adText);
        tick1 = (ImageView) getView().findViewById(R.id.tick1);
        tick2 = (ImageView) getView().findViewById(R.id.tick2);
        tick3 = (ImageView) getView().findViewById(R.id.tick3);
        tick4 = (ImageView) getView().findViewById(R.id.tick4);
        tagText = (TextView) getView().findViewById(R.id.tagText);
        adText1 = (TextView) getView().findViewById(R.id.adText1);
        adText2 = (TextView) getView().findViewById(R.id.adText2);
        adText3 = (TextView) getView().findViewById(R.id.adText3);
        adText4 = (TextView) getView().findViewById(R.id.adText4);
        tagView = (View) getView().findViewById(R.id.tagView);
        productType = (TextView) getView().findViewById(R.id.productType);

        ad.setVisibility(View.GONE);

        promote.setImageResource(R.drawable.promote_bg);

        tick1.setColorFilter(getResources().getColor(R.color.red));
        tick2.setColorFilter(getResources().getColor(R.color.red));
        tick3.setColorFilter(getResources().getColor(R.color.red));
        tick4.setColorFilter(getResources().getColor(R.color.red));

        pay.setText(getString(R.string.pay_and_highlight));
        tagText.setText(getString(R.string.urgent_tag_features));
        adText1.setText(getString(R.string.urgent_feature_list1));
        adText2.setText(getString(R.string.urgent_feature_list2));
        adText3.setText(getString(R.string.urgent_feature_list3));
        adText4.setText(getString(R.string.urgent_feature_list4));
        productType.setText(getString(R.string.urgent));

        tagText.setTextColor(getResources().getColor(R.color.red));
        tagView.setBackgroundColor(getResources().getColor(R.color.red));

        productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.urgentbg));

        if (!CreatePromote.urgent.equals("")) {
           // String price = CreatePromote.currencySymbol + "  " + String.format("%.2f", Float.parseFloat(CreatePromote.urgent));

            String price= String.format("%.2f", Float.parseFloat(CreatePromote.urgent)+" "+CreatePromote.currencySymbol);

            adText.setText(Html.fromHtml(getString(R.string.urgent_des) + " <font color='" + String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary))) + "'>" + price + "</font>"));
        }

        pay.setOnClickListener(this);

        dialog = new ProgressDialog(getActivity(),R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(getActivity()).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "resultCode==" + resultCode);
        if (requestCode == DROP_IN_REQUEST) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                String paymentMethodNonce = result.getPaymentMethodNonce().getNonce();
                Log.v(TAG, "paymentMethodNonce=" + paymentMethodNonce);
                // send paymentMethodNonce to your server
                dialog.show();
                payForPromotion(paymentMethodNonce);
            } else if (resultCode != RESULT_CANCELED) {
                JoysaleApplication.dialog(getActivity(), getString(R.string.alert), getString(R.string.payment_error));
            } else {
                Toast.makeText(getActivity(), getString(R.string.payment_cancelled), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Function for send paid promotion details to server
     **/

    private void payForPromotion(final String payNounce) {
        StringRequest req = new StringRequest(Request.Method.POST,Constants.API_PAY_FOR_PROMOTION, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonresponse) {
                try {
                    dialog.dismiss();

                    Log.v(TAG, "responsePayment=" + jsonresponse);
                    JSONObject json = new JSONObject(jsonresponse);
                    String response = DefensiveClass.optString(json, Constants.TAG_STATUS);

                    if (response.equalsIgnoreCase("true")) {
                        DetailActivity.fromEdit = true;
                        ((Activity) getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showDialog(getString(R.string.success), getString(R.string.your_promotion_was_activated_successfully));
                            }
                        });
                    } else {
                        ((Activity) getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JoysaleApplication.dialog(getActivity(), getResources().getString(R.string.alert), getString(R.string.somethingwrong));
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
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
                map.put(Constants.TAG_ITEM_ID, CreatePromote.itemId);
                map.put(Constants.TAG_PROMOTION_ID, "0");
                map.put(Constants.TAG_CURRENCY_CODE, CreatePromote.currencyCode);
                map.put(Constants.TAG_PAY_NONCE, payNounce);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    private void showDialog(String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(PromoteUrgent.this.getActivity()).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Activity) getActivity()).finish();
                Intent in = new Intent(getActivity(), Profile.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                in.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                startActivity(in);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    /**
     * Function for get client token to send braintree
     **/

    private void getClientToken(){
        final ProgressDialog dialog = new ProgressDialog(getActivity(),R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(getActivity()).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }
        dialog.show();
        StringRequest req = new StringRequest(Request.Method.POST,Constants.API_GET_CLIENTTOKEN, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                Log.v(TAG, "getClientTokenRes=" + res);
                if (dialog.isShowing())
                    dialog.dismiss();
                try {
                    JSONObject json = new JSONObject(res);
                    String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
                    if (response.equalsIgnoreCase("true")) {
                        CreatePromote.clientToken = DefensiveClass.optString(json, Constants.TAG_TOKEN);
                        Cart cart = Cart.newBuilder()
                                .setCurrencyCode(CreatePromote.currencySymbol)
                                .setTotalPrice(CreatePromote.urgent)
                                .addLineItem(LineItem.newBuilder()
                                        .setCurrencyCode(CreatePromote.currencySymbol)
                                        .setDescription("Promotion")
                                        .setQuantity("1")
                                        .setUnitPrice(CreatePromote.urgent)
                                        .setTotalPrice(CreatePromote.urgent)
                                        .build())
                                .build();

                        DropInRequest dropInRequest = new DropInRequest()
                                .tokenizationKey(CreatePromote.clientToken)
                                .amount(CreatePromote.urgent)
                                .androidPayCart(cart);
                        startActivityForResult(dropInRequest.getIntent(getActivity()), DROP_IN_REQUEST);
                    } else {
                        JoysaleApplication.dialog(getActivity(), getString(R.string.alert), getString(R.string.somethingwrong));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (dialog.isShowing())
                    dialog.dismiss();
                JoysaleApplication.dialog(getActivity(), getString(R.string.alert), getString(R.string.somethingwrong));
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
                map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
                map.put("currency_code", CreatePromote.currencyCode);
                Log.v("map", "map=" + map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * Function for Onclick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.promote:
                getClientToken();
                break;
        }
    }
}
