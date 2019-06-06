package com.homie.nf.Songs;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
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
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.homie.nf.R;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import me.tankery.lib.circularseekbar.CircularSeekBar;

import static android.media.audiofx.AudioEffect.CONTENT_TYPE_MUSIC;
import static android.media.audiofx.AudioEffect.EXTRA_AUDIO_SESSION;
import static android.media.audiofx.AudioEffect.EXTRA_CONTENT_TYPE;


public class PlayerActivity extends AppCompatActivity{

    private static final String TAG = "PlayerActivity";
    public static final int DEFAULT_INT_VALUE = 0;
    private Context mContext = PlayerActivity.this;
    public static MediaPlayer mediaPlayer;
    private CircularSeekBar seekBar;
    private Button mPause_btn, mGenius_btn, mLyrics_btn,mNext_btn, mPrev_btn;
    private TextView mSongname_view, mTotal_duration_view, mCurrent_duration_view;
    private String mLyric_file = "", mGenius_file = "";
    private Intent intent;
    private static ArrayList<String> mSongs_list;
    private ImageView mBack_arrow,mEquilizer;

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
    private Intent pauseIntent,playIntent,stopIntent,nextIntent,prevIntent;

    //audio focus change
    private static AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;

    //
    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce the volume instead of stopping playback.
    private static final float VOLUME_DUCK = 0.2f;
    // The volume we set the media player when we have audio focus.
    private static final float VOLUME_NORMAL = 1.0f;
    // we don't have audio focus, and can't duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    // we don't have focus, but can duck (play at a low volume)
    private static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    // we have full audio focus
    private static final int AUDIO_FOCUSED = 2;
    private static int mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
    private static AudioManager mAudioManager;


    //equilizer
    public static NotificationCompat.Builder notificationBuilder;
    public static NotificationManagerCompat notificationManager;

    private static final int REQUEST_EQ = 0;




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

        //audio focus change
        audioFocusListener();
        tryToGetAudioFocus(this);
        
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
        Log.d(TAG, "onCreate: Reciever Registered");


        mEquilizer.setOnClickListener(OpenEqualizerListener);

    }



    public void openEqualizer() {

        Intent eqIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        eqIntent.putExtra(EXTRA_CONTENT_TYPE, CONTENT_TYPE_MUSIC);
        eqIntent.putExtra(EXTRA_AUDIO_SESSION, CONTENT_TYPE_MUSIC);

        if ((eqIntent.resolveActivity(getPackageManager()) != null)) {
            startActivityForResult(eqIntent, REQUEST_EQ);
            //finish();
        } else {
            quickReturn();
        }
    }
    public void quickReturn() {
        Intent goHome = new Intent(Intent.ACTION_MAIN);
        goHome.addCategory(Intent.CATEGORY_HOME);
        goHome.setPackage("com.android.launcher");
        goHome.addCategory(Intent.CATEGORY_LAUNCHER);
        goHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goHome);
    }
    //-------------------------------AUDIO FOCUS CHANGE LISTENER----------------------/////
    //-------------------------------AUDIO FOCUS CHANGE LISTENER----------------------/////


    public static void tryToGetAudioFocus(Context context){
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int focusRequest = mAudioManager.requestAudioFocus(
                mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        switch (focusRequest) {
            case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                // don't start playback
                Log.d(TAG, "onCreate: AUDIOFOCUS_REQUEST_FAILED");
            case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                // actually start playback
                Log.d(TAG, "onCreate: AUDIOFOCUS_REQUEST_GRANTED");
        }

    }
    private static void configurePlayerState() {

        if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_NO_DUCK) {
            // We don't have audio focus and can't duck, so we have to pause
            try {
                mediaPlayer.stop();
            }catch (NullPointerException e){

                Log.d(TAG, "onHandleIntent: NullPointerException");
            }catch (IllegalArgumentException e){

                Log.d(TAG, "onHandleIntent: IllegalArgumentException");
            }catch (IllegalStateException e){
                Log.d(TAG, "onHandleIntent: IllegalStateException"+e.getMessage());
            }
        } else {

            if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_CAN_DUCK) {
                // We're permitted to play, but only if we 'duck', ie: play softly
                try {
                    mediaPlayer.setVolume(VOLUME_DUCK, VOLUME_DUCK);
                }catch (Exception e){
                    Log.d(TAG, "configurePlayerState: Exception"+e.getMessage());
                }
            } else {
                try {
                    mediaPlayer.setVolume(VOLUME_NORMAL, VOLUME_NORMAL);

                }catch (Exception e){
                    Log.d(TAG, "configurePlayerState: Exception"+e.getMessage());
                }
            }
        }
    }
    public static void audioFocusListener(){

//…
        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        mCurrentAudioFocusState = AUDIO_FOCUSED;
                        Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_GAIN");
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
                        mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK;
                        Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // Lost audio focus, but will gain it back (shortly), so note whether
                        // playback should resume
                        mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                        Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT");
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        // Lost audio focus, probably "permanently"
                        mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                        Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS");
                        break;
                }
                if (mediaPlayer != null) {
                    // Update the player state based on the change
                    configurePlayerState();
                }
            }
        };
    }
    //-------------------------------AUDIO FOCUS CHANGE LISTENER----------------------/////
    //-------------------------------AUDIO FOCUS CHANGE LISTENER----------------------/////


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
        //saveArrayList(mSongs_list,getString(R.string.shared_array_list_key),mContext);
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
        mEquilizer = findViewById(R.id.equilizer);


    }



    public void openGeniusFragment(String genius_url) {
        genius_fragment fragment=new genius_fragment();
        Bundle args=new Bundle();
        args.putString(getString(R.string.genius_url),genius_url);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
        transaction.replace(R.id.frameLayout,fragment);
        transaction.addToBackStack(getString(R.string.GENIUS_FRAGMENT));
        transaction.commit();

    }

    public void openLyricsFragment(String lyrics_file_name) {


        lyrics_frag fragment=new lyrics_frag();
        Bundle args=new Bundle();
        args.putString(getString(R.string.lyrics_file_name),lyrics_file_name);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
        transaction.replace(R.id.frameLayout,fragment);
        transaction.addToBackStack(getString(R.string.lyrics_fragment));
        transaction.commit();
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


                    }catch (NullPointerException e){

                        Log.d(TAG, "onHandleIntent: NullPointerException");
                    }catch (IllegalArgumentException e){

                        Log.d(TAG, "onHandleIntent: IllegalArgumentException");
                    }catch (IllegalStateException e){
                        Log.d(TAG, "onHandleIntent: IllegalStateException"+e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "onHandleIntent: IOException"+e.getMessage());
                    }
                }

            };
            myHandler.postDelayed(runnable, 100);

        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSplit(mContext);

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
                            try {
                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                //set current duration to textview
                                setDuration(mediaPlayer.getCurrentPosition(), mCurrent_duration_view);
                            }catch (NullPointerException e){
                                Log.d(TAG, "run: NullPointerException"+e.getMessage());
                            }catch (Exception e){
                                Log.d(TAG, "run: Exception"+e.getMessage());
                            }

                        }
                    });

                    //Log.d("Runwa", "run: " + mediaPlayer.getCurrentPosition());
                }
            }
        }

    }


    ////--------------------------------------LISTENERS------------------------------------------/////

    View.OnClickListener OpenEqualizerListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: Open Equalizers");
            openEqualizer();
        }
    };
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
            startActivity(new Intent(PlayerActivity.this, playlist_Activity.class)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
    };
    View.OnClickListener PauseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //seekBar.setMax(totalLength);
            try {
                seekBar.setMax(mediaPlayer.getDuration());

                if (mediaPlayer.isPlaying()) {

                    mPause_btn.setBackgroundResource(R.drawable.play_button);
                    mediaPlayer.pause();
                } else if (!mediaPlayer.isPlaying()) {
                    mPause_btn.setBackgroundResource(R.drawable.pause_button);
                    mediaPlayer.start();
                }
            }catch (NullPointerException e){

                Log.d(TAG, "onHandleIntent: NullPointerException");
            }catch (IllegalArgumentException e){

                Log.d(TAG, "onHandleIntent: IllegalArgumentException");
            }catch (IllegalStateException e){
                Log.d(TAG, "onHandleIntent: IllegalStateException"+e.getMessage());
            }


        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNoisyReceiver);
        Log.d(TAG, "onDestroy: Reciever Unregistered");
    }
    ////--------------------------------------LISTENERS------------------------------------------/////
    ////--------------------------------------NOTIFICATION INTENT------------------------------------------/////

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStop() {

        super.onStop();

        try {
            if(!mediaPlayer.isPlaying()){
                return;
            }else{

                Intent intentExtra=new Intent(this, NotificationActionService.class);
//            intentExtra.putStringArrayListExtra(getString(R.string.songslist),mSongs_list);
//            intentExtra.putExtra(getString(R.string.current_index),mCurrentIndex);


                //ContextCompat.startForegroundService(this,intentExtra);
                mContext.startService(intentExtra);
                Log.d(TAG, "onStop:  intentExtra started");

                displayNotification(mContext);
            }
        }catch (RuntimeException e){
            Log.d(TAG, "onStop: RuntimeException"+e.getMessage());
        }catch (Exception e){
            Log.d(TAG, "onStop: Exception"+e.getMessage());
        }
      

    }
    public static String songNameNotification(int index,Context context){


        ArrayList<String> songs=getArrayList(context.getString(R.string.shared_array_list_key),context);
        //int index=getCurrentIndexPref(getString(R.string.current_index),this);
        String songname=songs.get(index);
        String songnameminusMP3=songname.replace(".mp3","");
        return songnameminusMP3;

    }
    private void displayNotification(Context context) {




        Bitmap artwork = BitmapFactory.decodeResource(getResources(), R.drawable.notifi_thumbnail);

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





         notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_play)
                        .setContentTitle(songNameNotification
                                (getCurrentIndexPref(getString(R.string.shared_current_index),this),this))
                        .setLargeIcon(artwork)
                        .setContentText("NF")


                        .addAction(new NotificationCompat.Action(R.drawable.ic_prev,
                                "PrevAction", prevPendingIntent))

                        .addAction(new NotificationCompat.Action(R.drawable.ic_pause,
                                "PauseAction", pausePendingIntent))

                        .addAction(new NotificationCompat.Action(R.drawable.ic_play,
                                "PlayAction", playPendingIntent))

                        .addAction(new NotificationCompat.Action(R.drawable.ic_next,
                                "NextAction", nextPendingIntent))

                        .addAction(new NotificationCompat.Action(R.drawable.ic_cross,
                                "StopAction", stopPendingIntent))

                        .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0,2,3,4)
                                .setMediaSession(mediaSessionCompat.getSessionToken()))
                        .setSubText("Playing")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                ;


        notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());




    }


    ////////------------------------------------INTENT SERVICE-------------------////////////
    ////////------------------------------------INTENT SERVICE-------------------////////////
    public static class NotificationActionService extends IntentService {
        private static final String TAG = "NotificationActionServi";
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

            String songName=null;
            try {
                songName = getSongs_list().get(index);
            }catch (NullPointerException e){

                Log.d(TAG, "onHandleIntent: NullPointerException"+e.getMessage());
            }catch (IllegalArgumentException e){
                Log.d(TAG, "onHandleIntent: IllegalArgumentException"+e.getMessage());

            }

            String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Documents/";

            final String song_full = FOLDER_PATH + songName;

            if (mediaPlayer != null || mediaPlayer.isPlaying()) {
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

        public void updateNotificationName(int index){
            notificationBuilder.setContentTitle(PlayerActivity
                    .songNameNotification(index, NotificationActionService.this));
            PlayerActivity.songNameNotification(index, NotificationActionService.this);
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
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
            if(intent.getAction()==ACTION_PLAY){
                Log.d(TAG, "onStartCommand: PLAY");
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

            Handler mHandler = new Handler(getMainLooper());

//            if(intent.getAction()==android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY){
//                Log.d(TAG, "onHandleIntent: ACTION_AUDIO_BECOMING_NOISY");
//                if(mediaPlayer!=null){
//                    mediaPlayer.pause();
//                }
//                else {
//                    return;
//                }
//            }
            //PAUSE
            if(intent.getAction()==(ACTION_PAUSE)){
                Log.d(TAG, "onHandleIntent: ACTION PAUSE array : ");
                try {
                    //Handler mHandler = new Handler(getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(), "Pause Pressed", Toast.LENGTH_SHORT).show();
                            if(mediaPlayer!=null && mediaPlayer.isPlaying()){
                                mediaPlayer.pause();
                            }else {
                                Toast.makeText(NotificationActionService.this, "App is Terminated", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    });

                }catch (NullPointerException e){

                    Log.d(TAG, "onHandleIntent: NullPointerException");
                }catch (IllegalArgumentException e){

                    Log.d(TAG, "onHandleIntent: IllegalArgumentException");
                }catch (IllegalStateException e){
                    Log.d(TAG, "onHandleIntent: IllegalStateException"+e.getMessage());
                }


            }

            //PLAY
            if(intent.getAction()==(ACTION_PLAY)){
                Log.d(TAG, "onHandleIntent: ACTION_PLAY");
                try {
                    //Handler mHandler = new Handler(getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(), "Play Pressed", Toast.LENGTH_SHORT).show();
                            if(mediaPlayer!=null){
                                return;
                            }else if(mediaPlayer==null){
                                Toast.makeText(NotificationActionService.this, "App is Terminated", Toast.LENGTH_LONG).show();
                            }else if(!mediaPlayer.isPlaying()){
                                mediaPlayer.start();
                            }
                        }
                    });

                }catch (NullPointerException e){

                    Log.d(TAG, "onHandleIntent: NullPointerException");
                }catch (IllegalArgumentException e){

                    Log.d(TAG, "onHandleIntent: IllegalArgumentException");
                }catch (IllegalStateException e){
                    Log.d(TAG, "onHandleIntent: IllegalStateException"+e.getMessage());
                }

            }

            //STOP
            if(intent.getAction()==(ACTION_STOP)){
                Log.d(TAG, "onHandleIntent: ACTION_STOP");
                try {
                    //Handler mHandler = new Handler(getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(), "Stop Pressed", Toast.LENGTH_SHORT).show();
                            if(mediaPlayer!=null){
                                mediaPlayer.stop();
                                NotificationManagerCompat.from(NotificationActionService.this).cancel(NOTIFICATION_ID);
                                stopSelf();
                            }
                            else {
                                NotificationManagerCompat.from(NotificationActionService.this).cancel(NOTIFICATION_ID);
                                stopSelf();
                            }
                        }
                    });

                }catch (NullPointerException e){

                    Log.d(TAG, "onHandleIntent: NullPointerException");
                }catch (IllegalArgumentException e){

                    Log.d(TAG, "onHandleIntent: IllegalArgumentException");
                }catch (IllegalStateException e){
                    Log.d(TAG, "onHandleIntent: IllegalStateException"+e.getMessage());
                }


            }

            //NEXT
            if(intent.getAction()==(ACTION_NEXT)){
                Log.d(TAG, "onHandleIntent: ACTION_NEXT");
                try {


                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(NotificationActionService.this, "Next Pressed", Toast.LENGTH_LONG).show();
                            if(mediaPlayer!=null){
                                int index=getIndex_current();
                                index++;
                                index %= getSongs_list().size();
                                // playerActivity.setSongName(mCurrentIndex);
                                updateNotificationName(index);
                                playSongNotification(index);

                                Log.d(TAG, "ACTION_NEXT: incremented index : "+index);
                                saveCurrentIndexPref(getString(R.string.shared_current_index),index, NotificationActionService.this);
                            }else {
                                Toast.makeText(NotificationActionService.this, "App is Terminated", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    });

                }catch (NullPointerException e){

                    Log.d(TAG, "onHandleIntent: NullPointerException");
                }catch (IllegalArgumentException e){

                    Log.d(TAG, "onHandleIntent: IllegalArgumentException");
                }catch (IllegalStateException e){
                    Log.d(TAG, "onHandleIntent: IllegalStateException"+e.getMessage());
                }

            }

            //PREVIOUS
            if(intent.getAction()==(ACTION_PREV)){
                try {
                    // Handler mHandler = new Handler(getMainLooper());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(), "Prev Pressed", Toast.LENGTH_SHORT).show();
                            if(mediaPlayer!=null){
                                Log.d(TAG, "onHandleIntent: ACTION_PREV");
                                int index=getIndex_current();
                                index = index > 0 ? index - 1 : getSongs_list().size() - 1;
                                playSongNotification(index);
                                Log.d(TAG, "ACTION_NEXT: decremented index : "+index);
                                updateNotificationName(index);
                                saveCurrentIndexPref(getString(R.string.shared_current_index),index, NotificationActionService.this);
                            }else {
                                Toast.makeText(NotificationActionService.this, "App is Terminated", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    });

                }catch (NullPointerException e){

                    Log.d(TAG, "onHandleIntent: NullPointerException");
                }catch (IllegalArgumentException e){

                    Log.d(TAG, "onHandleIntent: IllegalArgumentException");
                }catch (IllegalStateException e){
                    Log.d(TAG, "onHandleIntent: IllegalStateException"+e.getMessage());
                }

            }


        }
    }

    ////////------------------------------------INTENT SERVICE-------------------////////////
    ////////------------------------------------INTENT SERVICE-------------------////////////


    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( mediaPlayer != null && mediaPlayer.isPlaying() ) {
                mediaPlayer.pause();
            }
        }
    };

    }
    ////--------------------------------------NOTIFICATION INTENT------------------------------------------/////




