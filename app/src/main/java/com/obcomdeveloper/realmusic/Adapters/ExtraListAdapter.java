package com.obcomdeveloper.realmusic.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Songs.PlayerActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ExtraListAdapter extends ArrayAdapter<Song> {


    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    private List<Song> songs_list =null;
    private ArrayList<Song> temp_array;
    private ArrayList<String> saved_songs_list;
    private int mPlayingSong;
    private String isPlaying;


    public ExtraListAdapter(Context context, int resource, List<Song> objects) {
        super(context, resource, objects);
        mInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext=context;
        layoutResource=resource;
        this.songs_list =objects;


        this.temp_array = new ArrayList<Song>();
        this.temp_array.addAll(songs_list);

    }


    private static class ViewHolder{
        private TextView song_name;
        private ImageView tick;
        private ImageView playing;


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
            holder.playing=convertView.findViewById(R.id.playing_extra);

            convertView.setTag(holder);
        }else{
            holder=(ViewHolder) convertView.getTag();

        }
        holder.song_name.setText(getItem(position).getSong_name());

        saved_songs_list = PlayerActivity.getArrayList(mContext.getString(R.string.shared_array_list_key),mContext);
        mPlayingSong=PlayerActivity.getCurrentIndexPref(mContext.getString(R.string.shared_current_index),mContext);

        if(mPlayingSong != -1 && saved_songs_list.size()>mPlayingSong){
            isPlaying=saved_songs_list.get(mPlayingSong).replace(".mp3","");
        }

        if(getItem(position).getSong_name().equals(isPlaying)){
            holder.playing.setVisibility(View.VISIBLE);
        }else {
            holder.playing.setVisibility(View.INVISIBLE);
        }

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
