package com.obcomdeveloper.realmusic;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.obcomdeveloper.realmusic.Extras.Extras;
import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.Songs.PlaylistActivity;
import com.obcomdeveloper.realmusic.Utils.DownloadFiles;
import com.obcomdeveloper.realmusic.Utils.UniversalImageLoader;
import com.obcomdeveloper.realmusic.Wallpapers.WallpaperMain;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.obcomdeveloper.realmusic.room.DatabaseTransactions;
import com.obcomdeveloper.realmusic.room.ExtrasEntity;
import com.obcomdeveloper.realmusic.room.PlaylistEntity;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import hotchemi.android.rate.AppRate;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.obcomdeveloper.realmusic.FCM.MyFCMService.CHANNEL_ID;

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

    private List<Song> list;

    private DatabaseTransactions mDatabaseTransactions;
    private CompositeDisposable mDisposibles=new CompositeDisposable();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        toolbar_tv=findViewById(R.id.main_toolbar_tv);

        mDatabaseTransactions=new DatabaseTransactions(this);

        directorySetup();
        internetConnectivity();
        initWidgets();
        initImageLoader();
        imageLoading();
        //firebaseNotificationSetup();
        drawerSetup();
        navigationSetup();

        list=new ArrayList<>();

        //will only run once per app install
        if ((networkInfo == null || !networkInfo.isConnected())) {
            showToast("Please Check Your Internet Connection & Restart the App");
            return;
        }else{
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if(prefs.getBoolean("firstRun", true)) {
                downloadPlaylistFirstSongOnce();// <-- your function
                downloadExtrasFirstSongOnce();
                initialEntriesInDatabase();
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

    private void initialEntriesInDatabase() {

        String song_name= "When I Grow Up";
        Completable newNote=mDatabaseTransactions.addSong(new
                PlaylistEntity(0, song_name,R.drawable.offline_playlist_small_edited,"NF",null,null,null));

        String song_name2= "Rick and Morty (Dubstep)";
        Completable newSongExtras=mDatabaseTransactions.addSongExtras
                (new ExtrasEntity(0,song_name2,R.drawable.offline_extras_small_edited,"Wubba Lubba Dub Dub",null,null,null));

        newSongExtras.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposibles.add(d);
                    }

                    @Override
                    public void onComplete() {

                        Log.d(TAG, "onComplete: Extras Note Added");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
        newNote.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposibles.add(d);
                    }

                    @Override
                    public void onComplete() {

                        Log.d(TAG, "onComplete: New Note Added");

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void downloadPlaylistFirstSongOnce(){
//        PlayerActivity.saveIntPref(getString(R.string.shared_run_once),1,this);
//
//        if(PlayerActivity.getIntPref(getString(R.string.shared_run_once),this)==1){
            //downloadSong the song

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
                //downloadSong file
                download("When I Grow Up.mp3", directory,STORAGE_SONG_REFERENCE);
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
            //downloadSong file
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
                        startActivity(new Intent(MainActivity.this, PlaylistActivity.class));
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
                //Fetching the downloadSong id received with the broadcast
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                //Checking if the received broadcast is for our enqueued downloadSong by matching downloadSong id
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
                //after downloadSong url from file is fetched it will downloadSong file

                String url = uri.toString();
                DownloadFiles downloadFiles = new DownloadFiles(
                        MainActivity.this,
                        "",
                        downloadDirectory,
                        fileNameInto,
                        dialog);

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

}

