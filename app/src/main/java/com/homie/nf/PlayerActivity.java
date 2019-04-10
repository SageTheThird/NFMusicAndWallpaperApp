package com.homie.nf;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.akaita.android.circularseekbar.CircularSeekBar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    static TextView lyrics_textView;
    NestedScrollView nestedScrollView;
    boolean tester=true;
    String text="";
    String lyrics_file;
    Intent intent;
    WebView geniusWebView;
    Button genius_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);
        nestedScrollView=findViewById(R.id.nestScrollView);
        intent=getIntent();




        btn_pause = (Button) findViewById(R.id.pause);
        btn_previous = (Button) findViewById(R.id.previous);
        btn_next = (Button) findViewById(R.id.next);
        songnameView = (TextView) findViewById(R.id.songtextView);
        songseekBar = (SeekBar) findViewById(R.id.seekBar);
        back_arrow=(Button) findViewById(R.id.back_button);
        final Button shuffle_btn=findViewById(R.id.shuffle);
        final Button repeat_btn=findViewById(R.id.repeat);
        geniusWebView=findViewById(R.id.geniusWebView);
        //startDur=findViewById(R.id.running_time);
        //totalDur=findViewById(R.id.total_time);
        genius_btn=findViewById(R.id.geniusbutton);


         lyrics_file=intent.getStringExtra("LYRICSFILE");
        Toast.makeText(this, "LYRICNAME"+lyrics_file, Toast.LENGTH_SHORT).show();


        //to edit the actionBar
        //getSupportActionBar().setTitle("Now Playing");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Button lyrics_button=findViewById(R.id.lyrics_button);
        lyrics_textView=findViewById(R.id.lyricstextView);

        lyrics_textView.setVisibility(View.INVISIBLE);
        nestedScrollView.setVisibility(View.INVISIBLE);



        lyrics_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(tester==true){

                lyrics_textView.setVisibility(View.VISIBLE);
                nestedScrollView.setVisibility(View.VISIBLE);

                btn_pause.setVisibility(View.INVISIBLE);
                btn_next.setVisibility(View.INVISIBLE);
                btn_previous.setVisibility(View.INVISIBLE);
                shuffle_btn.setVisibility(View.INVISIBLE);
                repeat_btn.setVisibility(View.INVISIBLE);





               try{

                    InputStream inputStream=getAssets().open(lyrics_file);
                    int size=inputStream.available();
                    byte[]  buffer=new byte[size];

                    inputStream.read(buffer);
                    inputStream.close();
                    text=new String(buffer);



                }catch (IOException ex){

                    ex.printStackTrace();
                }
                lyrics_textView.setText(text);

                tester=false;

                }
                else if(tester==false)
                {

                    lyrics_textView.setVisibility(View.INVISIBLE);
                    nestedScrollView.setVisibility(View.INVISIBLE);

                    btn_pause.setVisibility(View.VISIBLE);
                    btn_next.setVisibility(View.VISIBLE);
                    btn_previous.setVisibility(View.VISIBLE);
                    shuffle_btn.setVisibility(View.VISIBLE);
                    repeat_btn.setVisibility(View.VISIBLE);
                    tester=true;


                }

            }
        });




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

                mediaPlayer.seekTo(seekBar.getProgress());
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

