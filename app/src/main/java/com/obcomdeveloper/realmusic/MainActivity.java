package com.obcomdeveloper.realmusic;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.obcomdeveloper.realmusic.Extras.Extras;
import com.obcomdeveloper.realmusic.Songs.playlist_Activity;
import com.obcomdeveloper.realmusic.Utils.UniversalImageLoader;
import com.obcomdeveloper.realmusic.Wallpapers.WallpaperMain;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;


import java.io.File;
import dmax.dialog.SpotsDialog;
import hotchemi.android.rate.AppRate;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int STORAGE_SONG_REFERENCE=1;
    public static final int STORAGE_EXTRAS_REFERENCE=2;

    private DrawerLayout myDrawerMain;
    private Toolbar toolbar;
    private ActionBarDrawerToggle myToggle;
    private NavigationView navView;
    private Context mContext = MainActivity.this;
    private ImageView mBackground_image;


    private StorageReference storageReference;
    static AlertDialog dialog;

    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;

    private BroadcastReceiver onDownloadComplete;

    private String directory;

    private TextView toolbar_tv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        toolbar_tv=findViewById(R.id.main_toolbar_tv);


        directorySetup();
        internetConnectivity();
        initWidgets();
        initImageLoader();
        imageLoading();
        firebaseNotificationSetup();
        drawerSetup();
        navigationSetup();


        //will only run once per app install
        if ((networkInfo == null || !networkInfo.isConnected())) {
            showToast("Please Check Your Internet Connection & Restart the App");
            return;
        }else{
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if(prefs.getBoolean("firstRun", true)) {
                downloadPlaylistFirstSongOnce();// <-- your function
                downloadExtrasFirstSongOnce();
                prefs.edit().putBoolean("firstRun", false).commit();
            }
        }




        AppRate.with(this)
                .setInstallDays(1)
                .setLaunchTimes(3)
                .setRemindInterval(2)
                .monitor();

        AppRate.showRateDialogIfMeetsConditions(this);



    }

    private void downloadPlaylistFirstSongOnce(){
//        PlayerActivity.saveIntPref(getString(R.string.shared_run_once),1,this);
//
//        if(PlayerActivity.getIntPref(getString(R.string.shared_run_once),this)==1){
            //download the song

            if ((networkInfo == null || !networkInfo.isConnected())) {
                showToast("Please Check Your Internet Connection!");
                return;
            } else {

                //dialog for when downloading
                dialog = new SpotsDialog.Builder()
                        .setContext(MainActivity.this)
                        .setTheme(R.style.ForMain)
                        .setCancelable(false)
                        .build();
                //download file
                download("Why.mp3", directory,STORAGE_SONG_REFERENCE);
            }


         //   PlayerActivity.saveIntPref(getString(R.string.shared_run_once),2,this);

       // }
    }

    private void downloadExtrasFirstSongOnce(){

        String extraDirectory=Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/Extras/";

        File directExtra = new File(extraDirectory);

        if (!directExtra.exists()) {
            File myDirectory = new File(Environment.getExternalStorageDirectory().
                    getAbsolutePath() + "/Android/data/" + this.getPackageName() + "/files/Extras/");
            myDirectory.mkdir();
        }

        if ((networkInfo == null || !networkInfo.isConnected())) {
            showToast("Please Check Your Internet Connection!");
            return;
        } else {

            //dialog for when downloading
            dialog = new SpotsDialog.Builder()
                    .setContext(MainActivity.this)
                    .setTheme(R.style.ForMain)
                    .setCancelable(false)
                    .build();
            //download file
            download("Rick and Morty (Dubstep).mp3", extraDirectory,STORAGE_EXTRAS_REFERENCE);
        }

    }
    private void directorySetup() {
        //directory
        directory = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/Documents";

        File direct = new File(directory);

        if (!direct.exists()) {
            File myDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + this.getPackageName() + "/files/Documents");
            myDirectory.mkdir();
        }

    }


    public void showToast(String msg) {

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void drawerSetup() {
        setSupportActionBar(toolbar);
        myToggle = new ActionBarDrawerToggle(this, myDrawerMain, toolbar,
                R.string.open, R.string.close);
        myDrawerMain.addDrawerListener(myToggle);

    }

    private void navigationSetup() {
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.menu_home:
                        myDrawerMain.closeDrawer(GravityCompat.START);
                        break;

                    case R.id.menu_playlist:

                        checkUserPermission();
                        startActivity(new Intent(MainActivity.this, playlist_Activity.class));
                        Animatoo.animateZoom(mContext);

                        break;

                    case R.id.menu_wallpapers:
                        checkUserPermission();
                        startActivity(new Intent(MainActivity.this, WallpaperMain.class));
                        Animatoo.animateZoom(mContext);

                        break;
                    case R.id.extra_sideMenu:
                        checkUserPermission();
                        startActivity(new Intent(MainActivity.this, Extras.class));
                        Animatoo.animateZoom(mContext);

                        break;

                    case R.id.menu_about:

                        Intent about_intent=new Intent(MainActivity.this,About.class);
                        startActivity(about_intent);

                        break;
                    case R.id.menu_send:
                        //opens app chooser to share the app
                        Intent myIntent=new Intent(Intent.ACTION_SEND);
                        myIntent.setType("text/plain");
                        String shareSub="Hey, Try Out This Real Music App : ";
                        String shareBody="http://play.google.com/store/apps/details?id=" + "com.android.chrome";
                        myIntent.putExtra(Intent.EXTRA_SUBJECT,shareSub);
                        myIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
                        startActivity(Intent.createChooser(myIntent,"Share App Link via "));

                        break;

                    case R.id.rate_menu:
                        try {

                            //replace chrome with getPackageName() later
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=" + "com.android.chrome")));
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                        }

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


    private void initWidgets() {
        myDrawerMain = findViewById(R.id.myDrawer);
        toolbar = findViewById(R.id.main_toolbar);
        navView = findViewById(R.id.navigationView);
        mBackground_image = findViewById(R.id.background_imageview);


    }
    @Override
    public void onBackPressed() {
        if (myDrawerMain.isDrawerOpen(GravityCompat.START)) {
            myDrawerMain.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private void imageLoading() {
        Picasso
                .with(this)
                .load(R.drawable.test_background)
                .resize(800, 800)

                .placeholder(R.drawable.madeinsociety)
                .into(mBackground_image);

    }

    private void initImageLoader() {
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


    private void checkUserPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 124);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 125);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 126);

        }
    }

    /////-----------------------------------Download--------------/////

    private void initializeReciever(final long downloadID) {
        onDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Fetching the download id received with the broadcast
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                //Checking if the received broadcast is for our enqueued download by matching download id
                if (downloadID == id) {


                    Toast.makeText(context, "Good to go!!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                }
            }
        };

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    private void internetConnectivity() {
        //Internet Connectivity
        connectivityManager = (ConnectivityManager)
                MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

    }



    public void download(final String fileNameInto, final String downloadDirectory, final int reference) {
        if(reference == STORAGE_SONG_REFERENCE){
            storageReference = FirebaseStorage.getInstance().getReference(getString(R.string.storage_song_db));
        }
        if(reference== STORAGE_EXTRAS_REFERENCE){
            storageReference = FirebaseStorage.getInstance().getReference(getString(R.string.store_extras_db));
        }



        storageReference = storageReference.child(fileNameInto);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //after download url from file is fetched it will download file

                String url = uri.toString();
                DownloadFiles downloadFiles = new DownloadFiles(
                        MainActivity.this,
                        "",
                        downloadDirectory,
                        fileNameInto,
                        onDownloadComplete);

                final long downloadid = downloadFiles.downloadingFiles(url);

                if(reference == STORAGE_EXTRAS_REFERENCE){
                    initializeReciever(downloadid);
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Failed to connect to server: " + e.getMessage());
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(onDownloadComplete != null){
            unregisterReceiver(onDownloadComplete);
        }
    }


    class DownloadFiles {

        private Context context;
        private String fileExtension;
        private String destinationDirectory;
        private String fileName;
        private BroadcastReceiver onDownloadCompleteAsync;

        public DownloadFiles(Context context,
                             String fileExtension,
                             String destinationDirectory,
                             String fileName,
                             BroadcastReceiver onDownloadCompleteAsync) {

            this.context = context;
            this.fileExtension = fileExtension;
            this.destinationDirectory = destinationDirectory;
            this.fileName = fileName;
            this.onDownloadCompleteAsync = onDownloadCompleteAsync;
        }


        public long downloadingFiles(String url) {
            dialog.show();
            long mDownloadId = 0;

            DownloadManager downloadManager = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
                downloadManager = (DownloadManager) context
                        .getSystemService(Context.DOWNLOAD_SERVICE);
            }
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
                request = new DownloadManager.Request(uri);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);


            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                request.setDestinationUri(Uri.fromFile(new File(destinationDirectory, fileName + fileExtension)));
            }

            if (downloadManager != null) {
                mDownloadId = downloadManager.enqueue(request);
            }

            return mDownloadId;


        }

    }
}

