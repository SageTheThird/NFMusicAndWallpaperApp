package com.homie.nf.Songs;

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

import com.homie.nf.R;

import java.util.ArrayList;

import me.tankery.lib.circularseekbar.CircularSeekBar;


public class PlayerActivity extends AppCompatActivity implements genius_fragment.OnFragmentInteractionListener, lyrics_frag.OnFragmentInteractionListener {
    static MediaPlayer mediaPlayer;

    CircularSeekBar seekBar;
    Button btn_pause, back_arrow, genius_btn, lyrics_button,
            queue_btn;
    TextView songnameView;

    String lyrics_file="", genius_file="", fileName101="";
    Intent intent;


    LinearLayout linearLayout;
    ArrayList<String> song_list;
    private FrameLayout frameLayout;
    private Handler myHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

        song_list     =    new  ArrayList<>();
        frameLayout   =    findViewById(R.id.frameLayout);
        btn_pause     =    findViewById(R.id.pause);
        songnameView  =    findViewById(R.id.songtextView);
        seekBar       =    findViewById(R.id.circularSeekBar);
        back_arrow    =    findViewById(R.id.back_button);
        genius_btn    =    findViewById(R.id.geniusbutton);
        lyrics_button =    findViewById(R.id.lyrics_button);
        linearLayout  =    findViewById(R.id.linearLayout);
        queue_btn     =    findViewById(R.id.queue_button);

        intent        =    getIntent();

        lyrics_file   =    intent.getStringExtra("LYRICSFILE");
        genius_file   =    intent.getStringExtra("GENIUSFILENAME");
        fileName101   =    intent.getStringExtra("songname");
        final String songPath    =    Environment.getExternalStorageDirectory().getAbsolutePath()
                           + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Documents/"
                           + fileName101;


        String songNameOnly=fileName101.replace(".mp3","");
        songnameView.setText(songNameOnly);
        if (mediaPlayer != null ) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        playerSetup(songPath);

        Thread thread = new runThread();
        thread.start();


        genius_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGeniusFragment(genius_file);
            }
        });


        lyrics_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLyricsFragment(lyrics_file);
            }
        });


        seekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
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

        });


        btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //seekBar.setMax(totalLength);
                seekBar.setMax(mediaPlayer.getDuration());

                if (mediaPlayer.isPlaying()) {

                    btn_pause.setBackgroundResource(R.drawable.play_button);
                    mediaPlayer.pause();
                } else if (!mediaPlayer.isPlaying()) {
                    btn_pause.setBackgroundResource(R.drawable.pause_button);
                    mediaPlayer.start();
                }
            }
        });


        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PlayerActivity.this, playlist_Activity.class));
            }
        });


    }

    public void openGeniusFragment(String genius_url) {

        genius_fragment genius_fragment = com.homie.nf.Songs.genius_fragment.newInstance(genius_url);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frameLayout, genius_fragment, "GENIUS_FRAGMENT").commit();

    }

    public void openLyricsFragment(String lyrics_file) {

        lyrics_frag lyrics_fragment1 = lyrics_frag.newInstance(lyrics_file);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frameLayout, lyrics_fragment1, "LYRICS_FRAGMENT").commit();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        onBackPressed();
    }

    public void playerSetup(final String song) {
        if (btn_pause.getBackground().equals(R.drawable.pause_button)) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            btn_pause.setBackgroundResource(R.drawable.play_button);
        } else {

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {

                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(song);
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                                seekBar.setProgress(0);
                                seekBar.setMax(mediaPlayer.getDuration());
                                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        btn_pause.setBackgroundResource(R.drawable.play_button);


                                    }
                                });
                                Log.d("Prog", "run: " + mediaPlayer.getDuration());
                            }
                        });
                        btn_pause.setBackgroundResource(R.drawable.pause_button);


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

    public class runThread extends Thread {


        @Override
        public void run() {
            while (true) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("Runwa", "run: " + 1);
                if (mediaPlayer != null) {
                    seekBar.post(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        }
                    });

                    //Log.d("Runwa", "run: " + mediaPlayer.getCurrentPosition());
                }
            }
        }

    }
}

