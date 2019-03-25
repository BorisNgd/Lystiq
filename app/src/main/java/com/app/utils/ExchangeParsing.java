package com.app.utils;

import android.content.Context;
import android.text.Html;

import com.app.lystiq.JoysaleApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hitasoft on 14/7/16.
 * <p>
 * This class is for User Exchange's  Json Parsing.
 */

public class ExchangeParsing {
    Context context;

    // constructor
    public ExchangeParsing(Context ctx) {
        this.context = ctx;
    }

    public ArrayList<HashMap<String, String>> parsing(String jsonString) {
        ArrayList<HashMap<String, String>> exchangeAry = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject json = new JSONObject(jsonString);

            if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                JSONObject result = json
                        .optJSONObject(Constants.TAG_RESULT);
                if (result != null) {
                    JSONArray exchange = result.getJSONArray(Constants.TAG_EXCHANGE);
                    if (exchange != null) {
                        for (int i = 0; i < exchange.length(); i++) {
                            HashMap<String, String> map = new HashMap<String, String>();
                            JSONObject temp = exchange.getJSONObject(i);
                            if (temp.getJSONObject(Constants.TAG_MYPRODUCT) != null) {
                                map.put("m" + Constants.TAG_ITEMID, DefensiveClass.optString(temp.getJSONObject(Constants.TAG_MYPRODUCT), Constants.TAG_ITEMID));
                                map.put("m" + Constants.TAG_ITEMIMAGE, DefensiveClass.optString(temp.getJSONObject(Constants.TAG_MYPRODUCT), Constants.TAG_ITEMIMAGE).replace("100x100", "200"));
                                map.put("m" + Constants.TAG_ITEM_NAME, String.valueOf(Html.fromHtml(DefensiveClass.optString(temp.getJSONObject(Constants.TAG_MYPRODUCT), Constants.TAG_ITEM_NAME))));
                            }

                            JSONObject eproduct = temp.getJSONObject(Constants.TAG_EXCHANGEPRODUCT);
                            if (eproduct != null) {
                                map.put("e" + Constants.TAG_ITEMID, DefensiveClass.optString(eproduct, Constants.TAG_ITEMID));
                                map.put("e" + Constants.TAG_ITEMIMAGE, DefensiveClass.optString(eproduct, Constants.TAG_ITEMIMAGE).replace("100x100", "200"));
                                map.put("e" + Constants.TAG_ITEM_NAME, String.valueOf(Html.fromHtml(DefensiveClass.optString(eproduct, Constants.TAG_ITEM_NAME))));
                            }

                            map.put(Constants.TAG_TYPE, DefensiveClass.optString(temp, Constants.TAG_TYPE));
                            map.put(Constants.TAG_EXCHANGEID, DefensiveClass.optString(temp, Constants.TAG_EXCHANGEID));
                            map.put(Constants.TAG_STATUS, DefensiveClass.optString(temp, Constants.TAG_STATUS));
                            map.put(Constants.TAG_REQUEST_BY_ME, DefensiveClass.optString(temp, Constants.TAG_REQUEST_BY_ME));
                            map.put(Constants.TAG_EXCHANGETIME, DefensiveClass.optString(temp, Constants.TAG_EXCHANGETIME));
                            map.put(Constants.TAG_EXCHANGERNAME, DefensiveClass.optString(temp, Constants.TAG_EXCHANGERNAME));
                            map.put(Constants.TAG_EXCHANGERUSERNAME, DefensiveClass.optString(temp, Constants.TAG_EXCHANGERUSERNAME));
                            map.put(Constants.TAG_EXCHANGERID, DefensiveClass.optString(temp, Constants.TAG_EXCHANGERID));
                            map.put(Constants.TAG_EXCHANGERIMG, DefensiveClass.optString(temp, Constants.TAG_EXCHANGERIMG));

                            exchangeAry.add(map);
                        }
                    }
                }
            } else if (DefensiveClass.optString(json, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
                JoysaleApplication.disabledialog(context, json.optString(Constants.TAG_MESSAGE), GetSet.getUserId());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exchangeAry;
    }
}
