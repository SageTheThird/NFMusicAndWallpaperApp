package com.homie.nf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {

    public DrawerLayout myDrawerMain;
    public ActionBarDrawerToggle myToggle;
    public NavigationView navView;
    ImageView imageView;
    ImageView imageView_title;
    ImageView imageView_sideButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        myDrawerMain   =         findViewById(R.id.myDrawer);
        navView =                findViewById(R.id.navigationView);
        imageView =              findViewById(R.id.background_imageview);
        imageView_title =        findViewById(R.id.imageView_title);
        imageView_sideButton =   findViewById(R.id.imageView_sideButton);


        Picasso
                .with(this)
                .load(R.drawable.title)
                // .resize(700,700)

                .placeholder(R.drawable.back_arrow)
                .into(imageView_title);
        Picasso
                .with(this)
                .load(R.drawable.sidebutton)
                // .resize(700,700)

                .placeholder(R.drawable.ic_search_black_24dp)
                .into(imageView_sideButton);
        Picasso
                .with(this)
                .load(R.drawable.test_background1)
                .resize(800, 800)

                .placeholder(R.drawable.madeinsociety)
                .into(imageView);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.menu_playlist:
                        checkUserPermission();
                        startActivity(new Intent(MainActivity.this, playlist_Activity.class));

                        break;

                    case R.id.menu_wallpapers:
                        checkUserPermission();
                        startActivity(new Intent(MainActivity.this, WallpaperMain.class));

                        break;


                }

                return true;
            }
        });


        //ImageView side_button=(ImageView)findViewById(R.id.imageView2);

        //for drawer open close through actionbar
          /* private DrawerLayout myDrawerMain;
        private ActionBarDrawerToggle myToggle;

        myDrawerMain=(DrawerLayout) findViewById(R.id.myDrawer);
        myToggle=new ActionBarDrawerToggle(this,myDrawerMain,R.string.open,R.string.close);

        myDrawerMain.addDrawerListener(myToggle);
        myToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//For Drawer*/


    }

    @Override
    public void onBackPressed() {
        if(myDrawerMain.isDrawerOpen(GravityCompat.END)){
            myDrawerMain.closeDrawer(GravityCompat.END);
        }
        else {
            super.onBackPressed();
        }
    }
    private void checkUserPermission(){
        if(     ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        &&      ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
        &&      ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)

        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 124);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 125);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 126);

            }
        }

    }

