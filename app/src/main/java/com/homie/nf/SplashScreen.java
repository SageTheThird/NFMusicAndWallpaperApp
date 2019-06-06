package com.homie.nf;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.squareup.picasso.Picasso;


public class SplashScreen extends AppCompatActivity {

    private ImageView imageView_rightBox;
    private ImageView imageView_fontOutcast;
    private ImageView imageView_background;
    private ImageView imageView_left_boxes;
    private Context mContext=SplashScreen.this;

    AnimationDrawable logoAnimation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       setContentView(R.layout.activity_splash_screen);

        imageView_background=findViewById(R.id.backgroundsplash_imageView);
        imageView_fontOutcast =findViewById(R.id.outcast_fontImageView);
        imageView_left_boxes=findViewById(R.id.left_boxes_imageView);
        imageView_rightBox =findViewById(R.id.right_box_imageView);
        Picasso
                .with(this)
                .load(R.drawable.splash_background)
                .resize(342,444)
                .into(imageView_background);


        Animation myAnim= AnimationUtils.loadAnimation(this,R.anim.mytransition);
        Animation myAnim2= AnimationUtils.loadAnimation(this,R.anim.slide_in_rightoutcast);
        imageView_background.startAnimation(myAnim);
        imageView_fontOutcast.startAnimation(myAnim);
        imageView_left_boxes.startAnimation(myAnim);
        imageView_rightBox.startAnimation(myAnim2);


       /* imageView.setBackgroundResource(R.drawable.animation);
        logoAnimation=(AnimationDrawable)imageView.getBackground();

*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                Animatoo.animateZoom(mContext);
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

