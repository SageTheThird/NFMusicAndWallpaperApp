package com.obcomdeveloper.realmusic.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.content.res.AppCompatResources;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;


import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import needle.Needle;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;

public class UniversalImageLoader {

    public static final int DEFAULT_IMAGE= 0;

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
    public ImageLoaderConfiguration getConfigCards(){

        int width=mContext.getResources().getDisplayMetrics().widthPixels;
        final int cardWidth=width/3;
        DisplayImageOptions defaultOptions=new DisplayImageOptions.Builder()
                .showImageOnLoading(DEFAULT_IMAGE)
                .showImageForEmptyUri(DEFAULT_IMAGE)
                .considerExifParams(true)
                .showImageOnFail(DEFAULT_IMAGE)
                .cacheOnDisk(true).cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .postProcessor(new BitmapProcessor() {
                    @Override
                    public Bitmap process(Bitmap bitmap) {
                        return Bitmap.createScaledBitmap(bitmap,cardWidth,450,false);
                    }
                })
                .build();


        ImageLoaderConfiguration imageLoaderConfiguration=new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 *1024 *1024).build();

        return imageLoaderConfiguration;
    }

    public static void setImage(String url, final ImageView image, final ProgressBar progressBar, String append){

        ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.handleSlowNetwork(true);
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

    public static void setBlurredImage(String url, final ImageView image, final ProgressBar progressBar,
                                       String append, final SeekBar seekBar,
                                       final ImageView viewLyrics, final ImageView viewGenius){

        final ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.handleSlowNetwork(true);
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
            public void onLoadingComplete(String imageUri, final View view, final Bitmap loadedImage) {

                final Bitmap[] bitmap = {null};
                //final int[] getDominantColor = new int[1];
                final int[] getDominantColor2 = new int[1];
                Needle.onBackgroundThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        bitmap[0] = BlurImage.fastblur(loadedImage,0.1f,4);
                        //getDominantColor[0] =ColorPallete.getDominantColor(bitmap[0]);
                        getDominantColor2[0]=ColorPallete.getDominantColor2(bitmap[0]);
                        Needle.onMainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                image.setImageBitmap(bitmap[0]);
                                if(seekBar != null){

                                    Log.d(TAG, "run: seekbar thumb color setting");
                                    seekBar.getThumb().setColorFilter(getDominantColor2[0], PorterDuff.Mode.SRC_ATOP);
                                    seekBar.getProgressDrawable().setColorFilter(getDominantColor2[0], PorterDuff.Mode.SRC_ATOP);

                                }
                                if(viewLyrics != null && viewGenius != null){
                                    GradientDrawable drawable_lyrics = (GradientDrawable)viewLyrics.getBackground();
                                    drawable_lyrics.setStroke(3, getDominantColor2[0]);

                                    GradientDrawable drawable_genius = (GradientDrawable)viewGenius.getBackground();
                                    drawable_genius.setStroke(3, getDominantColor2[0]);
                                }
                            }
                        });

                    }
                });


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

    public static Drawable getTintedDrawable(Resources res,
                                             @DrawableRes int drawableResId, @ColorRes int colorResId) {
        Drawable drawable = res.getDrawable(drawableResId);
        int color = res.getColor(colorResId);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }




}
