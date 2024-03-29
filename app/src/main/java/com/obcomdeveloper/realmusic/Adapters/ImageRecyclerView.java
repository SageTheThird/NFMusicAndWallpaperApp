package com.obcomdeveloper.realmusic.Adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.Models.Wallpaper;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Utils.Animation;
import com.obcomdeveloper.realmusic.Utils.UniversalImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ImageRecyclerView extends RecyclerView.Adapter<ImageRecyclerView.ImageHolder> {




    private static final String TAG = "ImageRecyclerView";
    private  ImageonItemClickListener mListener;
    private Context context;
    private int previousPosition;
    private List<Wallpaper> walls_list;


    public ImageRecyclerView(Context context,ImageonItemClickListener mListener) {
        this.context = context;
        this.walls_list=new ArrayList<>();
        this.mListener=mListener;
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageHolder holder, final int position) {


        int width=context.getResources().getDisplayMetrics().widthPixels;
        int cardWidth=width/3;
        Log.d(TAG, "onBindViewHolder: Card Width : "+cardWidth);
        holder.cardView.setLayoutParams(new CardView.LayoutParams(cardWidth,900));


        //holder.imageViewWall.setText(model.getDownloadUrl());
       // holder.cardView.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_scale_animation));
        if(position > previousPosition){
            Animation.animate(holder,true);
        }else {
            Animation.animate(holder,false);
        }

        previousPosition=position;


        UniversalImageLoader.setImage(walls_list.get(position).getDownload_url(),holder.imageViewWall,holder.progressBar,"");



    }

    @Override
    public int getItemCount() {
        return walls_list.size();
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview,viewGroup,false);


        return new ImageHolder(v);
    }

    class ImageHolder extends RecyclerView.ViewHolder{

         ImageView imageViewWall;
         CardView cardView;
         ProgressBar progressBar;

        public ImageHolder(@NonNull View itemView) {

            super(itemView);
            imageViewWall=itemView.findViewById(R.id.wallpaper_id);
            cardView=itemView.findViewById(R.id.cardview_id);
            progressBar=itemView.findViewById(R.id.wall_progressBar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=getAdapterPosition();

                    if(position!=RecyclerView.NO_POSITION && mListener != null){

                        mListener.onItemClick(walls_list.get(position),position);

                    }

                }
            });
        }
    }

    public interface ImageonItemClickListener{

        void onItemClick(Wallpaper documentSnapshot, int position);
    }

    public void addItems(List<Wallpaper> list){
        this.walls_list.addAll(list);
        notifyDataSetChanged();
    }



    public boolean clearList(){

        try{
            if(walls_list != null){
                this.walls_list.clear();
                //songs_list=new ArrayList<>();
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }


        return true;
    }


    public void addAll(List<Wallpaper> newUsers) {
        int initialSize = walls_list.size();
        walls_list.addAll(newUsers);
        notifyItemRangeInserted(initialSize, newUsers.size());

    }

    public String getLastItemId() {
        return walls_list.get(walls_list.size() - 1).getId();
    }


}
