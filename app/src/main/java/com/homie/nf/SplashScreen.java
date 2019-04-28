package com.homie.nf;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;


public class SplashScreen extends AppCompatActivity {

    ImageView imageView_outcast;
    ImageView imageView_sideLine;
    ImageView imageView_logo;

    AnimationDrawable logoAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       setContentView(R.layout.activity_splash_screen);

        imageView_logo=findViewById(R.id.logo_imageView);
        imageView_sideLine=findViewById(R.id.sideLinesImageView);
        imageView_outcast=findViewById(R.id.outcast_imageView);
        Picasso
                .with(this)
                .load(R.drawable.logo)
                .resize(342,444)
                .into(imageView_logo);


        Animation myAnim= AnimationUtils.loadAnimation(this,R.anim.mytransition);
        Animation myAnim2= AnimationUtils.loadAnimation(this,R.anim.slide_in_rightoutcast);
        imageView_sideLine.startAnimation(myAnim);
        imageView_logo.startAnimation(myAnim);
        imageView_outcast.startAnimation(myAnim2);


       /* imageView.setBackgroundResource(R.drawable.animation);
        logoAnimation=(AnimationDrawable)imageView.getBackground();

*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, 3000);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
     // logoAnimation.start();
    }
}

