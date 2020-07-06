package firbase.go.beruniy.view_setup.choise;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import firbase.go.beruniy.R;
import firbase.go.beruniy.view_setup.choise.utils.Utils;


/**
 * @author Martin Pfeffer (pepperonas)
 */
public class ShareAdapter extends BaseAdapter {

    private Object[] items;
    private LayoutInflater mInflater;
    private Context mCtx;
    private Typeface mTypeface;


    public ShareAdapter(@NonNull Context context) {
        this.mInflater = LayoutInflater.from(context);

        Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        List activities = context.getPackageManager().queryIntentActivities(sendIntent, 0);
        items = activities.toArray();
        mCtx = context;
    }


    public ShareAdapter(@NonNull Context context, Typeface typeface) {
        this.mInflater = LayoutInflater.from(context);

        Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        List activities = context.getPackageManager().queryIntentActivities(sendIntent, 0);
        items = activities.toArray();
        mCtx = context;
        mTypeface = typeface;
    }


    public int getCount() {
        return items.length;
    }


    public Object getItem(int position) {
        return items[position];
    }


    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.custom_list_item_share_app, null);

            holder = new ViewHolder();

            holder.logo = (ImageView) convertView.findViewById(R.id.iv_simple_list_item_share_app);
            holder.name = (TextView) convertView.findViewById(R.id.tv_simple_list_item_share_app);

            if (mTypeface != null) {
                holder.name.setTypeface(mTypeface);
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(((ResolveInfo) items[position]).activityInfo
                .applicationInfo.loadLabel(mCtx.getPackageManager()).toString());

        holder.logo.setImageDrawable(((ResolveInfo) items[position]).activityInfo
                .applicationInfo.loadIcon(mCtx.getPackageManager()));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(
                Utils.dp2px(mCtx, 16),
                Utils.dp2px(mCtx, 4),
                Utils.dp2px(mCtx, 4),
                Utils.dp2px(mCtx, 4));

        holder.logo.setLayoutParams(layoutParams);

        return convertView;
    }


    static class ViewHolder {

        TextView name;
        ImageView logo;
    }

}