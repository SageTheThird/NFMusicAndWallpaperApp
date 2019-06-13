package com.homie.nf.Models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.homie.nf.R;
import com.homie.nf.Songs.PlayerActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SongAdapter extends ArrayAdapter<Song> {

    private static final String TAG = "UserListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    private List<Song> songs_list =null;
    private ArrayList<Song> temp_array;
    private ArrayList<String> saved_songs_list;

    public SongAdapter(Context context, int resource, List<Song> objects) {
        super(context, resource, objects);
        mInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext=context;
        layoutResource=resource;
        this.songs_list =objects;
        saved_songs_list = PlayerActivity.getArrayList(context.getString(R.string.shared_array_list_key),context);

        this.temp_array = new ArrayList<Song>();
        this.temp_array.addAll(songs_list);
    }


    private static class ViewHolder{
        TextView song_name;
        private ImageView tick;


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        if(convertView==null){
            convertView=mInflater.inflate(layoutResource,parent,false);
            holder=new ViewHolder();

            holder.song_name =convertView.findViewById(R.id.song_nameView_extra);
            holder.tick=convertView.findViewById(R.id.tick_extra);

            convertView.setTag(holder);
        }else{
            holder=(ViewHolder) convertView.getTag();

        }
        holder.song_name.setText(getItem(position).getSong_name());

        String name=getItem(position).getSong_name() + ".mp3";
        if(saved_songs_list.contains(name)){
            Log.d("TAG", "onBindViewHolder: "+saved_songs_list.size());
            holder.tick.setVisibility(View.VISIBLE);
        }else {
            holder.tick.setVisibility(View.INVISIBLE);
        }



        return convertView;
    }

    // put below code (method) in Adapter class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        songs_list.clear();
        if (charText.length() == 0) {
            songs_list.addAll(temp_array);
        }
        else
        {
            for (Song wp : temp_array) {
                if (wp.getSong_name().toLowerCase(Locale.getDefault()).contains(charText)) {
                    songs_list.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }


}
