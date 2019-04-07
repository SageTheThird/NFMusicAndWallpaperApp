package com.homie.nf;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.akaita.android.circularseekbar.CircularSeekBar;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button btn_pause, btn_previous, btn_next,back_arrow;
    static MediaPlayer mediaPlayer;

    TextView songnameView;
    SeekBar songseekBar;
    int positionSong;
    ArrayList<File> mySongs;

    Thread updateseekBar;
    String sname;
    TextView startDur,totalDur;
    int currentPosition=0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);




        btn_pause = (Button) findViewById(R.id.pause);
        btn_previous = (Button) findViewById(R.id.previous);
        btn_next = (Button) findViewById(R.id.next);
        songnameView = (TextView) findViewById(R.id.songtextView);
        songseekBar = (SeekBar) findViewById(R.id.seekBar);
        back_arrow=(Button) findViewById(R.id.back_button);
        startDur=findViewById(R.id.running_time);
        //totalDur=findViewById(R.id.total_time);

        //to edit the actionBar
        //getSupportActionBar().setTitle("Now Playing");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateseekBar=new Thread(){
            @Override
            public void run() {

                int totalDuration=mediaPlayer.getDuration();


                while(currentPosition < totalDuration){
                    try{
                        sleep(500);
                        currentPosition=mediaPlayer.getCurrentPosition();
                        //songseekBar.setEnabled(true);
                        songseekBar.setProgress(currentPosition);


                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }

                }
            }
        };

        if(mediaPlayer!=null){

            mediaPlayer.stop();
            mediaPlayer.release();
        }


       Intent intent=getIntent();

        String fileName101=intent.getStringExtra("songname");
        String nextSongname=intent.getStringExtra("nextsong");
        Log.i("File Name: ",fileName101);

        String songPath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/"+getApplicationContext().getPackageName()+"/files/Documents/"+fileName101;
        final String nextSongPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/"+getApplicationContext().getPackageName()+"/files/Documents/"+nextSongname;

        // String fullPath=songPath+fileName101;
        Log.i("Player Activity",songPath);
        //Uri u=Uri.parse("gs://nf2firebaseproject.appspot.com/04 - Green Lightsnf.mp3");

       // MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            final Uri[] u = {Uri.parse(songPath)};

         mediaPlayer=MediaPlayer.create(PlayerActivity.this, u[0]);
          //  mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();

            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    u[0] =Uri.parse(nextSongPath);
                    mp=MediaPlayer.create(PlayerActivity.this, u[0]);
                    mp.start();



                }
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception of type : " + e.toString());
            e.printStackTrace();
        }

       // mediaPlayer.start();
        final int totalLength=mediaPlayer.getDuration();
        songseekBar.setMax(totalLength);
        //totalDur.setText(totalLength);

        updateseekBar.start();
         //to change color of the seekBar //following 2 lines
       songseekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
       songseekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorAccent),PorterDuff.Mode.SRC_IN);

        songseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mediaPlayer.seekTo((int) seekBar.getProgress());
            }
        });


       btn_pause.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               songseekBar.setMax(totalLength);

               if( mediaPlayer.isPlaying()){

                   btn_pause.setBackgroundResource(R.drawable.play_button);
                   mediaPlayer.pause();
               }
               else{
                   btn_pause.setBackgroundResource(R.drawable.pause_button);
                   mediaPlayer.start();
               }
           }
       });

       btn_next.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               mediaPlayer.stop();
               mediaPlayer.release();



               positionSong=((positionSong+1)%mySongs.size());
               Uri uri=Uri.parse( mySongs.get(positionSong).toString());
               mediaPlayer=MediaPlayer.create(PlayerActivity.this,uri);

               sname=mySongs.get(positionSong).getName().toString();
               songnameView.setText(sname);
               mediaPlayer.start();
           }
       });

       btn_previous.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               mediaPlayer.stop();
               mediaPlayer.release();
               positionSong=((positionSong-1)<0)?(mySongs.size()-1):(positionSong-1);
               Uri uri=Uri.parse(mySongs.get(positionSong).toString());
               mediaPlayer=MediaPlayer.create(PlayerActivity.this,uri);
               sname=mySongs.get(positionSong).getName().toString();
               songnameView.setText(sname);
               mediaPlayer.start();

           }
       });

    back_arrow.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(PlayerActivity.this,playlist_Activity.class));
        }
    });



    }


}

   /* @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.btn_play:
                if(mediaPlayer==null) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.destiny);
                    mediaPlayer.start();
                }
                else if(!mediaPlayer.isPlaying()){

                    mediaPlayer.seekTo(pauseCurrentPosition);
                    mediaPlayer.start();
                }
                break;
            case R.id.btn_pause:
                if(mediaPlayer!=null){
                    mediaPlayer.pause();
                    pauseCurrentPosition=mediaPlayer.getCurrentPosition();

                }

                break;
            case R.id.btn_stop:
                if(mediaPlayer!=null){
                    mediaPlayer.stop();
                    mediaPlayer=null;

                }
                break;
        }

*/

