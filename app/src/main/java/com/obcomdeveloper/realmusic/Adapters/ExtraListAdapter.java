package com.obcomdeveloper.realmusic.Adapters;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Utils.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ExtraListAdapter extends RecyclerView.Adapter<ExtraListAdapter.ViewHolder> {

    private ExtrasItemClickListener mListener;

    public interface ExtrasItemClickListener{
        void onItemClickListener(Song song,int position,View view);
    }

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


    public ExtraListAdapter(Context context, int resource,ExtrasItemClickListener mListener) {


        mInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext=context;
        layoutResource=resource;
        this.songs_list =new ArrayList<>();


        this.temp_array = new ArrayList<>();

        mSharedPrefs=new SharedPreferences(context);

        this.mListener=mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v=mInflater.inflate(layoutResource,parent,false);
        return new ExtraListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        holder.song_name.setText(Objects.requireNonNull(songs_list.get(position)).getSong_name());

        saved_songs_list = mSharedPrefs.getList(mContext.getString(R.string.shared_array_list_key));
        mPlayingSong=mSharedPrefs.getInt(mContext.getString(R.string.shared_current_index),0);

        if(mPlayingSong != -1 && saved_songs_list.size()>mPlayingSong){
            isPlaying=saved_songs_list.get(mPlayingSong).replace(".mp3","");
        }

        if(Objects.requireNonNull(songs_list.get(position)).getSong_name().equals(isPlaying)){
            holder.playing.setVisibility(View.VISIBLE);
        }else {
            holder.playing.setVisibility(View.INVISIBLE);
        }

        //indicates the song exist in folder and ready to play
        String name= Objects.requireNonNull(songs_list.get(position)).getSong_name() + ".mp3";
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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClickListener(songs_list.get(position),holder.getAdapterPosition(),holder.itemView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs_list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView song_name;
        private ImageView tick;
        private ImageView playing;
        private ImageView delete;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            song_name =itemView.findViewById(R.id.song_nameView_extra);
            tick=itemView.findViewById(R.id.tick_extra);
            playing=itemView.findViewById(R.id.playing_extra);
            delete=itemView.findViewById(R.id.delete_iv);

        }
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
    public void addItems(List<Song> list){

        int initSize=songs_list.size();
        this.songs_list.addAll(list);
        this.temp_array.addAll(list);
        notifyItemRangeChanged(initSize,list.size());

    }


}
