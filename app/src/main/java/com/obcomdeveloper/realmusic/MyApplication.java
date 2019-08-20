package com.obcomdeveloper.realmusic;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import static com.obcomdeveloper.realmusic.FCM.MyFCMService.CHANNEL_ID;

public class MyApplication extends Application {

    public static final String CHANNEL_ID="Channel_1";
    public static final String CHANNEL_NAME="Channel__1";


    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext=getApplicationContext();
        createNotificationChannel();
        subscribeToFCM();
    }

    private void subscribeToFCM() {

        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.FreeVersion_NotificationSub))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

    }

    private void createNotificationChannel(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME
                    , NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);


        }

    }

    public static Context getAppContext() {
        return mContext;
    }
}
