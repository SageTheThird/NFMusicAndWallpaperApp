package com.obcomdeveloper.realmusic.Adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;;;



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

        if(internetConnectivity() != null && internetConnectivity().isConnected()){
            //For online images are loaded from url
            UniversalImageLoader.setImage(thumbnail,imageView,null,"");
        }else {
            //For Offline it loads drawable
            UniversalImageLoader.setImageDrawable(Integer.parseInt(thumbnail),imageView
                    ,null,"drawable://");
        }

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

    private NetworkInfo internetConnectivity(){
        //Internet Connectivity

        connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo;
    }



}
