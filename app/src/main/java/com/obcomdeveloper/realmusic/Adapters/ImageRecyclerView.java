package com.obcomdeveloper.realmusic.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.obcomdeveloper.realmusic.Models.Wallpaper;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Utils.Animation;
import com.obcomdeveloper.realmusic.Utils.UniversalImageLoader;

import java.util.List;

public class ImageRecyclerView extends RecyclerView.Adapter<ImageRecyclerView.ImageHolder> {

    private  onItemClickListener listener;
    Context context;
    private int previousPosition;
    private List<Wallpaper> walls_list;


    public ImageRecyclerView(List<Wallpaper> list, Context context) {
        this.context = context;
        this.walls_list=list;
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageHolder holder, final int position) {



        //holder.imageViewWall.setText(model.getDownloadUrl());
       // holder.cardView.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_scale_animation));
        if(position > previousPosition){
            Animation.animate(holder,true);
        }else {
            Animation.animate(holder,false);
        }

        previousPosition=position;


        UniversalImageLoader.setImage(walls_list.get(position).getDownload_url(),holder.imageViewWall,holder.progressBar,"");

        int width=context.getResources().getDisplayMetrics().widthPixels;
        int cardWidth=width/3;
        holder.cardView.setLayoutParams(new CardView.LayoutParams(cardWidth,900));

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

                    if(position!=RecyclerView.NO_POSITION && listener != null){

                        listener.onItemClick(walls_list.get(position),position);

                    }

                }
            });
        }
    }

    public interface onItemClickListener{

        void onItemClick(Wallpaper documentSnapshot, int position);
    }
    public void setOnItemClickListener(onItemClickListener listener){

        this.listener=listener;
    }


}
