package com.app.utils;

import android.content.Context;
import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 14/9/16.
 **/

public class OrderParsing {
    Context context;

    public OrderParsing(Context ctx) {
        this.context = ctx;
    }

    public ArrayList<HashMap<String, String>> parsing(String jsonString) {
        ArrayList<HashMap<String, String>> orderary = new ArrayList<HashMap<String, String>>();
        try {
            Log.v("response", "response" + jsonString);
            JSONObject json = new JSONObject(jsonString);
            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
            String total = "", shipping_cost = "", cSymbol = "", unitprice = "", price = "", quantity = "", buyer_img = "", seller_img = "", buyer_id = "", buyer_username = "", buyer_name = "", seller_id = "", seller_username = "", seller_name = "", itemname = "", itemid = "", itemimage = "", name = "", nickname = "", country = "", state = "", address1 = "", address2 = "", city = "", phone = "", zipcode = "", countrycode = "", id = "", shippingdate = "", couriername = "", courierservice = "", trackingid = "", notes = "", buyer_email = "";
            if (response.equalsIgnoreCase("true")) {
                JSONArray result = json
                        .getJSONArray(Constants.TAG_RESULT);
                for (int i = 0; i < result.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    JSONObject temp = result.getJSONObject(i);

                    map.put(Constants.TAG_ORDER_ID, DefensiveClass.optString(temp, Constants.TAG_ORDER_ID));
                    map.put(Constants.TAG_PAYMENT_TYPE, DefensiveClass.optString(temp, Constants.TAG_PAYMENT_TYPE));
                    map.put(Constants.TAG_SALEDATE, DefensiveClass.optString(temp, Constants.TAG_SALEDATE));
                    map.put(Constants.TAG_STATUS, DefensiveClass.optString(temp, Constants.TAG_STATUS));
                    map.put(Constants.TAG_DELIVERY_DATE, DefensiveClass.optString(temp, Constants.TAG_DELIVERY_DATE));
                    map.put(Constants.TAG_TRANSACTION_ID, DefensiveClass.optString(temp, Constants.TAG_TRANSACTION_ID));
                    map.put(Constants.TAG_CLAIM, DefensiveClass.optString(temp, Constants.TAG_CLAIM));
                    map.put(Constants.TAG_CANCEL, DefensiveClass.optString(temp, Constants.TAG_CANCEL));
                    map.put(Constants.TAG_INVOICE, DefensiveClass.optString(temp, Constants.TAG_INVOICE));
                    map.put(Constants.TAG_REVIEW_ID, DefensiveClass.optInt(temp, Constants.TAG_REVIEW_ID));
                    map.put(Constants.TAG_RATING, DefensiveClass.optInt(temp, Constants.TAG_RATING));
                    map.put(Constants.TAG_REVIEW_TITLE, DefensiveClass.optString(temp, Constants.TAG_REVIEW_TITLE));
                    map.put(Constants.TAG_REVIEW_DES, DefensiveClass.optString(temp, Constants.TAG_REVIEW_DES));
                    map.put(Constants.TAG_CREATED_DATE, DefensiveClass.optString(temp, Constants.TAG_CREATED_DATE));

                    try {
                        JSONObject shippingaddress = temp.optJSONObject(Constants.TAG_SHIPPING);
                        if (shippingaddress != null) {
                            name = DefensiveClass.optString(shippingaddress, Constants.TAG_NAME);
                            nickname = DefensiveClass.optString(shippingaddress, Constants.TAG_NICKNAME);
                            country = DefensiveClass.optString(shippingaddress, Constants.TAG_COUNTRY);
                            state = DefensiveClass.optString(shippingaddress, Constants.TAG_STATE);
                            address1 = DefensiveClass.optString(shippingaddress, Constants.TAG_ADDRESS1);
                            address2 = DefensiveClass.optString(shippingaddress, Constants.TAG_ADDRESS2);
                            city = DefensiveClass.optString(shippingaddress, Constants.TAG_CITY);
                            phone = DefensiveClass.optString(shippingaddress, Constants.TAG_PHONE);
                            zipcode = DefensiveClass.optString(shippingaddress, Constants.TAG_ZIPCODE);
                            countrycode = DefensiveClass.optString(shippingaddress, Constants.TAG_COUNTRYCODE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        JSONObject trackingdetails = temp.optJSONObject(Constants.TAG_TRACKING_DETAILS);
                        if (trackingdetails != null) {
                            id = DefensiveClass.optString(trackingdetails, Constants.TAG_ID);
                            shippingdate = DefensiveClass.optString(trackingdetails, Constants.TAG_SHIPPING_DATE);
                            couriername = DefensiveClass.optString(trackingdetails, Constants.TAG_COURIER_NAME);
                            courierservice = DefensiveClass.optString(trackingdetails, Constants.TAG_COURIER_SERVICE);
                            trackingid = DefensiveClass.optString(trackingdetails, Constants.TAG_TRACK_ID);
                            notes = DefensiveClass.optString(trackingdetails, Constants.TAG_NOTES);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        JSONObject orderitems = temp.optJSONObject(Constants.TAG_ORDERITEMS);
                        if (orderitems != null) {
                            itemimage = DefensiveClass.optString(orderitems, Constants.TAG_ORDERIMG);
                            itemid = DefensiveClass.optString(orderitems, Constants.TAG_ITEMID);
                            itemname = String.valueOf(Html.fromHtml(DefensiveClass.optString(orderitems, Constants.TAG_ITEMNAME)));
                            seller_name = DefensiveClass.optString(orderitems, Constants.TAG_SELLERNAME);
                            seller_username = DefensiveClass.optString(orderitems, Constants.TAG_SELLER_USERNAME);
                            seller_id = DefensiveClass.optString(orderitems, Constants.TAG_SELLERID);
                            buyer_id = DefensiveClass.optString(orderitems, Constants.TAG_BUYERID);
                            buyer_name = DefensiveClass.optString(orderitems, Constants.TAG_BUYERNAME);
                            buyer_username = DefensiveClass.optString(orderitems, Constants.TAG_BUYER_USERNAME);
                            seller_img = DefensiveClass.optString(orderitems, Constants.TAG_SELLERIMG);
                            buyer_img = DefensiveClass.optString(orderitems, Constants.TAG_BUYERIMG);
                            quantity = DefensiveClass.optString(orderitems, Constants.TAG_QUANTITY);
                            price = DefensiveClass.optString(orderitems, Constants.TAG_PRICE);
                            unitprice = DefensiveClass.optString(orderitems, Constants.TAG_UPRICE);
                            cSymbol = DefensiveClass.optString(orderitems, Constants.TAG_SYMBOL);
                            shipping_cost = DefensiveClass.optString(orderitems, Constants.TAG_SHIPPING_COST);
                            total = DefensiveClass.optString(orderitems, Constants.TAG_TOTAL);
                            buyer_email = DefensiveClass.optString(orderitems, Constants.TAG_BUYER_EMAIL);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    map.put(Constants.TAG_NAME, name);
                    map.put(Constants.TAG_NICKNAME, nickname);
                    map.put(Constants.TAG_COUNTRY, country);
                    map.put(Constants.TAG_STATE, state);
                    map.put(Constants.TAG_ADDRESS1, address1);
                    map.put(Constants.TAG_ADDRESS2, address2);
                    map.put(Constants.TAG_CITY, city);
                    map.put(Constants.TAG_PHONE, phone);
                    map.put(Constants.TAG_ZIPCODE, zipcode);
                    map.put(Constants.TAG_COUNTRYCODE, countrycode);
                    map.put(Constants.TAG_ID, id);
                    map.put(Constants.TAG_SHIPPING_DATE, shippingdate);
                    map.put(Constants.TAG_COURIER_NAME, couriername);
                    map.put(Constants.TAG_COURIER_SERVICE, courierservice);
                    map.put(Constants.TAG_TRACK_ID, trackingid);
                    map.put(Constants.TAG_NOTES, notes);
                    map.put(Constants.TAG_ORDERIMG, itemimage);
                    map.put(Constants.TAG_ITEMNAME, itemname);
                    map.put(Constants.TAG_ITEMID, itemid);
                    map.put(Constants.TAG_SELLERNAME, seller_name);
                    map.put(Constants.TAG_SELLER_USERNAME, seller_username);
                    map.put(Constants.TAG_SELLERID, seller_id);
                    map.put(Constants.TAG_BUYERID, buyer_id);
                    map.put(Constants.TAG_BUYERNAME, buyer_name);
                    map.put(Constants.TAG_BUYER_USERNAME, buyer_username);
                    map.put(Constants.TAG_SELLERIMG, seller_img);
                    map.put(Constants.TAG_BUYERIMG, buyer_img);
                    map.put(Constants.TAG_QUANTITY, quantity);
                    map.put(Constants.TAG_PRICE, price);
                    map.put(Constants.TAG_UPRICE, unitprice);
                    map.put(Constants.TAG_SYMBOL, cSymbol);
                    map.put(Constants.TAG_SHIPPING_COST, shipping_cost);
                    map.put(Constants.TAG_TOTAL, total);
                    map.put(Constants.TAG_BUYER_EMAIL, buyer_email);
                    orderary.add(map);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderary;
    }

    public ArrayList<HashMap<String, String>> gettracking(String jsonString) {
        ArrayList<HashMap<String, String>> orderary = new ArrayList<HashMap<String, String>>();
        try {
            Log.v("response", "response" + jsonString);
            JSONObject json = new JSONObject(jsonString);
            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
            String id = "", shippingdate = "", couriername = "", courierservice = "", trackingid = "", notes = "";
            if (response.equalsIgnoreCase("true")) {
                JSONArray result = json
                        .getJSONArray(Constants.TAG_RESULT);
                for (int i = 0; i < result.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    JSONObject temp = result.getJSONObject(i);

                    try {
                        JSONObject trackingdetails = temp.getJSONObject(Constants.TAG_TRACKING_DETAILS);
                        if (trackingdetails != null) {
                            id = DefensiveClass.optString(trackingdetails, Constants.TAG_ID);
                            shippingdate = DefensiveClass.optString(trackingdetails, Constants.TAG_SHIPPING_DATE);
                            couriername = DefensiveClass.optString(trackingdetails, Constants.TAG_COURIER_NAME);
                            courierservice = DefensiveClass.optString(trackingdetails, Constants.TAG_COURIER_SERVICE);
                            trackingid = DefensiveClass.optString(trackingdetails, Constants.TAG_TRACK_ID);
                            notes = DefensiveClass.optString(trackingdetails, Constants.TAG_NOTES);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    map.put(Constants.TAG_ID, id);
                    map.put(Constants.TAG_SHIPPING_DATE, shippingdate);
                    map.put(Constants.TAG_COURIER_NAME, couriername);
                    map.put(Constants.TAG_COURIER_SERVICE, courierservice);
                    map.put(Constants.TAG_TRACK_ID, trackingid);
                    map.put(Constants.TAG_NOTES, notes);

                    orderary.add(map);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderary;
    }

}
