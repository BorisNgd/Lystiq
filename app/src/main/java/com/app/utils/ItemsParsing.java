package com.app.utils;
/****************
 *
 * @author 'Hitasoft Technologies'
 *
 * Description:
 * This class is common for parsing items and defensive coding also included.
 *
 * Revision History:
 * Version 1.0 - Initial Version
 *
 *****************/

import android.content.Context;
import android.graphics.Color;
import android.text.Html;

import com.app.lystiq.JoysaleApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ItemsParsing {

    String from = "", userId = "";
    Context context;

    // constructor
    public ItemsParsing(Context ctx) {
        this.context = ctx;
    }

    public ItemsParsing(String from, Context ctx, String userId) {
        this.from = from;
        this.context = ctx;
        this.userId = userId;
    }

    public ItemsParsing(Context context, String userId) {
        this.context = context;
        this.userId = userId;
    }

    public ArrayList<HashMap<String, String>> parsing(String jsonString) {
        ArrayList<HashMap<String, String>> homePageItems = new ArrayList<HashMap<String, String>>();
        JSONArray items;
        HashMap<String, String> map;

        try {
            JSONObject json = new JSONObject(jsonString);
            String response = DefensiveClass.optString(json, Constants.TAG_STATUS);
            if (response.equalsIgnoreCase("true")) {

                JSONObject result = json
                        .optJSONObject(Constants.TAG_RESULT);
                if (result != null) {
                    items = result.optJSONArray(Constants.TAG_ITEMS);
                    if (items != null) {
                        for (int i = 0; i < items.length(); i++) {
                            map = new HashMap<String, String>();
                            JSONObject temp = items.getJSONObject(i);
                            String currencysym = "";
                            if (DefensiveClass.optCurrency(temp, Constants.TAG_CURRENCY_CODE).contains("-")) {
                                String cur[] = DefensiveClass.optCurrency(temp, Constants.TAG_CURRENCY_CODE).split("-");
                                currencysym = cur[0];
                            } else {
                                currencysym = DefensiveClass.optCurrency(temp, Constants.TAG_CURRENCY_CODE);
                            }

                            JSONArray shipdetail = temp.optJSONArray(Constants.TAG_SHIPPING_DETAIL);
                            if (shipdetail == null) {
                                map.put(Constants.TAG_SHIPPING_DETAIL, "");
                            } else {
                                map.put(Constants.TAG_SHIPPING_DETAIL, shipdetail.toString());
                            }

                            JSONArray size = temp.optJSONArray(Constants.TAG_SIZE);
                            if (size == null) {
                                map.put(Constants.TAG_SIZE, "");
                            } else {
                                map.put(Constants.TAG_SIZE, size.toString());
                            }

                            JSONArray photos = temp.optJSONArray(Constants.TAG_PHOTOS);
                            if (photos == null) {
                                map.put(Constants.TAG_PHOTOS, "");
                            } else {
                                map.put(Constants.TAG_PHOTOS, photos.toString());
                                for (int j = 0; j < photos.length(); j++) {
                                    JSONObject jph = photos.optJSONObject(j);
                                    if (j == 0) {
                                        map.put(Constants.TAG_WIDTH, DefensiveClass.optString(jph, Constants.TAG_WIDTH));
                                        map.put(Constants.TAG_HEIGHT, DefensiveClass.optString(jph, Constants.TAG_HEIGHT));
                                        map.put(Constants.TAG_ITEM_URL_350, DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_350));
                                        map.put(Constants.TAG_ITEM_URL_ORG, DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_ORG));
                                    }
                                }
                            }

                            Random rnd = new Random();
                            int color = Color.argb(25, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                            map.put(Constants.TAG_ID, DefensiveClass.optInt(temp, Constants.TAG_ID));
                            map.put(Constants.TAG_TITLE, String.valueOf(Html.fromHtml(DefensiveClass.optString(temp, Constants.TAG_TITLE))));
                            map.put(Constants.TAG_ITEM_DES, DefensiveClass.optString(temp, Constants.TAG_ITEM_DES));
                            map.put(Constants.TAG_ITEM_CONDITION, DefensiveClass.optString(temp, Constants.TAG_ITEM_CONDITION));
                            map.put(Constants.TAG_PRICE, DefensiveClass.optInt(temp, Constants.TAG_PRICE));
                            map.put(Constants.TAG_QUANTITY, DefensiveClass.optInt(temp, Constants.TAG_QUANTITY));
                            map.put(Constants.TAG_ITEM_STATUS, DefensiveClass.optString(temp, Constants.TAG_ITEM_STATUS));
                            map.put(Constants.TAG_SELLERID, DefensiveClass.optInt(temp, Constants.TAG_SELLERID));
                            map.put(Constants.TAG_SELLERNAME, DefensiveClass.optString(temp, Constants.TAG_SELLERNAME));
                            map.put(Constants.TAG_SELLERIMG, DefensiveClass.optString(temp, Constants.TAG_SELLERIMG));
                            map.put(Constants.TAG_CURRENCY_CODE, DefensiveClass.optCurrency(temp, Constants.TAG_CURRENCY_CODE));
                            map.put(Constants.TAG_PROURL, DefensiveClass.optString(temp, Constants.TAG_PROURL));
                            map.put(Constants.TAG_LIKECOUNT, DefensiveClass.optInt(temp, Constants.TAG_LIKECOUNT));
                            map.put(Constants.TAG_COMMENTCOUNT, DefensiveClass.optInt(temp, Constants.TAG_COMMENTCOUNT));
                            map.put(Constants.TAG_VIEWCOUNT, DefensiveClass.optInt(temp, Constants.TAG_VIEWCOUNT));
                            map.put(Constants.TAG_LIKED, DefensiveClass.optString(temp, Constants.TAG_LIKED));
                            map.put(Constants.TAG_POSTED_TIME, DefensiveClass.optString(temp, Constants.TAG_POSTED_TIME));
                            map.put(Constants.TAG_SERVER_TIME,DefensiveClass.optString(temp,Constants.TAG_SERVER_TIME));
                            map.put(Constants.TAG_LATITUDE, DefensiveClass.optString(temp, Constants.TAG_LATITUDE));
                            map.put(Constants.TAG_LONGITUDE, DefensiveClass.optString(temp, Constants.TAG_LONGITUDE));
                            map.put(Constants.TAG_LOCATION, DefensiveClass.optString(temp, Constants.TAG_LOCATION));
                            map.put(Constants.TAG_BEST_OFFER, DefensiveClass.optString(temp, Constants.TAG_BEST_OFFER));
                            map.put(Constants.TAG_BUYTYPE, DefensiveClass.optString(temp, Constants.TAG_BUYTYPE));
                            map.put(Constants.TAG_CATEGORYNAME, DefensiveClass.optString(temp, Constants.TAG_CATEGORYNAME));
                            map.put(Constants.TAG_CATEGORYID, DefensiveClass.optString(temp, Constants.TAG_CATEGORYID));
                            map.put(Constants.TAG_SUBCATEGORYNAME, DefensiveClass.optString(temp,Constants.TAG_SUBCATEGORYNAME));
                            map.put(Constants.TAG_SUBCATEGORYID, DefensiveClass.optString(temp, Constants.TAG_SUBCATEGORYID));
                            map.put(Constants.TAG_PAYPALID, DefensiveClass.optString(temp, Constants.TAG_PAYPALID));
                            map.put(Constants.TAG_SHIPPING_TIME, DefensiveClass.optString(temp, Constants.TAG_SHIPPING_TIME));
                            map.put(Constants.TAG_COLOR, Integer.toString(color));
                            map.put(Constants.TAG_REPORT, DefensiveClass.optString(temp, Constants.TAG_REPORT));
                            map.put(Constants.TAG_PROMOTION_TYPE, DefensiveClass.optString(temp, Constants.TAG_PROMOTION_TYPE));
                            map.put(Constants.TAG_EXCHANGE_BUY, DefensiveClass.optString(temp, Constants.TAG_EXCHANGE_BUY));
                            map.put(Constants.TAG_MAKE_OFFER, DefensiveClass.optString(temp, Constants.TAG_MAKE_OFFER));
                            map.put(Constants.TAG_SELLER_USERNAME, DefensiveClass.optString(temp, Constants.TAG_SELLER_USERNAME));
                            map.put(Constants.TAG_FACEBOOK_VERIFICATION, DefensiveClass.optString(temp, Constants.TAG_FACEBOOK_VERIFICATION));
                            map.put(Constants.TAG_MOBILE_VERIFICATION, DefensiveClass.optString(temp, Constants.TAG_MOBILE_VERIFICATION));
                            map.put(Constants.TAG_EMAIL_VERIFICATION, DefensiveClass.optString(temp, Constants.TAG_EMAIL_VERIFICATION));
                            map.put(Constants.TAG_COUNTRYID, DefensiveClass.optString(temp, Constants.TAG_COUNTRYID));
                            map.put(Constants.TAG_INSTANT_BUY, DefensiveClass.optString(temp, Constants.TAG_INSTANT_BUY));
                            map.put(Constants.TAG_SHIPPING_COST, DefensiveClass.optInt(temp, Constants.TAG_SHIPPING_COST));
                            map.put(Constants.TAG_CURRENCY_SYM, currencysym);
                            map.put(Constants.TAG_MOBILE_NO, DefensiveClass.optString(temp, Constants.TAG_MOBILE_NO));
                            map.put(Constants.TAG_SHOW_SELLER_MOB, DefensiveClass.optString(temp, Constants.TAG_SHOW_SELLER_MOB));
                            map.put(Constants.TAG_ITEM_APPROVE, DefensiveClass.optString(temp, Constants.TAG_ITEM_APPROVE));
                            map.put(Constants.TAG_SELLER_RATING, DefensiveClass.optInt(temp, Constants.TAG_SELLER_RATING));
                            map.put(Constants.TAG_GIVING_AWAY,DefensiveClass.optString(temp, Constants.TAG_GIVING_AWAY));
                            map.put(Constants.TAG_LOCATION_ID1, DefensiveClass.optString(temp, Constants.TAG_LOCATION_ID1));

                            homePageItems.add(map);
                        }
                    }
                }
            } else if (response.equalsIgnoreCase("error")) {
                JoysaleApplication.disabledialog(context, json.optString(Constants.TAG_MESSAGE), userId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return homePageItems;
    }
}
