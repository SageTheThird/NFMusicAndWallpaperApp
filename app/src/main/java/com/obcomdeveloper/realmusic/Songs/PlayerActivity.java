package com.obcomdeveloper.realmusic.Songs;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.ads.InterstitialAd;
import com.obcomdeveloper.realmusic.Adapters.PlayerPagerAdapter;
import com.obcomdeveloper.realmusic.Extras.Extras;
import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Utils.Ads;
import com.obcomdeveloper.realmusic.Utils.CustomViewPager;
import com.obcomdeveloper.realmusic.Utils.HeadphonesReciever;
import com.obcomdeveloper.realmusic.Utils.SharedPreferences;
import com.obcomdeveloper.realmusic.Utils.UniversalImageLoader;
import com.obcomdeveloper.realmusic.Utils.ViewUtilities;
import com.obcomdeveloper.realmusic.Utils.ZoomOutTransformation;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import static android.media.audiofx.AudioEffect.CONTENT_TYPE_MUSIC;
import static android.media.audiofx.AudioEffect.EXTRA_AUDIO_SESSION;
import static android.media.audiofx.AudioEffect.EXTRA_CONTENT_TYPE;


public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";

    //Notification
    public static final int NOTIFICATION_ID = 1;
    public static final String ACTION_PAUSE = "PauseAction";
    public static final String ACTION_PLAY = "PlayAction";
    public static final String ACTION_STOP = "StopAction";
    public static final String ACTION_NEXT = "NextAction";
    public static final String ACTION_PREV = "PrevAction";
    public static final int PAUSE_INTENT_REQUEST_CODE=0;
    public static final int PLAY_INTENT_REQUEST_CODE=1;
    public static final int STOP_INTENT_REQUEST_CODE=2;
    public static final int NEXT_INTENT_REQUEST_CODE=3;
    public static final int PREV_INTENT_REQUEST_CODE=4;
    public static final int CLICK_INTEN_REQUEST_CODE=5;
    public static final int DEFAULT_INT_VALUE = 0;
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
    private static final int REQUEST_EQ = 0;
    //activity Identifiers
    public static final int PLAYLIST_ACTIVITY_IDENTIFIER=20;
    public static final int EXTRAS_ACTIVITY_IDENTIFIER=10;
    private Context mContext = PlayerActivity.this;
    public static MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Button mPause_btn,mRepeat,mShuffle,mNext_btn,mPrev_btn;
    private TextView mSongname_view, mTotal_duration_view, mCurrent_duration_view, mArtist_tv;
    private String mLyric_file = "", mGenius_file = "";
    private Intent intent;
    private static ArrayList<String> mSongs_list;
    private ImageView mBack_arrow,mEquilizer, mGenius_btn, mLyrics_btn,mBackground_iv;
    private Handler myHandler = new Handler();
    //for song list
    private static int mCurrentIndex;
    MediaSessionCompat mediaSessionCompat;
    //action_intents
    private Intent pauseIntent,playIntent,stopIntent,nextIntent,prevIntent,clickIntent;
    //audio focus change
    private static AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private static AudioManager mAudioManager;
    //equilizer
    public static NotificationCompat.Builder notificationBuilder;
    public static NotificationManagerCompat notificationManager;
    public static int activityIdentifier;
    private boolean isButtonClicked = false;
    private boolean resumeSong=false;
    private boolean repeatOn=false;
    public static HeadphonesReciever receiver;
    private static boolean mNoisyRecieverOn=false;
    //ads
    private Ads ads;
    private InterstitialAd interstitialAd;
    //test
    private ImageView blurred_iv;
    private CustomViewPager viewPager;
    private com.obcomdeveloper.realmusic.Utils.SharedPreferences mSharedPrefs;
    private List<Song> existing_songs_ojects_list;
    private boolean trigger_on_page;
    private PagerAdapter adapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.test_player);


        mSharedPrefs = new com.obcomdeveloper.realmusic.Utils.SharedPreferences(PlayerActivity.this);
        trigger_on_page=true;
        mSharedPrefs.saveBoolean(getString(R.string.trigger_on_page),trigger_on_page);


        interstitialAd=new InterstitialAd(this);
        mediaSessionCompat = new MediaSessionCompat(this, "someTag");



        mSongs_list = new ArrayList<>();

        //methods
        initWidgets();
        getIncomingIntent();
        if(activityIdentifier==PLAYLIST_ACTIVITY_IDENTIFIER){

            PlaylistActivity.playlist_adapter.notifyDataSetChanged();

        }else {

            Extras.adapter.notifyDataSetChanged();

        }


        playSong(mCurrentIndex,activityIdentifier);


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

        initFirstGuide();

        //audio focus change
        audioFocusListener();
        tryToGetAudioFocus(this);

        //listeners
        mEquilizer.setOnClickListener(OpenEqualizerListener);
        mRepeat.setOnClickListener(RepeatClickListner);
        mShuffle.setOnClickListener(ShuffleClickListener);


        if(mSharedPrefs.getBoolean(getString(R.string.repeat_state),false)){
            mRepeat.setBackgroundResource(R.drawable.ic_repeat_one);
        }else {
            mRepeat.setBackgroundResource(R.drawable.ic_repeat_not);
        }


        registerReciever();


        //ads
        ads=new Ads();
        ads.initAdMob(this);
        ads.setupInterstitial(this,getString(R.string.interstitial_ad_test_unit_id),interstitialAd);

        blurredBackgroundSetup();
        viewpagerSetup();


    }

    private void initFirstGuide() {

        if(mSharedPrefs.getBoolean(getString(R.string.firstGuideViewPager), true)) {

            guideBuilder(this);

            mSharedPrefs.saveBoolean(getString(R.string.firstGuideViewPager), false);

        }

    }

    private void blurredBackgroundSetup() {
        String thumbnail=existing_songs_ojects_list.get(mCurrentIndex).getThumbnail();
        UniversalImageLoader.setBlurredImage(thumbnail,
                blurred_iv,null,"",seekBar,mLyrics_btn,mGenius_btn);

    }

    private void viewpagerSetup() {
        adapter = new PlayerPagerAdapter(existing_songs_ojects_list,
                PlayerActivity.this);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(mCurrentIndex);
        ZoomOutTransformation zoomOutTransformation=new ZoomOutTransformation();
        //viewPager.setPageTransformer(true,zoomOutTransformation);

        viewPager.setOnPageChangeListener(new CustomViewPager.OnPageChangeListener() {

            int previousPosition=viewPager.getCurrentItem();


            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int i) {

                if(mSharedPrefs.getBoolean(getString(R.string.trigger_on_page),false)){

                    if(i > previousPosition){
                        Log.d(TAG, "onPageSelected: Next Swipe");
                        nextTrack();
                        getThumbnailofPlayingIndex();
                        previousPosition=i;

                    }else{
                        Log.d(TAG, "onPageSelected: Previous Swipe");
                        previousTrack();
                        getThumbnailofPlayingIndex();
                        previousPosition=i;

                    }
                }else {
                    trigger_on_page=true;
                    mSharedPrefs.saveBoolean(getString(R.string.trigger_on_page),trigger_on_page);
                }


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void guideBuilder(Context context){

            new GuideView.Builder(context)
                    .setTitle("SWIPE")
                    .setContentText("For Next or Previous Track\n(Load Songs First)")
                    .setTargetView(viewPager)
                    .setIndicatorHeight(100f)
                    .setContentTextSize(15)
                    .setTitleTextSize(17)
                    //.setContentTypeFace(Typeface)//optional
                    //.setTitleTypeFace()//optional
                    .setDismissType(DismissType.anywhere) //optional - default dismissible by TargetView
                    .build()
                    .show();

    }

    private void getThumbnailofPlayingIndex(){


        String thumbnail=existing_songs_ojects_list.get(mCurrentIndex).getThumbnail();
        UniversalImageLoader.setBlurredImage(thumbnail,
                blurred_iv,null,"",seekBar,mLyrics_btn,mGenius_btn);


    }
    private void nextTrack(){

        mCurrentIndex++;
        mCurrentIndex %= mSongs_list.size();
        setSongName(mCurrentIndex);
        playSong(mCurrentIndex,activityIdentifier);
        //lyrics
        setupLyrics(mCurrentIndex);
        //genius
        setupGenius(mCurrentIndex);
        mSharedPrefs.saveInt(getString(R.string.shared_current_index),mCurrentIndex);
        if(activityIdentifier==PLAYLIST_ACTIVITY_IDENTIFIER){
            PlaylistActivity.playlist_adapter.notifyDataSetChanged();
        }else {
            Extras.adapter.notifyDataSetChanged();
        }
        updateNotification();

    }
    private void previousTrack(){

        mCurrentIndex = mCurrentIndex > 0 ? mCurrentIndex - 1 : mSongs_list.size() - 1;
        setSongName(mCurrentIndex);
        playSong(mCurrentIndex,activityIdentifier);
        setupLyrics(mCurrentIndex);
        setupGenius(mCurrentIndex);
        mSharedPrefs.saveInt(getString(R.string.shared_current_index),mCurrentIndex);
        if(activityIdentifier==PLAYLIST_ACTIVITY_IDENTIFIER){
            PlaylistActivity.playlist_adapter.notifyDataSetChanged();
        }else {
            Extras.adapter.notifyDataSetChanged();
        }
        updateNotification();
    }

    private void registerReciever() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        receiver = new HeadphonesReciever();
        try{
            registerReceiver( receiver, intentFilter );
        }catch (Exception e){
            Log.d(TAG, "registerReciever: Exception "+e.getMessage());
        }

        mNoisyRecieverOn=true;
        Log.d(TAG, "registerReciever: Noisy Reciever Registered");
    }


    public void openEqualizer() {

        Intent eqIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        eqIntent.putExtra(EXTRA_CONTENT_TYPE, CONTENT_TYPE_MUSIC);
        eqIntent.putExtra(EXTRA_AUDIO_SESSION, CONTENT_TYPE_MUSIC);

        if ((eqIntent.resolveActivity(getPackageManager()) != null)) {
            startActivityForResult(eqIntent, REQUEST_EQ);
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
                mediaPlayer.pause();
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



    private void getIncomingIntent() {
        existing_songs_ojects_list=new ArrayList<>();
        intent = getIntent();



        mSongs_list = intent.getStringArrayListExtra(getString(R.string.folder_songs_list));

        if(intent.hasExtra(getString(R.string.coming_from_extra_activity))){
            activityIdentifier=intent.getIntExtra(getString(R.string.coming_from_extra_activity_int),1);

        }
        if(intent.hasExtra(getString(R.string.coming_from_playlist_activity))){
            activityIdentifier=intent.getIntExtra(getString(R.string.coming_from_playlist_activity_int),2);
        }

        mCurrentIndex = intent.getIntExtra(getString(R.string.current_index), DEFAULT_INT_VALUE);

        String current_song_tapped=mSongs_list.get(mCurrentIndex);

        if(mCurrentIndex == mSharedPrefs.getInt(getString(R.string.shared_current_index),0)
        && current_song_tapped.equals(mSharedPrefs.getString(getString(R.string.current_song_name),"null"))){
            Log.d(TAG, "getIncomingIntent: Same Song");
            resumeSong=true;
            if(mSongs_list.size() == 1){
                mSharedPrefs.saveString(getString(R.string.current_song_name),current_song_tapped);
            }


        }else {
            mSharedPrefs.saveInt(getString(R.string.shared_current_index),mCurrentIndex);
            mSharedPrefs.saveString(getString(R.string.current_song_name),current_song_tapped);
        }


        existing_songs_ojects_list=intent.getParcelableArrayListExtra(getString(R.string.existing_songs_object_list));
        mSharedPrefs.saveObjectsList(existing_songs_ojects_list,getString(R.string.existing_objects_list_prefs));
        Log.d(TAG, "getIncomingIntent: existing_songs_ojects_list : "+existing_songs_ojects_list.size());



        //saving
        mSharedPrefs.saveList(mSongs_list,getString(R.string.shared_array_list_key));

        //songTextView setup
        setSongName(mCurrentIndex);
        //setup lyrics
        setupLyrics(mCurrentIndex);
        //setup Genius
        setupGenius(mCurrentIndex);

        updateNotification();

        ////////-------------------

    }

    private void setupGenius(int mCurrentIndex) {

        mGenius_file=existing_songs_ojects_list.get(mCurrentIndex).getGenius_url();

    }

    private void setupLyrics(int mCurrentIndex) {

        mLyric_file=existing_songs_ojects_list.get(mCurrentIndex).getSong_name()+".txt";
        Log.d(TAG, "setupLyrics: mLyrics_file : "+mLyric_file);

    }

    public void setSongName(int index) {
        String currentName = existing_songs_ojects_list.get(index).getSong_name();
        if(activityIdentifier == EXTRAS_ACTIVITY_IDENTIFIER){
            String artist_name=existing_songs_ojects_list.get(index).getArtist_name();
            mArtist_tv.setText(artist_name);
        }else {
            mArtist_tv.setText("NF");
        }
        mSongname_view.setText(currentName);
        mSongname_view.setHorizontallyScrolling(true);
        mSongname_view.setSelected(true);


    }

    private void initWidgets() {
        mPause_btn = findViewById(R.id.pause);
        mSongname_view = findViewById(R.id.songtextView);
        seekBar = findViewById(R.id.horizantal_seekbar);
        mBack_arrow = findViewById(R.id.back_button);
        mGenius_btn = findViewById(R.id.geniusbutton);
        mLyrics_btn = findViewById(R.id.lyrics_button);
        mNext_btn = findViewById(R.id.nextBtn);
        mPrev_btn = findViewById(R.id.prevBtn);
        mTotal_duration_view = findViewById(R.id.fullDurationView);
        mCurrent_duration_view = findViewById(R.id.currentDuration);
        mEquilizer = findViewById(R.id.equilizer);
        mArtist_tv = findViewById(R.id.artist_tv);
        mRepeat = findViewById(R.id.repeat);
        mShuffle=findViewById(R.id.shuffle);

        //
        viewPager=findViewById(R.id.player_viewpager);
        blurred_iv=findViewById(R.id.blurred_iv_player);
        mBackground_iv=findViewById(R.id.backg_player);

        Picasso
                .with(this)
                .load(R.drawable.test_background_blurred)
                .resize(800, 800)
                .into(mBackground_iv);

    }





    public void openGeniusFragment(String genius_url) {
        GeniusFragment fragment=new GeniusFragment();
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


        LyricsFragment fragment=new LyricsFragment();
        Bundle args=new Bundle();
        args.putString(getString(R.string.lyrics_file_name),lyrics_file_name);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
        transaction.replace(R.id.frameLayout,fragment);
        transaction.addToBackStack(getString(R.string.lyrics_fragment));
        transaction.commit();
    }

    public void playSong(final int index, final int activityIdentifier) {
        Log.d(TAG, "playSong: Identifier : "+activityIdentifier);
        String songName = null;
        try {
            songName = existing_songs_ojects_list.get(index).getSong_name()+".mp3";
        } catch (IndexOutOfBoundsException e) {
            showToast("No offline files in directory");
            e.printStackTrace();
        }
        String FOLDER_PATH=null;
        if(activityIdentifier==PLAYLIST_ACTIVITY_IDENTIFIER){
            FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Documents/";
            Log.d(TAG, "playSong: Identifier : Playlist Path");
            mArtist_tv.setText("NF");
        }
        if(activityIdentifier==EXTRAS_ACTIVITY_IDENTIFIER){
            FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Extras/";
            Log.d(TAG, "playSong: Identifier : Extras Path : "+FOLDER_PATH);
            mArtist_tv.setText(existing_songs_ojects_list.get(mCurrentIndex).getArtist_name());
        }
        final String song_full = FOLDER_PATH + songName;

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {

                        if (mediaPlayer != null) {


                                if(resumeSong && mSongs_list.size()>1){
                                    Log.d(TAG, "run: resuming song");

                                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                                    seekBar.setMax(mediaPlayer.getDuration());
                                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                    setDuration(mediaPlayer.getDuration(), mTotal_duration_view);
                                    mPause_btn.setBackgroundResource(R.drawable.ic_pause);
                                    mediaPlayer.start();
                                    resumeSong=false;


                            }else {
                                mediaPlayer.stop();
                                mediaPlayer.reset();

                                mediaPlayer = new MediaPlayer();
                                mediaPlayer.setDataSource(song_full);
                                mediaPlayer.prepareAsync();
                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mPause_btn.setBackgroundResource(R.drawable.ic_pause);
                                        mp.start();
                                        seekBar.setProgress(0);
                                        //set max duration to seek_bar
                                        seekBar.setMax(mediaPlayer.getDuration());
                                        //set total duration to textview
                                        setDuration(mediaPlayer.getDuration(), mTotal_duration_view);
                                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mp) {

                                                Log.d(TAG, "onCompletion: upper On Completion");

                                                if(mRepeat.getBackground().getConstantState().equals(getResources()
                                                        .getDrawable(R.drawable.ic_repeat_one).getConstantState())) {
                                                    repeatOn = true;
                                                    mSharedPrefs.saveBoolean(getString(R.string.repeat_state), repeatOn);
                                                }else {
                                                    repeatOn = false;
                                                    mSharedPrefs.saveBoolean(getString(R.string.repeat_state), repeatOn);
                                                }

                                                if(mSharedPrefs.getBoolean(getString(R.string.repeat_state),false)){
                                                    //mRepeat.setBackgroundResource(R.drawable.ic_repeat_one);
                                                    setSongName(mCurrentIndex);
                                                    playSong(mCurrentIndex,activityIdentifier);
                                                }else{

                                                    if(mSongs_list.size() > 1){


                                                        nextTrack();
                                                        updateNotification();
                                                        getThumbnailofPlayingIndex();
                                                        updateViewPager();

                                                    }
                                                    }




                                            }
                                        });
                                        Log.d("Prog", "run: " + mediaPlayer.getDuration());
                                    }
                                });
                            }


                        }else {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setDataSource(song_full);
                            mediaPlayer.prepareAsync();
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mPause_btn.setBackgroundResource(R.drawable.ic_pause);
                                    mp.start();

                                    seekBar.setProgress(0);
                                    //set max duration to seek_bar
                                    seekBar.setMax(mediaPlayer.getDuration());
                                    //set total duration to textview
                                    setDuration(mediaPlayer.getDuration(), mTotal_duration_view);
                                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {

                                            Log.d(TAG, "onCompletion: downward On Completion");

                                            if(mRepeat.getBackground().getConstantState().equals(getResources()
                                                    .getDrawable(R.drawable.ic_repeat_one).getConstantState())) {
                                                repeatOn = true;
                                                mSharedPrefs.saveBoolean(getString(R.string.repeat_state), repeatOn);
                                            }else {
                                                repeatOn = false;
                                                mSharedPrefs.saveBoolean(getString(R.string.repeat_state), repeatOn);
                                            }

                                            if(mSharedPrefs.getBoolean(getString(R.string.repeat_state),false)){
                                                //mRepeat.setBackgroundResource(R.drawable.ic_repeat_one);
                                                setSongName(mCurrentIndex);
                                                playSong(mCurrentIndex,activityIdentifier);
                                            }else{

                                                if(mSongs_list.size() >1){

                                                    nextTrack();
                                                    updateNotification();
                                                    getThumbnailofPlayingIndex();
                                                    updateViewPager();


                                                }

                                            }
                                        }
                                    });
                                    Log.d("Prog", "run: " + mediaPlayer.getDuration());
                                }
                            });

                        }
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

    private void updateViewPager(){
        trigger_on_page=false;
        mSharedPrefs.saveBoolean(getString(R.string.trigger_on_page),trigger_on_page);
        ViewUtilities.waitForLayout(viewPager, new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(mCurrentIndex);
            }
        });
    }

    private void updateNotification(){
        try {
            if(notificationBuilder != null){
                Log.d(TAG, "onCompletion: updating notification name");
                notificationBuilder.setContentTitle(songNameNotification(mCurrentIndex,mContext));
                notificationBuilder.setContentText(songSubtext(mCurrentIndex,mContext));
                notificationManager.notify(NOTIFICATION_ID,notificationBuilder.build());
            }
        }catch (Exception e){
            Log.d(TAG, "updateNotification: Exception " +e.getMessage());
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

            nextTrack();
            getThumbnailofPlayingIndex();
            updateViewPager();
            //viewPager.setCurrentItem(mCurrentIndex,true);
            //viewPager.arrowScroll(View.FOCUS_RIGHT);
            //viewPager.arrowScroll(View.FOCUS_RIGHT);




        }
    };
    View.OnClickListener ShuffleClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: Shuffle");
            if (v.getId() == R.id.shuffle) {
                isButtonClicked = !isButtonClicked; // toggle the boolean flag
                v.setBackgroundResource(isButtonClicked ? R.drawable.ic_shuffle_pressed : R.drawable.ic_shuffle);
            }
        }
    };
    View.OnClickListener RepeatClickListner=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mRepeat.getBackground().getConstantState().equals(getResources()
                    .getDrawable(R.drawable.ic_repeat_one).getConstantState())){

                mRepeat.setBackgroundResource(R.drawable.ic_repeat_not);
                repeatOn=false;
                mSharedPrefs.saveBoolean(getString(R.string.repeat_state),repeatOn);

            }else{
                if(mSongs_list.size() > 1){
                    mRepeat.setBackgroundResource(R.drawable.ic_repeat_one);
                    repeatOn=true;
                    mSharedPrefs.saveBoolean(getString(R.string.repeat_state),repeatOn);
                }

            }
            Log.d(TAG, "onClick: Repeat");
//            if (v.getId() == R.id.repeat) {
//                isButtonClicked = !isButtonClicked; // toggle the boolean flag
//                v.setBackgroundResource(isButtonClicked ? R.drawable.ic_repeat_one : R.drawable.ic_repeat_not);
//            }
        }
    };
    View.OnClickListener PrevClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.d(TAG, "onClick: next button");
            previousTrack();
            getThumbnailofPlayingIndex();
            updateViewPager();
            //viewPager.setCurrentItem(mCurrentIndex,true);

        }
    };

    SeekBar.OnSeekBarChangeListener SeekBarChangeListener= new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //int h = Math.round(progress);

            if (mediaPlayer != null && fromUser) {
                mediaPlayer.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

            mediaPlayer.seekTo(seekBar.getProgress());
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };



    View.OnClickListener GeniusClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ads.loadinterstitial(interstitialAd);
                }
            });

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
            finish();
        }
    };
    View.OnClickListener PauseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //seekBar.setMax(totalLength);
            try {
                seekBar.setMax(mediaPlayer.getDuration());

                if (mediaPlayer.isPlaying()) {

                    mPause_btn.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                } else if (!mediaPlayer.isPlaying()) {
                    mPause_btn.setBackgroundResource(R.drawable.ic_pause);
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

        try{
            if(mNoisyRecieverOn){
                unregisterReceiver(receiver);
                Log.d(TAG, "run: Noisy Reciever Unregistered");
            }
        }catch (IllegalArgumentException e){
            Log.d(TAG, "run: IllegalArgumentException"+e.getMessage());
        }catch (NullPointerException e){
            Log.d(TAG, "run: IllegalArgumentException"+e.getMessage());

        }

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

        SharedPreferences mSharedPrefs=new SharedPreferences(context);
        List <String> songs= mSharedPrefs.getList(context.getString(R.string.shared_array_list_key));
        //int index=getIntPref(getString(R.string.current_index),this);
        String songname=songs.get(index);
        String songName_=songname.replace(".mp3","");
        return songName_;

    }
    public static String songSubtext(int index,Context context){


        if(activityIdentifier == PLAYLIST_ACTIVITY_IDENTIFIER){
            return "NF";
        }else {
            SharedPreferences mSharedPrefs=new SharedPreferences(context);
            List<Song> songs=mSharedPrefs.getObjectsList(context.getString(R.string.existing_objects_list_prefs));
            //int index=getIntPref(getString(R.string.current_index),this);
            String artist_name=songs.get(index).getArtist_name();
            return artist_name;

        }

    }

    private void displayNotification(Context context) {

        Bitmap artwork = BitmapFactory.decodeResource(getResources(), R.drawable.notifi_thumbnail);

        if(activityIdentifier == PLAYLIST_ACTIVITY_IDENTIFIER){
            clickIntent=new Intent(this, PlaylistActivity.class);
        }else {
            clickIntent=new Intent(this, Extras.class);
        }


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

        PendingIntent pausePendingIntent = PendingIntent.getService(context, PAUSE_INTENT_REQUEST_CODE,
                pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent playPendingIntent = PendingIntent.getService(context, PLAY_INTENT_REQUEST_CODE,
                playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stopPendingIntent= PendingIntent.getService(context, STOP_INTENT_REQUEST_CODE,
                stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nextPendingIntent= PendingIntent.getService(context, NEXT_INTENT_REQUEST_CODE,
                nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent prevPendingIntent= PendingIntent.getService(context, PREV_INTENT_REQUEST_CODE,
                prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent clickPendingIntent=PendingIntent.getActivity(this,CLICK_INTEN_REQUEST_CODE,
                clickIntent,PendingIntent.FLAG_UPDATE_CURRENT);





         notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_play)
                        .setContentTitle(songNameNotification
                                (mSharedPrefs.getInt(getString(R.string.shared_current_index),0),this))
                        .setLargeIcon(artwork)
                        .setContentText(songSubtext(mSharedPrefs.getInt(getString(R.string.shared_current_index),0),this))
                        .setContentIntent(clickPendingIntent)


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

                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0,1,3)
                                .setMediaSession(mediaSessionCompat.getSessionToken()))
                        //.setSubText("Playing")
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
            mSharedPrefs=new SharedPreferences(NotificationActionService.this);
            mPlayerActivity=new PlayerActivity();
            Log.d(TAG, "onCreate: IntentService");

        }


        private SharedPreferences mSharedPrefs;
        private PlayerActivity mPlayerActivity;

        List<String> songs_list=new ArrayList<>();
        int index_current;

        public void setIndex_current(int index_current) {
            this.index_current = index_current;
        }

        public int getIndex_current() {
            return index_current;
        }

        public List<String> getSongs_list() {
            return songs_list;
        }

        public void setSongs_list(List<String> songs_list) {
            this.songs_list = songs_list;
        }

        public void playSongNotification(int index){
            Log.d(TAG, "playSongNotification: IntentService index : "+index);

            String songName=null;
            try {
                songName = getSongs_list().get(index);
            }catch (NullPointerException e){

                Log.d(TAG, "onHandleIntent: NullPointerException"+e.getMessage());
            }catch (IllegalArgumentException e){
                Log.d(TAG, "onHandleIntent: IllegalArgumentException"+e.getMessage());

            }

            String FOLDER_PATH=null;
            if(PlayerActivity.activityIdentifier==PLAYLIST_ACTIVITY_IDENTIFIER){
                FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Documents/";
                Log.d(TAG, "playSong: Identifier : Playlist Path");
            }
            if(PlayerActivity.activityIdentifier==EXTRAS_ACTIVITY_IDENTIFIER){
                FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Extras/";
                Log.d(TAG, "playSong: Identifier : Extras Path : "+FOLDER_PATH);
            }

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


                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {

                                if(mSharedPrefs.getBoolean(getString(R.string.repeat_state),false)){
                                    //mRepeat.setBackgroundResource(R.drawable.ic_repeat_one);
                                    int index =getIndex_current();
                                    //updateNotification(index);
                                    playSongNotification(index);
                                    Log.d(TAG, "onCompletion: Repeats On - Moving On");
                                }else{

                                    Log.d(TAG, "onCompletion: Repeats Off - Moving On");
                                    //mRepeat.setBackgroundResource(R.drawable.ic_repeat_not);
                                    setIndex_current(mSharedPrefs.getInt(getString(R.string.shared_current_index),
                                            0));
                                    setSongs_list(mSharedPrefs.getList(getString(R.string.shared_array_list_key)));
                                    int index=getIndex_current();
                                    index++;
                                    index %= getSongs_list().size();
                                    // playerActivity.setSongName(mCurrentIndex);
                                    updateNotification(index);
                                    playSongNotification(index);

                                    Log.d(TAG, "ACTION_NEXT: incremented index : "+index);
                                    mSharedPrefs.saveInt(getString(R.string.shared_current_index),index);



                                    try{
                                        if(PlaylistActivity.playlist_adapter != null || Extras.adapter != null){
                                            if(activityIdentifier==PLAYLIST_ACTIVITY_IDENTIFIER){
                                                PlaylistActivity.playlist_adapter.notifyDataSetChanged();
                                            }else {
                                                Extras.adapter.notifyDataSetChanged();
                                            }
                                        }
                                    }catch (NullPointerException e){
                                        Log.d(TAG, "onCompletion: NullPointerException " +e.getMessage());
                                    }catch (IllegalArgumentException e){
                                        Log.d(TAG, "onCompletion: IllegalArgumentException " +e.getMessage());
                                    }catch (Exception e){
                                        Log.d(TAG, "onCompletion: Exception " +e.getMessage());
                                    }


                                }
                            }
                        });

                    }
                });

            }catch (Exception e){
                Log.d(TAG, "playSongNotification: "+e.getMessage());
            }
        }

        public void updateNotification(int index){
            notificationBuilder.setContentTitle(PlayerActivity
                    .songNameNotification(index, NotificationActionService.this));
            //PlayerActivity.songNameNotification(index, NotificationActionService.this);
            notificationBuilder.setContentText(PlayerActivity.songSubtext(index,NotificationActionService.this));
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }

        public void stopNotification(){
            NotificationManagerCompat.from(NotificationActionService.this).cancel(NOTIFICATION_ID);
            stopSelf();
            try{
                if(mNoisyRecieverOn){
                    unregisterReceiver(receiver);
                    Log.d(TAG, "run: Noisy Reciever Unregistered");
                }
            }catch (IllegalArgumentException e){
                Log.d(TAG, "run: IllegalArgumentException"+e.getMessage());
            }catch (NullPointerException e){
                Log.d(TAG, "run: IllegalArgumentException"+e.getMessage());

            }
        }


        @Override
        public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

            Log.d(TAG, "onStartCommand: IntentService");



            if(intent.getAction()==ACTION_NEXT){

                setIndex_current(mSharedPrefs.getInt(getString(R.string.shared_current_index),0));
                setSongs_list(mSharedPrefs.getList(getString(R.string.shared_array_list_key)));
                Log.d(TAG, "onStartCommand: NEXT");
            }
            if(intent.getAction()==ACTION_PREV){
                Log.d(TAG, "onStartCommand: PAUSE");
                setIndex_current(mSharedPrefs.getInt(getString(R.string.shared_current_index),0));
                setSongs_list(mSharedPrefs.getList(getString(R.string.shared_array_list_key)));
            }
            if(intent.getAction()==ACTION_PLAY){
                Log.d(TAG, "onStartCommand: PLAY");
                setIndex_current(mSharedPrefs.getInt(getString(R.string.shared_current_index),0));
                setSongs_list(mSharedPrefs.getList(getString(R.string.shared_array_list_key)));
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
                            if(mediaPlayer!=null){
                                mediaPlayer.pause();
                            }else if(mediaPlayer==null){
                                Toast.makeText(NotificationActionService.this, "No Track Playing", Toast.LENGTH_LONG).show();
                            }else {
                                Toast.makeText(NotificationActionService.this, "App is Terminated", Toast.LENGTH_LONG).show();
                                stopNotification();
                                return;
                            }
                        }
                    });

                }catch (NullPointerException e){

                    Log.d(TAG, "onHandleIntent: NullPointerException"+e.getMessage());
                }catch (IllegalArgumentException e){

                    Log.d(TAG, "onHandleIntent: IllegalArgumentException"+e.getMessage());
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
                                mediaPlayer.start();
                                return;
                            }else if(mediaPlayer==null){
                                Toast.makeText(NotificationActionService.this, "App is Terminated", Toast.LENGTH_LONG).show();
                                stopNotification();
                            }else if(!mediaPlayer.isPlaying()){
                                mediaPlayer.start();
                            }else if(mediaPlayer!=null && !mediaPlayer.isPlaying()){
                                mediaPlayer.start();
                            }
                        }
                    });

                }catch (NullPointerException e){

                    Log.d(TAG, "onHandleIntent: NullPointerException"+e.getMessage());
                }catch (IllegalArgumentException e){

                    Log.d(TAG, "onHandleIntent: IllegalArgumentException"+e.getMessage());
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
                                stopNotification();

                            }
                            else {
                                stopNotification();


                            }
                        }
                    });

                }catch (NullPointerException e){

                    Log.d(TAG, "onHandleIntent: NullPointerException"+e.getMessage());
                }catch (IllegalArgumentException e){

                    Log.d(TAG, "onHandleIntent: IllegalArgumentException"+e.getMessage());
                }catch (IllegalStateException e){
                    Log.d(TAG, "onHandleIntent: IllegalStateException"+e.getMessage());
                }catch (RuntimeException e){
                    Log.d(TAG, "onHandleIntent: RuntimeException"+e.getMessage());
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
                                updateNotification(index);
                                playSongNotification(index);

                                Log.d(TAG, "ACTION_NEXT: incremented index : "+index);
                                mSharedPrefs.saveInt(getString(R.string.shared_current_index),index);
                            }else {
                                Toast.makeText(NotificationActionService.this, "App is Terminated", Toast.LENGTH_LONG).show();
                                stopNotification();
                                return;
                            }
                        }
                    });

                }catch (NullPointerException e){

                    Log.d(TAG, "onHandleIntent: NullPointerException"+e.getMessage());
                }catch (IllegalArgumentException e){

                    Log.d(TAG, "onHandleIntent: IllegalArgumentException"+e.getMessage());
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
                                updateNotification(index);
                                mSharedPrefs.saveInt(getString(R.string.shared_current_index),index);
                            }else {
                                Toast.makeText(NotificationActionService.this, "App is Terminated", Toast.LENGTH_LONG).show();
                                stopNotification();
                                return;
                            }
                        }
                    });

                }catch (NullPointerException e){

                    Log.d(TAG, "onHandleIntent: NullPointerException"+e.getMessage());
                }catch (IllegalArgumentException e){

                    Log.d(TAG, "onHandleIntent: IllegalArgumentException"+e.getMessage());
                }catch (IllegalStateException e){
                    Log.d(TAG, "onHandleIntent: IllegalStateException"+e.getMessage());
                }

            }


        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            try{
                if(mNoisyRecieverOn){
                    unregisterReceiver(receiver);
                    Log.d(TAG, "run: Noisy Reciever Unregistered");
                }
            }catch (IllegalArgumentException e){
                Log.d(TAG, "run: IllegalArgumentException"+e.getMessage());
            }catch (NullPointerException e){
                Log.d(TAG, "run: IllegalArgumentException"+e.getMessage());

            }
        }
    }



    ////////------------------------------------INTENT SERVICE-------------------////////////
    ////////------------------------------------INTENT SERVICE-------------------////////////

    }
    ////--------------------------------------NOTIFICATION INTENT------------------------------------------/////




