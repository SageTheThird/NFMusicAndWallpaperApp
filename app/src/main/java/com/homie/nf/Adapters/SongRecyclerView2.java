package com.homie.nf.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.homie.nf.Models.RowItem;
import com.homie.nf.R;
import com.homie.nf.Songs.PlayerActivity;

import java.util.ArrayList;

import static com.homie.nf.Songs.playlist_Activity.listView;


public class SongRecyclerView2 extends FirestoreRecyclerAdapter<RowItem, SongRecyclerView2.ImageHolder> {
    private static final String TAG = "SongRecyclerView";

    private  onItemClickListener listener;
    private Context context;
    private ArrayList<String> saved_songs_list;


    public SongRecyclerView2(@NonNull FirestoreRecyclerOptions<RowItem> options, Context context) {

        super(options);
        this.context = context;

        //get the fresh list of files in the folder everytime
        saved_songs_list = PlayerActivity.getArrayList(context.getString(R.string.shared_array_list_key),context);


    }

    @Override
    protected void onBindViewHolder(@NonNull ImageHolder holder, final int position, @NonNull final RowItem model) {


        Log.d(TAG, "onBindViewHolder: on bind called"+position);
        //String imageUri = context.getString(R.string.drawable_universal) + R.drawable.songs_thumbnail;
        //UniversalImageLoader.setImage(imageUri,holder.imageViewWall,null,"");

       String song_name=model.getSong_name();
        String song_name_on_display=song_name.replace(".mp3","");

       holder.song_name.setText(song_name_on_display);

       //set imageview to downloaded songs i-e songs present in the folder
       if(saved_songs_list.contains(song_name)){
           Log.d("TAG", "onBindViewHolder: "+saved_songs_list.size());
           holder.tick.setVisibility(View.VISIBLE);
       }else {
           holder.tick.setVisibility(View.INVISIBLE);
       }


    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item,viewGroup,false);


        return new ImageHolder(v);
    }

    class ImageHolder extends RecyclerView.ViewHolder{


        private TextView song_name;
        private ImageView tick;



        public ImageHolder(@NonNull View itemView) {

            super(itemView);
            song_name=itemView.findViewById(R.id.song_nameView);
            tick=itemView.findViewById(R.id.tick);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=getAdapterPosition();

                    if(position!=RecyclerView.NO_POSITION && listener != null){

                        listener.onItemClick(getSnapshots().getSnapshot(position),position);

                    }

                }
            });
        }
    }

    public interface onItemClickListener{

        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }
    public void setOnItemClickListener(onItemClickListener listener){

        this.listener=listener;
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();

    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
