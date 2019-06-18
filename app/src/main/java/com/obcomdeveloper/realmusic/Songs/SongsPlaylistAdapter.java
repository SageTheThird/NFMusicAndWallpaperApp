package com.obcomdeveloper.realmusic.Songs;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.R;

import java.util.ArrayList;


public class SongsPlaylistAdapter extends RecyclerView.Adapter<SongsPlaylistAdapter.ViewHolder>{
    private ArrayList<Song> songs_list;
    private SongsPlaylistAdapter.onItemClickListener listener;
    private Context mContext;

    // RecyclerView listView;
    public SongsPlaylistAdapter(ArrayList<Song> songs_list, Context context) {
        this.songs_list = songs_list;
        this.mContext=context;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view= layoutInflater.inflate(R.layout.item, parent, false);
       return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {


        holder.song_name.setText(songs_list.get(position).getSong_name());

    }


    @Override
    public int getItemCount() {
        return songs_list.size();
    }

     class ViewHolder extends RecyclerView.ViewHolder {

        private TextView song_name;

        public ViewHolder(View itemView) {
            super(itemView);

            song_name=itemView.findViewById(R.id.song_nameView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=getAdapterPosition();

                    if(position!=RecyclerView.NO_POSITION && listener != null){

                        listener.onItemClick(position);

                    }

                }
            });
        }
    }
    public interface onItemClickListener{

        void onItemClick(int position);
    }
    public void setOnItemClickListener(SongsPlaylistAdapter.onItemClickListener listener){

        this.listener=listener;
    }
}