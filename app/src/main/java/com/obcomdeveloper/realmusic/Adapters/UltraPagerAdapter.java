package com.obcomdeveloper.realmusic.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Utils.UniversalImageLoader;

import java.util.ArrayList;

public class UltraPagerAdapter extends PagerAdapter {
    private static final String TAG = "UltraPagerAdapter";

    private Context context;
    private boolean isMultiScr;
    private ArrayList<String> mImagesUrls;
    private int mUrlPosition;


    public UltraPagerAdapter(boolean isMultiScr, ArrayList<String> mImagesUrls, Context context,int urlPosition) {

        this.context=context;
        this.isMultiScr = isMultiScr;
        this.mImagesUrls = mImagesUrls;
        this.mUrlPosition =urlPosition;

    }

   @Override
    public int getCount() {

        return mImagesUrls.size();
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
        //final ProgressBar progressBar = item_view.findViewById(R.id.wallpaper_progressBar);



        //mUrlPosition=position;
        Log.d(TAG, "AdapterPosition: "+position);
        Log.d(TAG, "mUrlPosition: "+mUrlPosition);


        UniversalImageLoader.setImage(mImagesUrls.get(position),imageView,null,"");
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
        container.removeView((LinearLayout) object);

    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container,mUrlPosition, object);

    }
}