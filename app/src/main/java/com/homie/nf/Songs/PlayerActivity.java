package com.homie.nf.Songs;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.homie.nf.MainActivity;
import com.homie.nf.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import me.tankery.lib.circularseekbar.CircularSeekBar;


public class PlayerActivity extends AppCompatActivity implements genius_fragment.OnFragmentInteractionListener, lyrics_frag.OnFragmentInteractionListener {

    private static final String TAG = "PlayerActivity";
    public static final int DEFAULT_INT_VALUE = 0;
    private Context mContext = PlayerActivity.this;
    static MediaPlayer mediaPlayer;
    private CircularSeekBar seekBar;
    private Button mPause_btn, mGenius_btn, mLyrics_btn,mNext_btn, mPrev_btn;
    private TextView mSongname_view, mTotal_duration_view, mCurrent_duration_view;
    private String mLyric_file = "", mGenius_file = "";
    private Intent intent;
    private static ArrayList<String> mSongs_list;
    private ImageView mBack_arrow;

    private Handler myHandler = new Handler();
    //for song list
    private static int mCurrentIndex;

    //Notification
    public static final int NOTIFICATION_ID = 1;
    public static final String ACTION_PAUSE = "PauseAction";
    public static final String ACTION_PLAY = "PlayAction";
    public static final String ACTION_STOP = "StopAction";
    public static final String ACTION_NEXT = "NextAction";
    public static final String ACTION_PREV = "PrevAction";
    MediaSessionCompat mediaSessionCompat;


    //action_intents
    Intent pauseIntent,playIntent,stopIntent,nextIntent,prevIntent;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

        mSongs_list = new ArrayList<>();

        //methods
        initWidgets();
        getIncomingIntent();
        playSong(mCurrentIndex);

        Thread thread = new runThread();
        thread.start();


        //listeners
        mGenius_btn.setOnClickListener(GeniusClickListener);
        mLyrics_btn.setOnClickListener(LyricsClickListener);
        seekBar.setOnSeekBarChangeListener(SeekBarChangeListener);
        mPause_btn.setOnClickListener(PauseClickListener);
        mBack_arrow.setOnClickListener(BackClickListener);
        mNext_btn.setOnClickListener(NextClickListener);
        mPrev_btn.setOnClickListener(PrevClickListener);

        //notific
        mediaSessionCompat = new MediaSessionCompat(this, "someTag");

        ArrayList<String> songs=getArrayList(getString(R.string.shared_array_list_key),mContext);
        Log.d(TAG, "onCreate: "+songs.size());

        int index=getCurrentIndexPref(getString(R.string.shared_current_index),mContext);
        Log.d(TAG, "onCreate: "+index);

        

    }

    public static void saveCurrentIndexPref(String key,int index,Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, index);
        editor.commit();
    }
    public static int getCurrentIndexPref(String key,Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int myIntValue = prefs.getInt(key, -1);
        return myIntValue;
    }
    public  static void saveArrayList(ArrayList<String> list, String key,Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public static ArrayList<String> getArrayList(String key,Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void getIncomingIntent() {
        intent = getIntent();

        mLyric_file = intent.getStringExtra(getString(R.string.LYRICSFILE));
        mGenius_file = intent.getStringExtra(getString(R.string.GENIUSFILENAME));

        mCurrentIndex = intent.getIntExtra(getString(R.string.position_song), DEFAULT_INT_VALUE);
        mSongs_list = intent.getStringArrayListExtra(getString(R.string.songslist));
        //saveArrayList(mSongs_list,getString(R.string.shared_array_list_key));
        saveCurrentIndexPref(getString(R.string.shared_current_index),mCurrentIndex,mContext);

        //songTextView setup
        setSongName(mCurrentIndex);

    }

    public void setSongName(int index) {
        String currentName = mSongs_list.get(index);

        String songNameOnly = currentName.replace(getString(R.string.mp3_extenstion), "");
        mSongname_view.setText(songNameOnly);


    }

    private void initWidgets() {
        mPause_btn = findViewById(R.id.pause);
        mSongname_view = findViewById(R.id.songtextView);
        seekBar = findViewById(R.id.circularSeekBar);
        mBack_arrow = findViewById(R.id.back_button);
        mGenius_btn = findViewById(R.id.geniusbutton);
        mLyrics_btn = findViewById(R.id.lyrics_button);
        mNext_btn = findViewById(R.id.nextBtn);
        mPrev_btn = findViewById(R.id.prevBtn);
        mTotal_duration_view = findViewById(R.id.fullDurationView);
        mCurrent_duration_view = findViewById(R.id.currentDuration);


    }



    public void openGeniusFragment(String genius_url) {

        genius_fragment genius_fragment = com.homie.nf.Songs.genius_fragment.newInstance(genius_url);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frameLayout, genius_fragment, getString(R.string.GENIUS_FRAGMENT)).commit();

    }

    public void openLyricsFragment(String lyrics_file) {

        lyrics_frag lyrics_fragment1 = lyrics_frag.newInstance(lyrics_file);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frameLayout, lyrics_fragment1, getString(R.string.LYRICS_FRAGMENT)).commit();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        onBackPressed();
    }

    public void playSong(int index) {
        String songName = null;
        try {
            songName = mSongs_list.get(index);
        } catch (IndexOutOfBoundsException e) {
            showToast("No offline files in directory");
            e.printStackTrace();
        }
        String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Documents/";
        final String song_full = FOLDER_PATH + songName;
        if (mPause_btn.getBackground().equals(R.drawable.pause_button)) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            mPause_btn.setBackgroundResource(R.drawable.play_button);
        } else {

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {

                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                        }
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(song_full);
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                                seekBar.setProgress(0);
                                //set max duration to seek_bar
                                seekBar.setMax(mediaPlayer.getDuration());
                                //set total duration to textview
                                setDuration(mediaPlayer.getDuration(), mTotal_duration_view);
                                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        mPause_btn.setBackgroundResource(R.drawable.play_button);
                                        mCurrentIndex++;
                                        mCurrentIndex %= mSongs_list.size();
                                        setSongName(mCurrentIndex);
                                        playSong(mCurrentIndex);

                                    }
                                });
                                Log.d("Prog", "run: " + mediaPlayer.getDuration());
                            }
                        });
                        mPause_btn.setBackgroundResource(R.drawable.pause_button);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            };
            myHandler.postDelayed(runnable, 100);

        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    /*
     * Sets up textview to current position and total duration
     * */
    private void setDuration(int currentDuration, TextView textView) {
        int min = currentDuration / (1000 * 60) % 60;
        int sec = (currentDuration / 1000) % 60;

        String minutes = Integer.toString(min);
        String seconds = Integer.toString(sec);
        if (min <= 9) {

            minutes = "0" + minutes;

        }
        if (sec <= 9) {
            seconds = "0" + seconds;
        }
        textView.setText(minutes + ":" + seconds);
    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public class runThread extends Thread {


        @Override
        public void run() {
            while (true) {

                try {
                    Thread.sleep(75);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("Runwa", "run: " + 1);
                if (mediaPlayer != null) {
                    seekBar.post(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            //set current duration to textview
                            setDuration(mediaPlayer.getCurrentPosition(), mCurrent_duration_view);
                        }
                    });

                    //Log.d("Runwa", "run: " + mediaPlayer.getCurrentPosition());
                }
            }
        }

    }


    ////--------------------------------------LISTENERS------------------------------------------/////
    View.OnClickListener NextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: next");

            mCurrentIndex++;
            mCurrentIndex %= mSongs_list.size();
            setSongName(mCurrentIndex);
            playSong(mCurrentIndex);

        }
    };
    View.OnClickListener PrevClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.d(TAG, "onClick: next button");
            mCurrentIndex = mCurrentIndex > 0 ? mCurrentIndex - 1 : mSongs_list.size() - 1;
            setSongName(mCurrentIndex);
            playSong(mCurrentIndex);
        }
    };
    CircularSeekBar.OnCircularSeekBarChangeListener SeekBarChangeListener = new CircularSeekBar.OnCircularSeekBarChangeListener() {
        @Override
        public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
            int h = Math.round(progress);
            //In the above line we are converting the float value into int because
            // media player required int value and seekbar library gives progress in float
            if (mediaPlayer != null && fromUser) {
                mediaPlayer.seekTo(h);
            }
        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {
            mediaPlayer.seekTo((int) seekBar.getProgress());

        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {

        }
    };
    View.OnClickListener GeniusClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openGeniusFragment(mGenius_file);

        }
    };
    View.OnClickListener LyricsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openLyricsFragment(mLyric_file);

        }
    };
    View.OnClickListener BackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(PlayerActivity.this, playlist_Activity.class));


        }
    };
    View.OnClickListener PauseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //seekBar.setMax(totalLength);
            seekBar.setMax(mediaPlayer.getDuration());

            if (mediaPlayer.isPlaying()) {

                mPause_btn.setBackgroundResource(R.drawable.play_button);
                mediaPlayer.pause();
            } else if (!mediaPlayer.isPlaying()) {
                mPause_btn.setBackgroundResource(R.drawable.pause_button);
                mediaPlayer.start();
            }

        }
    };
    ////--------------------------------------LISTENERS------------------------------------------/////
    ////--------------------------------------NOTIFICATION INTENT------------------------------------------/////

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStop() {

        super.onStop();

        if(!mediaPlayer.isPlaying()){
            return;
        }else{

            ServiceConnection serviceConnection=new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    Log.d(TAG, "onServiceConnected: Service Connected");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };


            Intent intentExtra=new Intent(this,NotificationActionService.class);
//            intentExtra.putStringArrayListExtra(getString(R.string.songslist),mSongs_list);
//            intentExtra.putExtra(getString(R.string.current_index),mCurrentIndex);


            //ContextCompat.startForegroundService(this,intentExtra);
            mContext.startService(intentExtra);
            Log.d(TAG, "onStop:  intentExtra started");

            displayNotification(mContext);
        }

    }
    private void displayNotification(Context context) {

        


        Bitmap artwork = BitmapFactory.decodeResource(getResources(), R.drawable.songs_thumbnail);







        pauseIntent = new Intent(this, NotificationActionService.class)
                .setAction(ACTION_PAUSE);
        playIntent = new Intent(this, NotificationActionService.class)
                .setAction(ACTION_PLAY);
        stopIntent = new Intent(this, NotificationActionService.class)
                .setAction(ACTION_STOP);
        nextIntent = new Intent(this, NotificationActionService.class)
                .setAction(ACTION_NEXT);
        prevIntent = new Intent(this, NotificationActionService.class)
                .setAction(ACTION_PREV);


        PendingIntent pausePendingIntent = PendingIntent.getService(context, 0,
                pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent playPendingIntent = PendingIntent.getService(context, 1,
                playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stopPendingIntent= PendingIntent.getService(context, 2,
                stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nextPendingIntent= PendingIntent.getService(context, 3,
                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent prevPendingIntent= PendingIntent.getService(context, 4,
                prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);




        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_play)
                        .setContentTitle("Why")
                        .setLargeIcon(artwork)
                        .setContentText("NF")

                        .addAction(new NotificationCompat.Action(R.drawable.ic_prev,
                                "PrevAction", prevPendingIntent))

                        .addAction(new NotificationCompat.Action(R.drawable.ic_pause,
                                "PauseAction", pausePendingIntent))

                        .addAction(new NotificationCompat.Action(R.drawable.ic_play,
                                "PlayAction", playPendingIntent))

                        .addAction(new NotificationCompat.Action(R.drawable.ic_stop,
                                "StopAction", stopPendingIntent))

                        .addAction(new NotificationCompat.Action(R.drawable.ic_next,
                                "NextAction", nextPendingIntent))

                        .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0,2,4)
                                .setMediaSession(mediaSessionCompat.getSessionToken())
                        )
                        .setSubText("Playing")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());



    }
    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
            Log.d(TAG, "NotificationActionService: IntentService ");

        }

        @Override
        public void onCreate() {
            super.onCreate();
            Log.d(TAG, "onCreate: IntentService");

          

        }

        ArrayList<String> songs_list=new ArrayList<>();
        int index_current;

        public void setIndex_current(int index_current) {
            this.index_current = index_current;
        }

        public int getIndex_current() {
            return index_current;
        }

        public ArrayList<String> getSongs_list() {
            return songs_list;
        }

        public void setSongs_list(ArrayList<String> songs_list) {
            this.songs_list = songs_list;
        }

        public void playSongNotification(int index){


           String songName = getSongs_list().get(index);
            String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Documents/";

            final String song_full = FOLDER_PATH + songName;

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();

                Log.d(TAG, "playSongNotification: player not null");
            }
            try {

                Log.d(TAG, "playSongNotification: creating new player");
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(song_full);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        Log.d(TAG, "onPrepared: prepared");
                        mp.start();

                    }
                });

            }catch (Exception e){
                Log.d(TAG, "playSongNotification: "+e.getMessage());
            }
        }


        @Override
        public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

            Log.d(TAG, "onStartCommand: IntentService");
            
            if(intent.getAction()==ACTION_NEXT){

                setIndex_current(getCurrentIndexPref(getString(R.string.shared_current_index),this));
                setSongs_list(getArrayList(getString(R.string.shared_array_list_key),this));
                Log.d(TAG, "onStartCommand: NEXT");
            }
            if(intent.getAction()==ACTION_PREV){
                Log.d(TAG, "onStartCommand: PAUSE");
                setIndex_current(getCurrentIndexPref(getString(R.string.shared_current_index),this));
                setSongs_list(getArrayList(getString(R.string.shared_array_list_key),this));
            }

            return super.onStartCommand(intent, flags, startId);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            Log.d(TAG, "onBind: IntentService");
            return super.onBind(intent);
        }

        @Override
        protected void onHandleIntent(Intent intent) {


            //PAUSE
            if(intent.getAction()==(ACTION_PAUSE)){
                Log.d(TAG, "onHandleIntent: ACTION PAUSE array : ");
                mediaPlayer.pause();

            }

            //PLAY
            if(intent.getAction()==(ACTION_PLAY)){
                Log.d(TAG, "onHandleIntent: ACTION_PLAY");
                if(mediaPlayer.isPlaying()){
                    return;
                }else{
                    mediaPlayer.start();
                }
            }

            //STOP
            if(intent.getAction()==(ACTION_STOP)){
                Log.d(TAG, "onHandleIntent: ACTION_STOP");
                mediaPlayer.stop();
                mediaPlayer.release();
                NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
                stopSelf();
            }

            //NEXT
            if(intent.getAction()==(ACTION_NEXT)){
                Log.d(TAG, "onHandleIntent: ACTION_NEXT");
                int index=getIndex_current();
                index++;
                index %= getSongs_list().size();
                           // playerActivity.setSongName(mCurrentIndex);
                playSongNotification(index);
                Log.d(TAG, "ACTION_NEXT: incremented index : "+index);
                saveCurrentIndexPref(getString(R.string.shared_current_index),index,this);
            }

            //PREVIOUS
            if(intent.getAction()==(ACTION_PREV)){
                Log.d(TAG, "onHandleIntent: ACTION_PREV");
                int index=getIndex_current();
                index = index > 0 ? index - 1 : getSongs_list().size() - 1;
                playSongNotification(index);
                Log.d(TAG, "ACTION_NEXT: decremented index : "+index);
                saveCurrentIndexPref(getString(R.string.shared_current_index),index,this);
            }


                }
        }
    }
    ////--------------------------------------NOTIFICATION INTENT------------------------------------------/////




