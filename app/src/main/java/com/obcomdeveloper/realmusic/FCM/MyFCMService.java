package com.obcomdeveloper.realmusic.FCM;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.obcomdeveloper.realmusic.Extras.Extras;
import com.obcomdeveloper.realmusic.Models.Wallpaper;
import com.obcomdeveloper.realmusic.MyApplication;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Songs.PlaylistActivity;
import com.obcomdeveloper.realmusic.Wallpapers.WallpaperMain;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MyFCMService extends FirebaseMessagingService {

    private static final String TAG = "MyFCMService";
    public static final String CHANNEL_ID="Channel_1";
    public static final String SPLIT_INTO_LINES_BY="-";
    public static final int NOTIFICATION_ID=01;
    private Intent intent;
    public static final int CLICK_INTENT_REQUEST_CODE=001;
    private String notifyFor;
    private String image;
    private String nStyle;
    public static NotificationCompat.Builder notificationBuilder;
    public static NotificationManagerCompat notificationManager;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title=remoteMessage.getNotification().getTitle();
        String message=remoteMessage.getNotification().getBody();



        Map bundle=remoteMessage.getData();
        String goTo= (String) bundle.get(getString(R.string.Activity_Notification));
        image=(String)bundle.get(getString(R.string.Image_Notification));
        nStyle=(String) bundle.get(getString(R.string.Style_Notification));

        if(Objects.requireNonNull(goTo).equals(getString(R.string.Incoming_ForExtras))){

            intent=new Intent(MyApplication.getAppContext(), Extras.class);

        }else if(Objects.requireNonNull(goTo).equals(getString(R.string.Incoming_ForPlaylist))){

            intent=new Intent(MyApplication.getAppContext(), PlaylistActivity.class);

        }else if(Objects.requireNonNull(goTo).equals(getString(R.string.Incoming_ForWallpapers))){

            intent=new Intent(MyApplication.getAppContext(), WallpaperMain.class);

        }

        notify(title,message,intent,image,nStyle);


    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG, "onNewToken: ");

    }

    private void notify(String title,String message,Intent intent,String imageUrl,String notificationStyle){

        if(Objects.requireNonNull(notificationStyle).equals(getString(R.string.BigPicture_Style))){
            new generatePictureStyleNotification(MyApplication.getAppContext(),title,message,imageUrl,intent)
                    .execute();
        }else if(Objects.requireNonNull(notificationStyle).equals(getString(R.string.Inbox_Style))){
            generateInboxStyleNotification(title,message,intent);
        }


    }

    public void popNotification(String title,String message,Intent intent,String notifyFor,String imageUrl){

        NotificationCompat.Builder builder;

        PendingIntent clickPendingIntent=PendingIntent.getActivity(this,CLICK_INTENT_REQUEST_CODE,
                intent,PendingIntent.FLAG_UPDATE_CURRENT);

        if(Objects.requireNonNull(notifyFor).equals("Extras") || Objects.requireNonNull(notifyFor).equals("Playlist")){
            //Customize Extras/Playlist Notification
            builder=new NotificationCompat.Builder(MyApplication.getAppContext(),CHANNEL_ID)
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_cross)
                    .setAutoCancel(true)
                    .setContentText(message)
                    .setContentIntent(clickPendingIntent);
        }else {
            //Customize Wallpaper Notification

            Log.d(TAG, "popNotification: wallpaper");

        }

        Log.d(TAG, "popNotification: ");


//        NotificationManagerCompat manager=NotificationManagerCompat.from(MyApplication.getAppContext());
//        manager.notify(NOTIFICATION_ID,builder.build());
    }

    public class generatePictureStyleNotification extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private String title, message, imageUrl,notifyFor;
        private Intent intent;

        public generatePictureStyleNotification(Context context, String title, String message, String imageUrl,Intent intent) {
            super();
            this.mContext = context;
            this.title = title;
            this.message = message;
            this.imageUrl = imageUrl;
            this.intent=intent;

        }

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
            try {
                URL url = new URL(this.imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);


            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 100, intent, PendingIntent.FLAG_ONE_SHOT);


            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notif = new Notification.Builder(mContext)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(result)
                    .setStyle(new Notification.BigPictureStyle().bigPicture(result)
                    .setBigContentTitle(title)
                    .bigLargeIcon(result))
                    .build();
            notif.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(1, notif);
        }
    }


    public void generateInboxStyleNotification(String title,String message,Intent intent){

        //35

        String[] split_message=message.split(SPLIT_INTO_LINES_BY,getLimit(message));

        PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getAppContext(), 100, intent, PendingIntent.FLAG_ONE_SHOT);

        notificationBuilder =
                new NotificationCompat.Builder(MyApplication.getAppContext())
                        .setSmallIcon(R.drawable.ic_equilizer)
                        .setContentTitle(title)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_equilizer))
                        .setContentIntent(pendingIntent);


        int numberOfLines=split_message.length;

        if(numberOfLines == 2){


            notificationBuilder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(split_message[0])
                    .addLine(split_message[1]));


        }else if(numberOfLines == 3){


            notificationBuilder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(split_message[0])
                    .addLine(split_message[1])
                    .addLine(split_message[2]));

        }else if(numberOfLines == 4){


            notificationBuilder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(split_message[0])
                    .addLine(split_message[1])
                    .addLine(split_message[2])
                    .addLine(split_message[3]));


        }else if(numberOfLines ==5){


            notificationBuilder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(split_message[0])
                    .addLine(split_message[1])
                    .addLine(split_message[2])
                    .addLine(split_message[3])
                    .addLine(split_message[4]));

        }else if(numberOfLines == 6){


            notificationBuilder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(split_message[0])
                    .addLine(split_message[1])
                    .addLine(split_message[2])
                    .addLine(split_message[3])
                    .addLine(split_message[4])
                    .addLine(split_message[5]));

        }else if(numberOfLines == 7){


            notificationBuilder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(split_message[0])
                    .addLine(split_message[1])
                    .addLine(split_message[2])
                    .addLine(split_message[3])
                    .addLine(split_message[4])
                    .addLine(split_message[5])
                    .addLine(split_message[6]));

        }else if(numberOfLines == 8){

            notificationBuilder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(split_message[0])
                    .addLine(split_message[1])
                    .addLine(split_message[2])
                    .addLine(split_message[3])
                    .addLine(split_message[4])
                    .addLine(split_message[5])
                    .addLine(split_message[6])
                    .addLine(split_message[7]));

        }else if(numberOfLines == 9){

            notificationBuilder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(split_message[0])
                    .addLine(split_message[1])
                    .addLine(split_message[2])
                    .addLine(split_message[3])
                    .addLine(split_message[4])
                    .addLine(split_message[5])
                    .addLine(split_message[6])
                    .addLine(split_message[7])
                    .addLine(split_message[8]));

        }else if(numberOfLines == 10){

            notificationBuilder.setStyle(new NotificationCompat.InboxStyle()
                    .addLine(split_message[0])
                    .addLine(split_message[1])
                    .addLine(split_message[2])
                    .addLine(split_message[3])
                    .addLine(split_message[4])
                    .addLine(split_message[5])
                    .addLine(split_message[6])
                    .addLine(split_message[7])
                    .addLine(split_message[8])
                    .addLine(split_message[9]));

        }


        notificationManager = NotificationManagerCompat.from(MyApplication.getAppContext());

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    private static String[] splitToNChar(String text, int size) {
        List<String> parts = new ArrayList<>();

        int length = text.length();
        for (int i = 0; i < length; i += size) {
            parts.add(text.substring(i, Math.min(length, i + size)));
        }
        return parts.toArray(new String[0]);
    }

    private int getLimit(String message){

        if(message.length() < 70){
            return 2;
        }else if(message.length() > 70 && message.length() < 105){
            return 3;
        }else if(message.length() > 105 && message.length() < 140){
            return 4;
        }else if(message.length() > 140 && message.length() < 175){
            return 5;
        }else if(message.length() > 175 && message.length() < 210){
            return 6;
        }else {
            return 7;
        }
    }

}
