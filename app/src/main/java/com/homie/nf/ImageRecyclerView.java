package com.homie.nf;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import static com.homie.nf.WallpaperMain.recyclerView;

public class ImageRecyclerView extends FirestoreRecyclerAdapter<Wallpaper,ImageRecyclerView.ImageHolder> {

    private  onItemClickListener listener;
    Context context;


    public ImageRecyclerView(@NonNull FirestoreRecyclerOptions<Wallpaper> options, Context context) {

        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ImageHolder holder, final int position, @NonNull final Wallpaper model) {



        //holder.imageViewWall.setText(model.getDownloadUrl());

       Picasso.with(context)
                .load(model.getDownloadUrl())
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.ic_search_black_24dp)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .resize(500, 500)
                .into(holder.imageViewWall);


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

        public ImageHolder(@NonNull View itemView) {

            super(itemView);
            imageViewWall=itemView.findViewById(R.id.wallpaper_id);
            cardView=itemView.findViewById(R.id.cardview_id);

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
