package com.homie.nf;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import abak.tr.com.boxedverticalseekbar.BoxedVertical;
import me.tankery.lib.circularseekbar.CircularSeekBar;

import static com.homie.nf.PlayerActivity.currentPosition;
import static com.homie.nf.PlayerActivity.mediaPlayer;
import static com.homie.nf.PlayerActivity.seekBar;
import static java.lang.Thread.sleep;


public class PlayerActivity extends AppCompatActivity implements genius_fragment.OnFragmentInteractionListener {
    static MediaPlayer mediaPlayer;
    static TextView lyrics_textView;
    static int currentPosition = 0;
    static CircularSeekBar seekBar;
    Button btn_pause, btn_previous, btn_next,
            back_arrow, genius_btn, lyrics_button,
            shuffle_btn, repeat_btn,
            queue_btn, volume_btn;
    TextView songnameView;

    ArrayList<File> mySongs;
    Thread updateseekBar;

    NestedScrollView nestedScrollView;
    boolean tester = true;
    String lyrics_text = "";
    String lyrics_file;
    Intent intent;
    String genius_file;
    LinearLayout linearLayout;
    ArrayList<String> song_list;
    AudioManager audioManager;
    BoxedVertical boxedVerticalSeekBar;
    private boolean verticalSeekBarTester = true;
    private FrameLayout frameLayout;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

        song_list = new ArrayList<>();
        boxedVerticalSeekBar = findViewById(R.id.boxed_vertical);
        volume_btn = findViewById(R.id.volume_btn);
        frameLayout = findViewById(R.id.frameLayout);
        btn_pause = findViewById(R.id.pause);
        songnameView = findViewById(R.id.songtextView);
        boxedVerticalSeekBar = findViewById(R.id.boxed_vertical);
        seekBar = findViewById(R.id.circularSeekBar);
        back_arrow = findViewById(R.id.back_button);
        genius_btn = findViewById(R.id.geniusbutton);
        lyrics_button = findViewById(R.id.lyrics_button);
        linearLayout = findViewById(R.id.linearLayout);
        nestedScrollView = findViewById(R.id.nestScrollView);
        lyrics_textView = findViewById(R.id.lyricstextView);
        queue_btn = findViewById(R.id.queue_button);
        intent = getIntent();

        String fileName101 = intent.getStringExtra("songname");
        String songPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Documents/"
                + fileName101;



        mediaPlayer=MediaPlayer.create(PlayerActivity.this,Uri.parse(songPath));
        seekBar.setMax(mediaPlayer.getDuration());

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //getting current volume from system phone
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        boxedVerticalSeekBar.setMax(maxVolume);
        //setting current volume
        boxedVerticalSeekBar.setDefaultValue(curVolume);


        lyrics_file = intent.getStringExtra("LYRICSFILE");
        genius_file = intent.getStringExtra("GENIUSFILENAME");


        /*BackgroundSeekAsync backgroundSeekAsync = new BackgroundSeekAsync(PlayerActivity.this);
        backgroundSeekAsync.execute(songPath);
*/


        //to edit the actionBar
        //getSupportActionBar().setTitle("Now Playing");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        volume_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (verticalSeekBarTester) {

                    boxedVerticalSeekBar.setVisibility(View.VISIBLE);
                    verticalSeekBarTester = false;
                } else if (!verticalSeekBarTester) {

                    boxedVerticalSeekBar.setVisibility(View.INVISIBLE);
                    verticalSeekBarTester = true;
                }

            }
        });

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


     updateseekBar=new Thread(){
         @Override
         public void run() {
             int totalDuration=mediaPlayer.getDuration();
             int currentPosition=0;

             while(currentPosition<totalDuration)
             {

                 try {
                     sleep(500);

                     currentPosition=mediaPlayer.getCurrentPosition();
                     seekBar.setProgress(currentPosition);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }

             }

         }
     };

        //seekBar.setMax(mediaPlayer.getDuration());

        if (mediaPlayer != null) {

            mediaPlayer.stop();
            mediaPlayer.release();
        }



        try {
            mediaPlayer.release();
            final Uri u = Uri.parse(songPath);
            // mediaPlayer.setDataSource(hashname);

            mediaPlayer = MediaPlayer.create(PlayerActivity.this, u);
            //  mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();

            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration());

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception of type : " + e.toString());
            e.printStackTrace();
        }

        // mediaPlayer.start();
        //final int totalLength = mediaPlayer.getDuration();

        //totalDur.setText(totalLength);

        //updateseekBar.start();
        //to change color of the seekBar //following 2 lines
        // songseekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        //songseekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {

             /*   int h = Math.round(progress);
                //In the above line we are converting the float value into int because
                // media player required int value and seekbar library gives progress in float
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(h);
                }*/
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
                } else if(!mediaPlayer.isPlaying()) {
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

        boxedVerticalSeekBar.setOnBoxedPointsChangeListener(new BoxedVertical.OnValuesChangeListener() {
            @Override
            public void onPointsChanged(BoxedVertical boxedPoints, final int value) {
                System.out.println(value);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
            }

            @Override
            public void onStartTrackingTouch(BoxedVertical boxedPoints) {
                //Toast.makeText(MainActivity.this, "onStartTrackingTouch", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(BoxedVertical boxedPoints) {
                //Toast.makeText(MainActivity.this, "onStopTrackingTouch", Toast.LENGTH_SHORT).show();
            }
        });


       /* Intent currSong = getIntent();
        Bundle b = currSong.getExtras();*/

    /*    // load initial index only once
        mCurrentIndex = (int) b.get("songIndex");

        btnPrevious.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               mCurrentIndex = mCurrentIndex > 0 ? mCurrentIndex - 1 : mSongList.size() - 1;
                                               playSongNumber(mCurrentIndex);
                                           }
                                       }

                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCurrentIndex++;
                        mCurrentIndex %= mSongList.size();
                        playSongNumber(mCurrentIndex);
                    }
                }

    }

    private void playSongNumber(int index) {

        try {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mSongList.get(index).getData());
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    }*/


    }

    public void openGeniusFragment(String genius_url) {

        genius_fragment genius_fragment = com.homie.nf.genius_fragment.newInstance(genius_url);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(R.id.frameLayout, genius_fragment, "GENIUS_FRAGMENT").commit();

    }
    public void openLyricsFragment(String lyrics_file) {

       lyrics_fragment lyrics_fragment1 = lyrics_fragment.newInstance(lyrics_file);
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


}


class BackgroundSeekAsync extends AsyncTask<String, Void, String> {


    String songLocation;
    Context context;

    public BackgroundSeekAsync(Context context) {

        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();


        //  mediaPlayer.setDataSource(songPath);


    }

    @Override
    protected String doInBackground(String... strings) {
        songLocation = strings[0];


        return songLocation;
    }


    @Override
    protected void onPostExecute(String songPath1) {
        super.onPostExecute(songPath1);
        try {
            //mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(context, Uri.parse(songPath1));
            mediaPlayer.prepare();
            //mediaPlayer.prepareAsync();

            mediaPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        int totalDuration = mediaPlayer.getDuration();

        seekBar.setMax(totalDuration);

        while (currentPosition < totalDuration) {
            try {
                sleep(500);

                currentPosition = mediaPlayer.getCurrentPosition();

                //seekBar.setEnabled(true);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            seekBar.setProgress(currentPosition);

        }

        Toast.makeText(context, "Async: Successful", Toast.LENGTH_LONG).show();

    }

}


