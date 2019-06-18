package com.obcomdeveloper.realmusic.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

import com.obcomdeveloper.realmusic.Songs.PlayerActivity;

public class HeadphonesReciever extends BroadcastReceiver {
    private static final String TAG = "HeadphonesReciever";
    @Override
    public void onReceive(Context context, Intent intent) {


        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            // pause();
            try {
                PlayerActivity.mediaPlayer.pause();
                Log.d(TAG, "onReceive: Paused");
            }catch (NullPointerException e){
                Log.d(TAG, "onReceive: NullPointerException "+e.getMessage());
            }catch (IllegalStateException e){
                Log.d(TAG, "onReceive: IllegalStateException "+e.getMessage());
            }catch (IllegalArgumentException e){
                Log.d(TAG, "onReceive: IllegalArgumentException "+e.getMessage());
            }
            
        }
    }

}