package com.obcomdeveloper.realmusic.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;


import com.obcomdeveloper.realmusic.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class UniversalImageLoader {

    public static final int DEFAULT_IMAGE= R.drawable.ic_search_black_24dp;

    private Context mContext;

    public UniversalImageLoader(Context mContext) {
        this.mContext = mContext;
    }


    public ImageLoaderConfiguration getConfig(){

        DisplayImageOptions defaultOptions=new DisplayImageOptions.Builder()
                .showImageOnLoading(DEFAULT_IMAGE)
                .showImageForEmptyUri(DEFAULT_IMAGE)
                .considerExifParams(true)
                .showImageOnFail(DEFAULT_IMAGE)
                .cacheOnDisk(true).cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration imageLoaderConfiguration=new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 *1024 *1024).build();

        return imageLoaderConfiguration;
    }

    public static void setImage(String url, ImageView image, final ProgressBar progressBar,String append){

        ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.displayImage(append + url, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(progressBar != null){
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                if(progressBar != null){
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                if(progressBar != null){
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

                if(progressBar != null){
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }



}
