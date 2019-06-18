package com.obcomdeveloper.realmusic;

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

    private ImageView whole_logo;
    private ImageView imageView_background;
    private Context mContext=SplashScreen.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       setContentView(R.layout.activity_splash_screen);

        imageView_background=findViewById(R.id.backgroundsplash_imageView);
        whole_logo =findViewById(R.id.logo_iv);

        Picasso
                .with(this)
                .load(R.drawable.splash_background)
                .resize(342,444)
                .into(imageView_background);
        Picasso
                .with(this)
                .load(R.drawable.whole_logotwo)
                .into(whole_logo);


        Animation myAnim= AnimationUtils.loadAnimation(this,R.anim.mytransition);
        //imageView_background.startAnimation(myAnim);
        whole_logo.startAnimation(myAnim);





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
        }, 2100);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
     // logoAnimation.start();
    }
}

