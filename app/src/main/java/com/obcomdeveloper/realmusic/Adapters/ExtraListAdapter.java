package com.obcomdeveloper.realmusic.Adapters;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Songs.PlayerActivity;
import com.obcomdeveloper.realmusic.Utils.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ExtraListAdapter extends ArrayAdapter<Song> {


    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    private List<Song> songs_list =null;
    private ArrayList<Song> temp_array;
    private List<String> saved_songs_list;
    private int mPlayingSong;
    private String isPlaying;
    private SharedPreferences mSharedPrefs;
    private AlertDialog alertDialog;


    public ExtraListAdapter(Context context, int resource, List<Song> objects) {
        super(context, resource, objects);
        mInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext=context;
        layoutResource=resource;
        this.songs_list =objects;


        this.temp_array = new ArrayList<Song>();
        this.temp_array.addAll(songs_list);
        mSharedPrefs=new SharedPreferences(context);

    }


    private static class ViewHolder{
        private TextView song_name;
        private ImageView tick;
        private ImageView playing;
        private ImageView delete;


    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;

        if(convertView==null){
            convertView=mInflater.inflate(layoutResource,parent,false);
            holder=new ViewHolder();

            holder.song_name =convertView.findViewById(R.id.song_nameView_extra);
            holder.tick=convertView.findViewById(R.id.tick_extra);
            holder.playing=convertView.findViewById(R.id.playing_extra);
            holder.delete=convertView.findViewById(R.id.delete_iv);

            convertView.setTag(holder);
        }else{
            holder=(ViewHolder) convertView.getTag();

        }
        holder.song_name.setText(Objects.requireNonNull(getItem(position)).getSong_name());

        saved_songs_list = mSharedPrefs.getList(mContext.getString(R.string.shared_array_list_key));
        mPlayingSong=mSharedPrefs.getInt(mContext.getString(R.string.shared_current_index),0);

        if(mPlayingSong != -1 && saved_songs_list.size()>mPlayingSong){
            isPlaying=saved_songs_list.get(mPlayingSong).replace(".mp3","");
        }

        if(Objects.requireNonNull(getItem(position)).getSong_name().equals(isPlaying)){
            holder.playing.setVisibility(View.VISIBLE);
        }else {
            holder.playing.setVisibility(View.INVISIBLE);
        }

        //indicates the song exist in folder and ready to play
        String name= Objects.requireNonNull(getItem(position)).getSong_name() + ".mp3";
        if(saved_songs_list.contains(name)){
            holder.tick.setBackgroundResource(R.drawable.ic_tick);
            holder.delete.setBackgroundResource(R.drawable.ic_delete);

        }else {
            holder.tick.setBackgroundResource(R.drawable.ic_ad);
            holder.delete.setBackgroundResource(R.drawable.ic_load);
        }



        if(holder.delete.getBackground().getConstantState().equals(mContext.getResources()
                .getDrawable(R.drawable.ic_delete).getConstantState())){

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(mContext, ""+position, Toast.LENGTH_LONG).show();
                    if(position != 0){


                        AlertDialog.Builder temmAlertDialogBuilder=getAlertDialogBuilder(position);

                        // create alert dialog
                        alertDialog=temmAlertDialogBuilder.create();
                        alertDialog.show();
//                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//                        int width=mContext.getResources().getDisplayMetrics().widthPixels;
//                        int cardWidth=width-300;
//                        lp.copyFrom(alertDialog.getWindow().getAttributes());
//                        lp.width = cardWidth;
//                        lp.height = 700;
//                        lp.x=-170;
//                        lp.y=50;
//                        alertDialog.getWindow().setAttributes(lp);
//

                    }else {

                        Toast.makeText(mContext, "Error dropping this track", Toast.LENGTH_LONG).show();

                    }


                }
            });

        }else {
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "Load Song", Toast.LENGTH_LONG).show();
                }
            });
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
    private void deleteTrack(int position){

        String temp=songs_list.get(position).getSong_name()+".mp3";
        for(int i=0;i<saved_songs_list.size();i++){
            if(saved_songs_list.get(i).equals(temp)){
                //deletion of song
                String fileToDelete=saved_songs_list.get(i);
                saved_songs_list.remove(i);


                String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/" + mContext.getPackageName() + "/files/Extras/";

                File file = new File(FOLDER_PATH+fileToDelete);
                boolean deleted=file.delete();

                if(deleted){
                    Toast.makeText(mContext, "File Dropped", Toast.LENGTH_LONG).show();
                    mSharedPrefs.saveList(saved_songs_list,mContext.getString(R.string.shared_array_list_key));
                    notifyDataSetChanged();

                }else {
                    Toast.makeText(mContext, "Failed To Drop File", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    private AlertDialog.Builder getAlertDialogBuilder(final int position){

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext,
                R.style.MyAlertDialogTheme);
        LayoutInflater inflater=(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.alert_dialog_custom_view,null);
        Button confirm_btn=view.findViewById(R.id.confirm_dialog_btn);
        Button cancel_btn=view.findViewById(R.id.cancel_dialog_btn);
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(mContext, "Delete Confirmed", Toast.LENGTH_LONG).show();
                deleteTrack(position);
                if(alertDialog!=null){
                    alertDialog.dismiss();
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(alertDialog!=null){
                    alertDialog.dismiss();
                }
            }
        });

        alertDialogBuilder.setView(view);
        // set title
        // set dialog message
        alertDialogBuilder
                .setCancelable(true);



        return alertDialogBuilder;
    }


}
