package com.obcomdeveloper.realmusic.Adapters;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.obcomdeveloper.realmusic.Models.Wallpaper;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Utils.UniversalImageLoader;
import com.obcomdeveloper.realmusic.Wallpapers.ImageClickActivity;

import java.util.List;

public class UltraPagerAdapter extends PagerAdapter {
    private static final String TAG = "UltraPagerAdapter";

    private Context context;
    private List<Wallpaper> full_walls_list;
    private int mUrlPosition;
    private List<Wallpaper> small_walls_list;


    public UltraPagerAdapter(List<Wallpaper> full_walls_list, Context context,int urlPosition,
                             List<Wallpaper> small_walls_list) {

        this.context=context;
        this.full_walls_list = full_walls_list;
        this.mUrlPosition =urlPosition;
        this.small_walls_list=small_walls_list;

    }

   @Override
    public int getCount() {

        return full_walls_list.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }


    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {


        LayoutInflater mInflater = LayoutInflater.from(context);
        View item_view = mInflater.inflate(R.layout.fullscreen_image, container, false);

        final ImageView imageView = item_view.findViewById(R.id.fullscreen_imageView);
        final ProgressBar progressBar = item_view.findViewById(R.id.full_wall_progress_bar);



        //mUrlPosition=position;


        UniversalImageLoader.setImage(full_walls_list.get(position).getDownload_url(),imageView,progressBar,"");

        item_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, ImageClickActivity.class);
                intent.putExtra("clickedurl",full_walls_list.get(position).getDownload_url());
                context.startActivity(intent);
            }
        });
        /*Picasso
                .with(context)
                .load(mImagesUrls.get(position))
                .fetch(new Callback() {

                    @Override
                    public void onSuccess() {
                        Picasso
                                .with(context)
                                .load(mImagesUrls.get(position))
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.ic_change_history_black_24dp)
                                .into(imageView);


                    }

                    @Override
                    public void onError() {

                    }
                });*/

        container.addView(item_view);
//        linearLayout.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, container.getContext().getResources().getDisplayMetrics());
//        linearLayout.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, container.getContext().getResources().getDisplayMetrics());
        return item_view;
    }



    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);

    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container,mUrlPosition, object);

    }
}
