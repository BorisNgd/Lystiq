package com.app.helper;

import android.content.Context;

import com.app.lystiq.R;
import com.app.utils.Constants;

import java.util.ArrayList;

public class Model {

    public static ArrayList<Item> items;

    public static void LoadModel(Context ctx) {

        items = new ArrayList<Item>();

        items.add(new Item(1, R.drawable.s_cam, ctx.getString(R.string.sell_your_stuff)));
        items.add(new Item(2, R.drawable.s_message, ctx.getString(R.string.chat)));
        items.add(new Item(3, R.drawable.s_category, ctx.getString(R.string.categories)));
        items.add(new Item(4, R.drawable.s_user, ctx.getString(R.string.myprofile)));
        if (Constants.BUYNOW && Constants.EXCHANGE && Constants.PROMOTION) {
            items.add(new Item(5, R.drawable.s_mysale, ctx.getString(R.string.myorders_sales)));
            items.add(new Item(6, R.drawable.s_exchange, ctx.getString(R.string.myexchange)));
            items.add(new Item(7, R.drawable.s_promotion, ctx.getString(R.string.my_promotions)));
            items.add(new Item(8, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
            items.add(new Item(9, R.drawable.s_help, ctx.getString(R.string.help)));
        } else if (Constants.BUYNOW && Constants.EXCHANGE) {
            items.add(new Item(5, R.drawable.s_mysale, ctx.getString(R.string.myorders_sales)));
            items.add(new Item(6, R.drawable.s_exchange, ctx.getString(R.string.myexchange)));
            items.add(new Item(7, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
            items.add(new Item(8, R.drawable.s_help, ctx.getString(R.string.help)));
        } else if (Constants.BUYNOW && Constants.PROMOTION) {
            items.add(new Item(5, R.drawable.s_mysale, ctx.getString(R.string.myorders_sales)));
            items.add(new Item(6, R.drawable.s_promotion, ctx.getString(R.string.my_promotions)));
            items.add(new Item(7, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
            items.add(new Item(8, R.drawable.s_help, ctx.getString(R.string.help)));
        } else if (Constants.EXCHANGE && Constants.PROMOTION) {
            items.add(new Item(5, R.drawable.s_exchange, ctx.getString(R.string.myexchange)));
            items.add(new Item(6, R.drawable.s_promotion, ctx.getString(R.string.my_promotions)));
            items.add(new Item(7, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
            items.add(new Item(8, R.drawable.s_help, ctx.getString(R.string.help)));
        } else if (Constants.BUYNOW) {
            items.add(new Item(5, R.drawable.s_mysale, ctx.getString(R.string.myorders_sales)));
            items.add(new Item(6, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
            items.add(new Item(7, R.drawable.s_help, ctx.getString(R.string.help)));
        } else if (Constants.EXCHANGE) {
            items.add(new Item(5, R.drawable.s_exchange, ctx.getString(R.string.myexchange)));
            items.add(new Item(6, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
            items.add(new Item(7, R.drawable.s_help, ctx.getString(R.string.help)));
        } else if (Constants.PROMOTION) {
            items.add(new Item(5, R.drawable.s_promotion, ctx.getString(R.string.my_promotions)));
            items.add(new Item(6, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
            items.add(new Item(7, R.drawable.s_help, ctx.getString(R.string.help)));
        } else {
            items.add(new Item(5, R.drawable.s_invite, ctx.getString(R.string.invite_friends)));
            items.add(new Item(6, R.drawable.s_help, ctx.getString(R.string.help)));
        }
    }

    public static Item GetbyId(int id) {

        for (Item item : items) {
            if (item.Id == id) {
                return item;
            }
        }

        return null;
    }
}