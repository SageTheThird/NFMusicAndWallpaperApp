package com.homie.nf;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.homie.nf.Songs.playlist_Activity;
import com.homie.nf.Utils.UniversalImageLoader;
import com.homie.nf.Wallpapers.WallpaperMain;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout myDrawerMain;
    private Toolbar toolbar;
    private ActionBarDrawerToggle myToggle;
    private NavigationView navView;
    private  ImageView mBackground_image, mTitle_image;
    private Context mContext=MainActivity.this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);



        initWidgets();
        initImageLoader();
        imageLoading();
        firebaseNotificationSetup();
        drawerSetup();
        navigationSetup();


    }

    private void drawerSetup(){
        setSupportActionBar(toolbar);
        myToggle = new ActionBarDrawerToggle(this, myDrawerMain, toolbar,
                R.string.open, R.string.close);
        myDrawerMain.addDrawerListener(myToggle);

    }
    private void navigationSetup(){
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
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        myToggle.syncState();
    }


    private void initWidgets(){
        myDrawerMain   =         findViewById(R.id.myDrawer);
        toolbar = findViewById(R.id.main_toolbar);
        navView =                findViewById(R.id.navigationView);
        mBackground_image =              findViewById(R.id.background_imageview);
        mTitle_image =        findViewById(R.id.imageView_title);

    }
    private void imageLoading(){

        Picasso
                .with(this)
                .load(R.drawable.title)
                .placeholder(R.drawable.back_arrow)
                .into(mTitle_image);
        Picasso
                .with(this)
                .load(R.drawable.test_background)
                .resize(800, 800)

                .placeholder(R.drawable.madeinsociety)
                .into(mBackground_image);

    }
    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void firebaseNotificationSetup() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TOKEN FAIL:", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        Log.d("TOKEN SUCCESS", token);
                        // Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });


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

