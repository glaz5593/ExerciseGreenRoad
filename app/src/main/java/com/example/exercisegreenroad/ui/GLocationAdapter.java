package com.example.exercisegreenroad.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.exercisegreenroad.R;
import com.example.exercisegreenroad.manage.AppManager;
import com.example.exercisegreenroad.objects.GLocation;
import com.example.exercisegreenroad.utils.Utils;

public class GLocationAdapter extends ArrayAdapter<GLocation> {
    Context context;
    boolean showFullData;
    GLocationAdapterListener listener;
    public interface GLocationAdapterListener{
        void onSelectGLocation(GLocation location);
    }
    public void setShowFullData(boolean value){
        showFullData=value;
        notifyDataSetChanged();
    }

    public GLocationAdapter(@NonNull Context context,GLocationAdapterListener listener) {
        super(context, R.layout.item_glocation_full, AppManager.getInstance().getHistory().locations);
        this.context=context;
        this.listener=listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
       GLocation item=getItem(position);

        convertView = LayoutInflater.from(context).inflate(showFullData ? R.layout.item_glocation_full:R.layout.item_glocation_point, parent, false);

        ImageView iv_type = convertView.findViewById(R.id.iv_type);
        int iconResId=R.drawable.ic_baseline_location_on_red;
        if(item.type==GLocation.TYPE_EXIT){
            iconResId=R.drawable.exit_arrow;
        }else if(item.type==GLocation.TYPE_ENTER){
            iconResId=R.drawable.enter_arrow;
        }
        iv_type.setImageResource(iconResId);

        if(!showFullData){
            return convertView;
        }

        TextView tv_time=convertView.findViewById(R.id.tv_time);
        tv_time.setText(Utils.getTimeOrDateTime(item.date));
        TextView tv_time2=convertView.findViewById(R.id.tv_time2);
        tv_time2.setText(Utils.getTimeOrDateTime(item.date));

        TextView tv_description=convertView.findViewById(R.id.tv_description);
        tv_description.setText(item.description);
        TextView tv_description2=convertView.findViewById(R.id.tv_description2);
        tv_description2.setText(item.description);

        convertView.setTag(item);
        convertView.setOnClickListener(view -> {
            listener.onSelectGLocation((GLocation) view.getTag());
        });

        return convertView;
    }
}
