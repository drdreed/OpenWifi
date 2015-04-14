package com.dustinmreed.openwifi;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import static com.dustinmreed.openwifi.Utilities.saveToPreferences;

/**
 * Created by Dustin on 4/13/2015.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {

    private static final String KEY_MAIN_LISTVIEW_FILTER = "main_listview_filter";

    List<Information> data = Collections.emptyList();
    Context context;
    private LayoutInflater inflater;
    public NavigationDrawerAdapter(Context context, List<Information> data) {
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.nav_drawer_custom_row, viewGroup, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int position) {
        Information current = data.get(position);
        viewHolder.title.setText(current.title);
        viewHolder.icon.setImageResource(current.iconId);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        ImageView icon;
        Intent intent;
        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.listText);
            icon = (ImageView) itemView.findViewById(R.id.listIcon);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (getAdapterPosition()) {
                case 0:
                    Intent intent = new Intent(context, MapActivity.class);
                    context.startActivity(intent);
                    break;
                case 2:
                    saveToPreferences(context, KEY_MAIN_LISTVIEW_FILTER, "library");
                    intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    break;
                case 3:
                    saveToPreferences(context, KEY_MAIN_LISTVIEW_FILTER, "communitycenter");
                    intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    break;
                case 4:
                    saveToPreferences(context, KEY_MAIN_LISTVIEW_FILTER, "publicgathering");
                    intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    break;
                case 1:
                    saveToPreferences(context, KEY_MAIN_LISTVIEW_FILTER, "all");
                    intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }
}
