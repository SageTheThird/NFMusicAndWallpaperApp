package com.homie.nf.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.homie.nf.Models.Wallpaper;
import com.homie.nf.R;
import com.homie.nf.Utils.Animation;
import com.homie.nf.Utils.UniversalImageLoader;

import static com.homie.nf.Wallpapers.WallpaperMain.recyclerView;

public class ImageRecyclerView2 extends FirestoreRecyclerAdapter<Wallpaper, ImageRecyclerView2.ImageHolder> {

    private  onItemClickListener listener;
    Context context;
    private int previousPosition;


    public ImageRecyclerView2(@NonNull FirestoreRecyclerOptions<Wallpaper> options, Context context) {

        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final ImageHolder holder, final int position, @NonNull final Wallpaper model) {



        //holder.imageViewWall.setText(model.getDownloadUrl());
       // holder.cardView.setAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_scale_animation));
        if(position > previousPosition){
            Animation.animate(holder,true);
        }else {
            Animation.animate(holder,false);
        }

        previousPosition=position;


        UniversalImageLoader.setImage(model.getDownload_url(),holder.imageViewWall,holder.progressBar,"");

        int width=context.getResources().getDisplayMetrics().widthPixels;
        int cardWidth=width/3;
        holder.cardView.setLayoutParams(new CardView.LayoutParams(cardWidth,900));

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
        recyclerView.scrollToPosition(0);
    }




}
