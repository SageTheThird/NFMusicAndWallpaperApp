package com.obcomdeveloper.realmusic;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;


public class About extends AppCompatActivity {


    AnimationDrawable logoAnimation;
    private TextView mAbout_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       setContentView(R.layout.activity_about);
       mAbout_tv=findViewById(R.id.about_tv);

       //mAbout_tv.setText(getString(R.string.about_tv));




    }

}

