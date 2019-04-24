package com.homie.nf;


import android.content.ClipData;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.MenuItem;

import android.view.WindowManager;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity  {

    public DrawerLayout myDrawerMain;
    public ActionBarDrawerToggle myToggle;
    public NavigationView navView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //myDrawerMain=(DrawerLayout) findViewById(R.id.myDrawer);
        navView=(NavigationView) findViewById(R.id.navigationView);


       navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
           @Override
           public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

               switch(menuItem.getItemId()){

                   case R.id.menu_playlist:


                       startActivity(new Intent(MainActivity.this,playlist_Activity.class));

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






}