package com.app.lystiq;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.app.external.ExpandableHeightListView;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.app.utils.GetSet;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by hitasoft on 24/6/16.
 * <p>
 * This class is for Promote an Ad Type Promotion.
 */

public class Promotead extends Fragment implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    public static TextView payPromote;
    static RelativeLayout ad, main;
    static AVLoadingIndicatorView progress;
    static ScrollView scrollView;
    ImageView promote, tick1, tick2, tick3, tick4;
    ExpandableHeightListView promoteList;
    TextView adText, tagText, adText1, adText2, adText3, adText4, productType;
    View tagView;
    ProgressDialog dialog;

    public static PromoteAdapter adapter;
    Context context;

    /**
     * Declare Variables
     **/
    static final String TAG = "Promotead";
    private static final int DROP_IN_REQUEST = 100;
    String selectedId = "", selectedPrice = "";

    public Promotead() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_promote, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        promote = (ImageView) getView().findViewById(R.id.imageView);
        ad = (RelativeLayout) getView().findViewById(R.id.promotead);
        payPromote = (TextView) getView().findViewById(R.id.promote);
        adText = (TextView) getView().findViewById(R.id.adText);
        promoteList = (ExpandableHeightListView) getView().findViewById(R.id.promoteList);
        scrollView = (ScrollView) getView().findViewById(R.id.scrollView);
        progress = (AVLoadingIndicatorView) getView().findViewById(R.id.progress);
        main = (RelativeLayout) getView().findViewById(R.id.main);
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

        context = getActivity();

        ad.setVisibility(View.VISIBLE);
        adText.setVisibility(View.VISIBLE);

        promote.setImageResource(R.drawable.promote_bg);

        payPromote.setOnClickListener(this);
        promoteList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        promoteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedId = CreatePromote.promoteItems.get(position).get(Constants.TAG_ID);
                selectedPrice = CreatePromote.promoteItems.get(position).get(Constants.TAG_PRICE);
                adapter.notifyDataSetChanged();
            }
        });

        tick1.setColorFilter(getResources().getColor(R.color.colorPrimary));
        tick2.setColorFilter(getResources().getColor(R.color.colorPrimary));
        tick3.setColorFilter(getResources().getColor(R.color.colorPrimary));
        tick4.setColorFilter(getResources().getColor(R.color.colorPrimary));

        payPromote.setText(getString(R.string.pay_and_promote));
        adText.setText(getString(R.string.ads_des));
        tagText.setText(getString(R.string.promote_tag_features));
        adText1.setText(getString(R.string.promote_feature_list1));
        adText2.setText(getString(R.string.promote_feature_list2));
        adText3.setText(getString(R.string.promote_feature_list3));
        adText4.setText(getString(R.string.promote_feature_list4));
        productType.setText(getString(R.string.ad));

        tagText.setTextColor(getResources().getColor(R.color.colorPrimary));
        tagView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        productType.setBackgroundDrawable(getResources().getDrawable(R.drawable.adbg));
        setAdapter();
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

    //To set Adapter in promoteList
    private void setAdapter() {
        adapter = new PromoteAdapter(getActivity(), CreatePromote.promoteItems);
        promoteList.setAdapter(adapter);
        promoteList.setExpanded(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "resultCode=" + resultCode);

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
        StringRequest req = new StringRequest(Request.Method.POST, Constants.API_PAY_FOR_PROMOTION, new Response.Listener<String>() {
            @Override
            public void onResponse(String res) {
                try {
                    dialog.dismiss();
                    JSONObject json = new JSONObject(res);

                    if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                        DetailActivity.fromEdit = true;
                        ((Activity) getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showDialog(Promotead.this.getActivity(), getString(R.string.success), getString(R.string.your_promotion_was_activated_successfully));
                            }
                        });
                    } else {
                        ((Activity) getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JoysaleApplication.dialog(getActivity(), getString(R.string.alert), getString(R.string.somethingwrong));
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
                map.put(Constants.TAG_PROMOTION_ID, selectedId);
                map.put(Constants.TAG_CURRENCY_CODE, CreatePromote.currencyCode);
                map.put(Constants.TAG_PAY_NONCE, payNounce);
                Log.i(TAG, "payForPromotion getParams: "+map);
                return map;
            }
        };
        JoysaleApplication.getInstance().addToRequestQueue(req);
    }

    private void showDialog(final Context context, String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Activity) context).finish();
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
     * Adapter for Ad Promote
     **/

    public class PromoteAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> data;
        ViewHolder holder = null;
        Context mContext;

        PromoteAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            mContext = context;
            data = Items;
        }

        @Override
        public int getCount() {
            return data.size();
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
                convertView = inflater.inflate(R.layout.create_promote_item,
                        parent, false);
                holder = new ViewHolder();

                holder.promotionName = (TextView) convertView.findViewById(R.id.promotionName);
                holder.promotionPrice = (TextView) convertView.findViewById(R.id.promotionPrice);
                holder.promotionDays = (TextView) convertView.findViewById(R.id.promotionDays);
                holder.viewLine = (View) convertView.findViewById(R.id.view);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                if (selectedId.equals(data.get(position).get(Constants.TAG_ID))) {
                    holder.viewLine.setVisibility(View.VISIBLE);
                    holder.promotionPrice.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    holder.viewLine.setVisibility(View.INVISIBLE);
                    holder.promotionPrice.setTextColor(getResources().getColor(R.color.primaryText));
                }

                holder.promotionName.setText(data.get(position).get(Constants.TAG_NAME));
                holder.promotionDays.setText(data.get(position).get(Constants.TAG_DAYS) + " " + getString(R.string.days));
                holder.promotionPrice.setText(CreatePromote.currencySymbol + " " +
                        String.format("%.2f", Float.parseFloat(data.get(position).get(Constants.TAG_PRICE))));

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertView;
        }

        class ViewHolder {
            TextView promotionName, promotionPrice, promotionDays;
            View viewLine;
        }
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
                                .setTotalPrice(selectedPrice)
                                .addLineItem(LineItem.newBuilder()
                                        .setCurrencyCode(CreatePromote.currencySymbol)
                                        .setDescription("Promotion")
                                        .setQuantity("1")
                                        .setUnitPrice(selectedPrice)
                                        .setTotalPrice(selectedPrice)
                                        .build())
                                .build();

                        DropInRequest dropInRequest = new DropInRequest()
                                .tokenizationKey(CreatePromote.clientToken)
                                .amount(selectedPrice)
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
     * Function for OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.promote:
                if (selectedId.equals("")) {
                    JoysaleApplication.dialog(getActivity(), getResources().getString(R.string.alert), getString(R.string.please_select_promotion));
                } else {
                    getClientToken();
                }
                break;
        }
    }
}