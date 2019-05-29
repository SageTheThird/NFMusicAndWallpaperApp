package com.homie.nf.Songs;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.homie.nf.R;

import java.util.ArrayList;

import me.tankery.lib.circularseekbar.CircularSeekBar;


public class PlayerActivity extends AppCompatActivity implements genius_fragment.OnFragmentInteractionListener, lyrics_frag.OnFragmentInteractionListener {

    private static final String TAG = "PlayerActivity";
    public static final int DEFAULT_INT_VALUE = 0;
    private Context mContext = PlayerActivity.this;
    static MediaPlayer mediaPlayer;
    private CircularSeekBar seekBar;
    private Button mPause_btn, mBack_arrow, mGenius_btn, mLyrics_btn,mNext_btn, mPrev_btn;
    private TextView mSongname_view, mTotal_duration_view, mCurrent_duration_view;
    private String mLyric_file = "", mGenius_file = "";
    private Intent intent;
    private ArrayList<String> mSongs_list;

    private Handler myHandler = new Handler();
    //for song list
    private int mCurrentIndex;




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


    }

    private void getIncomingIntent() {
        intent = getIntent();

        mLyric_file = intent.getStringExtra(getString(R.string.LYRICSFILE));
        mGenius_file = intent.getStringExtra(getString(R.string.GENIUSFILENAME));

        mCurrentIndex = intent.getIntExtra(getString(R.string.position_song), DEFAULT_INT_VALUE);
        mSongs_list = intent.getStringArrayListExtra(getString(R.string.songslist));
        //songTextView setup
        setSongName(mCurrentIndex);

    }

    private void setSongName(int index) {
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

}

