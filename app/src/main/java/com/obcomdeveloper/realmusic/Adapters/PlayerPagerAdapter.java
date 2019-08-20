package com.obcomdeveloper.realmusic.Adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Songs.PlayerActivity;
import com.obcomdeveloper.realmusic.Utils.SharedPreferences;
import com.obcomdeveloper.realmusic.Utils.UniversalImageLoader;

import java.util.List;

public class PlayerPagerAdapter extends PagerAdapter {
    private static final String TAG = "UltraPagerAdapter";

    private Context context;
    private List<Song> song_list;
    private SharedPreferences mSharedPrefs;



    public PlayerPagerAdapter(List<Song> song_list, Context context) {

        this.context=context;
        this.song_list = song_list;

        mSharedPrefs=new SharedPreferences(context);
    }

   @Override
    public int getCount() {

        return song_list.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }


    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {


        LayoutInflater mInflater = LayoutInflater.from(context);
        View item_view = mInflater.inflate(R.layout.test_player_fullscreen_image, container, false);

        final ImageView imageView = item_view.findViewById(R.id.test_player_fullscreen_iv);



        //mUrlPosition=position;
        Log.d(TAG, "AdapterPosition: "+position);



        String thumbnail=song_list.get(mSharedPrefs.getInt(context.getString(R.string.shared_current_index),0)).getThumbnail();
        UniversalImageLoader.setImage(thumbnail,imageView,null,"");
//        Needle.onBackgroundThread().execute(new Runnable() {
////            @Override
////            public void run() {
////                String song_name=mSongsList.get(PlayerActivity
////                        .getIntPref(context.getString(R.string.shared_current_index),context))
////                        .replace(".mp3","");
////
////                for(int i1=0;i1<song_list.size();i1++){
////                    if(song_list.get(i1).getSong_name().equals(song_name)){
////                        final String thumbnail=song_list.get(i1).getThumbnail();
////                        Log.d(TAG, "instantiateItem: thumbnail : "+thumbnail);
////                        Needle.onMainThread().execute(new Runnable() {
////                            @Override
////                            public void run() {
////                                UniversalImageLoader.setImage(thumbnail,
////                                        imageView,null,"");
////                            }
////                        });
////
////
////
////                    }
////                }
////            }
////        });




        //String song_name=mSongsList.get(position).replace(".mp3","");


        //UniversalImageLoader.setImage(song_list.get(position).getThumbnail(),imageView,null,"");

//        item_view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent=new Intent(context, ImageClickActivity.class);
//                intent.putExtra("clickedurl",mImagesUrls.get(position));
//                intent.putExtra("smallclickedurl",small_walls_list.get(position).getDownload_url());
//                context.startActivity(intent);
//            }
//        });
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
        super.setPrimaryItem(container,position, object);

    }


}
