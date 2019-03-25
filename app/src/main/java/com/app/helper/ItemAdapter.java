package com.app.helper;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.external.BadgeView;
import com.app.lystiq.FragmentMainActivity;
import com.app.lystiq.R;
import com.app.utils.GetSet;

public class ItemAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] Ids;
    private final int rowResourceId;

    public ItemAdapter(Context context, int textViewResourceId, String[] objects) {

        super(context, textViewResourceId, objects);

        this.context = context;
        this.Ids = objects;
        this.rowResourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(rowResourceId, parent, false);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        TextView textView = (TextView) rowView.findViewById(R.id.textView);
        ImageView adIcon = (ImageView) rowView.findViewById(R.id.adIcon);
        ImageView badgeIcon = (ImageView) rowView.findViewById(R.id.badgeIcon);

        RelativeLayout menuLay = (RelativeLayout) rowView.findViewById(R.id.menuLay);

		/*if (JoysaleApplication.isRTL(context)){
            menuLay.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
		} else {
			menuLay.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
		}*/

        int id = Integer.parseInt(Ids[position]);
        //Log.v("id",""+id);

        //String name = context.getResources().getString(Model.GetbyId(id).Name).toString();
        String name = Model.GetbyId(id).name;
        textView.setText(name);
        // get input stream
        imageView.setImageResource(Model.GetbyId(id).IconFile);
        if (name.equals(context.getResources().getString(R.string.my_promotions))) {
            adIcon.setVisibility(View.GONE);
            badgeIcon.setVisibility(View.GONE);
        } else if (name.equals(context.getResources().getString(R.string.chat))) {
            adIcon.setVisibility(View.GONE);
            badgeIcon.setVisibility(View.INVISIBLE);
            if (GetSet.isLogged() && !FragmentMainActivity.chatCount.equals("0") && !FragmentMainActivity.chatCount.equals("")) {
                BadgeView chatBadge = new BadgeView(context, badgeIcon);
                chatBadge.setText(FragmentMainActivity.chatCount);
                chatBadge.setBadgePosition(BadgeView.POSITION_CENTER);
                chatBadge.setBadgeMargin(7);
                chatBadge.setTextSize(13);
                chatBadge.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.chat_badge_bg));
                chatBadge.setGravity(Gravity.CENTER);
                chatBadge.show();
            }
        } else {
            adIcon.setVisibility(View.GONE);
            badgeIcon.setVisibility(View.GONE);
        }
        return rowView;

    }

}