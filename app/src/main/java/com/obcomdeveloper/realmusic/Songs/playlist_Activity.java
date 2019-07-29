package com.obcomdeveloper.realmusic.Songs;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.obcomdeveloper.realmusic.Adapters.SongRecyclerView;
import com.obcomdeveloper.realmusic.Models.RowItem;
import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Utils.Ads;

import com.obcomdeveloper.realmusic.Utils.DownloadFiles;
import com.obcomdeveloper.realmusic.Utils.Quotes;
import com.obcomdeveloper.realmusic.Utils.UIUpdater;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;


public class playlist_Activity extends AppCompatActivity {
    private static final String TAG = "playlist_Activity";

    public static final int AD_TIME_INTERVAL=90*1000;
    public static final int RANDOM_QUOTE_INTERVAL=20*1000;

    public static final int PLAYLIST_IDENTIFIER=20;
    public static ListView listView;
    static AlertDialog dialog;
    private ImageView playlist_background;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    //static Progress progress;
    private ArrayList<RowItem> arrayList;
    private BroadcastReceiver onDownloadComplete;
    private Context mContext=playlist_Activity.this;
    private String track_directory,lyrics_directory;


    private ArrayList<String> filesNameList=new ArrayList<>();

    public static SongRecyclerView playlist_adapter;

    private List<Song> songs_list;

    private EditText searchView;

    private TextView patentTextView, random_quotes_tv;

    //ads
    private AdView adView;
    private Ads ads;
    private InterstitialAd interstitialAd;



    private Handler mHandler=new Handler();

    //
    private List<String> temp_list;
    private List<Song> existing_songs_models_list;

    //random_quotes_tv for textview
    private DatabaseReference quotes_Ref;

    private int count_quotes;

    private UIUpdater mUIUpdater;

    private List<String> random_quotes_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_playlist_);
        quotes_Ref=FirebaseDatabase.getInstance().getReference().child(getString(R.string.db_random_quotes)).child("array");

        searchView=findViewById(R.id.searchEdit);
        listView = findViewById(R.id.listViewPlaylist);
        patentTextView =findViewById(R.id.patent_playlist);
        random_quotes_tv =findViewById(R.id.random_quotes_playlist);
        adView =findViewById(R.id.adView);
        interstitialAd=new InterstitialAd(this);
        songs_list=new ArrayList<>();
        random_quotes_list=new ArrayList<>();


        patentTextView.setHorizontallyScrolling(true);
        patentTextView.setSelected(true);

        random_quotes_tv.setHorizontallyScrolling(true);
        random_quotes_tv.setSelected(true);

        //methods
        directorySetup();
        getFilesListFromFolder();
        layoutImageLoading();
        internetConnectivity();
        setupListView();
        searchSetup();


        //listeners
        listView.setOnItemClickListener(SongClickListener);


        //ads
        ads=new Ads();
        ads.initAdMob(this);
        ads.setupBanner(adView);
        ads.setupInterstitial(this,getString(R.string.interstitial_ad_test_unit_id),interstitialAd);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("setCountToZero", true)) {
            Log.d(TAG, "onCreate: count _ pref : called");
            count_quotes=0;
            PlayerActivity.saveIntPref(getString(R.string.count_random_quotes),count_quotes,this);
            prefs.edit().putBoolean("setCountToZero", false).commit();
        }


        quotes_Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                String quotes=dataSnapshot.getValue().toString();
                String modified_string=quotes.substring(1);
                //String[] quotes_array=modified_string.split("-");
                List<String> quotes_list = Arrays.asList(modified_string.split("-"));

                PlayerActivity.saveArrayList(quotes_list,getString(R.string.random_quotes_list_prefs),mContext);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Log.d(TAG, "onCreate: after Set Array Size : "+Quotes.getRandom_lyrics_array().length);




        mAdRunnable.run();
        //
        mUIUpdater = new UIUpdater(new Runnable() {
            @Override
            public void run() {
                //do what ever you want to do with your textview
                try{

                    random_quotes_list=PlayerActivity.getArrayList(getString(R.string.random_quotes_list_prefs),mContext);
                    count_quotes=PlayerActivity.getIntPref(getString(R.string.count_random_quotes),mContext);
                    Log.d(TAG, "run: count _ : "+count_quotes);
                    random_quotes_tv.setText(random_quotes_list.get(count_quotes));
                    if(count_quotes == random_quotes_list.size()-1){
                        count_quotes=0;
                        PlayerActivity.saveIntPref(getString(R.string.count_random_quotes),count_quotes,mContext);
                    }else {
                        count_quotes++;
                        PlayerActivity.saveIntPref(getString(R.string.count_random_quotes),count_quotes,mContext);
                    }


                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });


    }

    private Runnable mAdRunnable=new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ads.loadinterstitial(interstitialAd);
                }
            });

            mHandler.postDelayed(this,AD_TIME_INTERVAL);
        }
    };

    private Runnable random_quote_runnable=new Runnable() {



        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{

                        count_quotes=PlayerActivity.getIntPref(getString(R.string.count_random_quotes),mContext);
                        Log.d(TAG, "run: count _ : "+count_quotes);
                        random_quotes_tv.setText(Quotes.getRandom_lyrics_array()[count_quotes]);
                        if(count_quotes >= Quotes.getRandom_lyrics_array().length){
                            count_quotes=0;
                            PlayerActivity.saveIntPref(getString(R.string.count_random_quotes),count_quotes,mContext);
                        }
                        count_quotes++;
                        PlayerActivity.saveIntPref(getString(R.string.count_random_quotes),count_quotes,mContext);

                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }

                }
            });

            mHandler.postDelayed(this,RANDOM_QUOTE_INTERVAL);
        }
    };



    private void searchSetup() {
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                try{
                    String text = searchView.getText().toString().toLowerCase(Locale.getDefault());
                    playlist_adapter.filter(text);
                }catch (NullPointerException e){
                    Log.d(TAG, "afterTextChanged: NullPointerException" +e.getMessage());
                }

            }
        });
    }

    private void getFilesListFromFolder(){
        temp_list=new ArrayList<>();
        existing_songs_models_list=new ArrayList<>();
        int filesIndex=0;
        //String path = track_directory;
        Log.d("Folder Files", "Path: " + track_directory);
        File directoryFolder = new File(track_directory);
        File[] files = directoryFolder.listFiles();
        //Log.d("Folder Files", "Size: "+ files.length);

        if(files.length == 0){
            Log.d(TAG, "onCreate: Folder Empty");

        }else {

            for (int i = 0; i < files.length; i++)
            {
                Log.d("Files", "FileName:" + files[i].getName());
                filesNameList.add(filesIndex,files[i].getName());
                filesIndex++;
            }
            PlayerActivity.saveArrayList(filesNameList,getString(R.string.shared_array_list_key),mContext);
        }

        folder_files_to_temp_list();


    }

    private void folder_files_to_temp_list(){
        for(int i=0;i<filesNameList.size();i++){
            String name= filesNameList.get(i).replace(".mp3","");
            Log.d(TAG, "getFilesListFromFolder: temp_name : "+name+ "songs_list_size : "+songs_list.size());
            temp_list.add(name);
        }
    }


    private void setup_existing_ojects_list(){
        for(int j=0;j<temp_list.size();j++){
            String temp_name=temp_list.get(j);
            for(int k=0;k<songs_list.size();k++){
                if(songs_list.get(k).getSong_name().equals(temp_name)){
                    existing_songs_models_list.add(songs_list.get(k));
                }
            }
        }

        PlayerActivity.saveArrayListObjects(existing_songs_models_list,getString(R.string.existing_objects_list_prefs),mContext);
        Log.d(TAG, "onItemClick: existing size : "+existing_songs_models_list.size());

    }

    AdapterView.OnItemClickListener SongClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {



            setup_existing_ojects_list();

            String download_url=songs_list.get(position).getDownload_url();
            String song_name = songs_list.get(position).getSong_name();
            String songFull = song_name + getString(R.string.mp3_extenstion);
            String lyrics_url=songs_list.get(position).getLyrics_url();




            //if file is there in the fileNameList i-e present in the folder it will play it
            if (filesNameList.contains(songFull)) {

                int index=filesNameList.indexOf(songFull);
                Log.d(TAG, "onItemClick: song found in the arrayList/track_directory");

                startActivity(new Intent(mContext, PlayerActivity.class)
                        .putExtra(getString(R.string.current_index), index)
                        .putStringArrayListExtra(getString(R.string.folder_songs_list), filesNameList)
                        .putParcelableArrayListExtra(getString(R.string.all_songs_object_list), (ArrayList<? extends Parcelable>) songs_list)
                        .putParcelableArrayListExtra(getString(R.string.existing_songs_object_list), (ArrayList<? extends Parcelable>) existing_songs_models_list)
                        .putExtra(getString(R.string.coming_from_playlist_activity)
                                , getString(R.string.coming_from_playlist_activity))
                        .putExtra(getString(R.string.coming_from_playlist_activity_int)
                                ,PLAYLIST_IDENTIFIER)
                );

                Log.d(TAG, "onItemClick: existing size : "+existing_songs_models_list.size());
                Animatoo.animateZoom(mContext);


            }
            //if not downloadSong it
            else {
                Log.d(TAG, "onItemClick: song not found");
                if ((networkInfo == null || !networkInfo.isConnected())) {
                    showToast("Please Check Your Internet Connection!");
                    return;
                } else {

                    //dialog for when downloading
                    dialog = new SpotsDialog.Builder()
                            .setContext(mContext)
                            .setTheme(R.style.Custom)
                            .setCancelable(false)
                            .build();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ads.loadinterstitial(interstitialAd);
                        }
                    });


                    //downloadSong file
                    downloadSong(song_name, track_directory,download_url);
                    if(lyrics_url != null){
                        download_lyrics_file(song_name,lyrics_directory,lyrics_url);

                    }
                }
            }
        }
    };


    private void directorySetup(){
        //track_directory
        track_directory = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/Documents";
        lyrics_directory=Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/Lyrics";

        File direct_track = new File(track_directory);
        File direct_lyrics = new File(lyrics_directory);

        if (!direct_lyrics.exists() ) {
            File myDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Android/data/" + this.getPackageName() + "/files/Lyrics");
            myDirectory.mkdir();
        }

        if (!direct_track.exists() ) {
            File myDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Android/data/" + this.getPackageName() + "/files/Documents");
            myDirectory.mkdir();
        }

    }
    private void internetConnectivity(){
        //Internet Connectivity
        connectivityManager = (ConnectivityManager)
                playlist_Activity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

    }


    private void layoutImageLoading() {
        playlist_background = findViewById(R.id.playlist_background);
        Picasso
                .with(this)
                .load(R.drawable.playlist_backthree)
                //.resize(800, 800)

                .placeholder(R.drawable.playlist_backthreeloading)
                .into(playlist_background);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try{
            if(mUIUpdater != null){
                mUIUpdater.startUpdates();
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        try{
            if(mUIUpdater != null){
                mUIUpdater.stopUpdates();
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public void download_lyrics_file(final String fileNameInto, final String downloadDirectory, String download_url) {




        DownloadFiles downloadFiles = new DownloadFiles(
                mContext,
                ".txt",
                downloadDirectory,
                fileNameInto,
                null);

        downloadFiles.downloadingFiles(download_url);
    }

    public void downloadSong(final String fileNameInto, final String downloadDirectory, String download_url) {




        DownloadFiles downloadFiles = new DownloadFiles(
                mContext,
                ".mp3",
                downloadDirectory,
                fileNameInto,
                dialog);

        final long downloadid = downloadFiles.downloadingFiles(download_url);

        initializeReciever(downloadid);
    }

    private void initializeReciever(final long downloadID) {
        onDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Fetching the downloadSong id received with the broadcast
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                //Checking if the received broadcast is for our enqueued downloadSong by matching downloadSong id
                if (downloadID == id) {


                    Toast.makeText(context, "File Loaded!", Toast.LENGTH_SHORT).show();
                    getFilesListFromFolder();
                    playlist_adapter.notifyDataSetChanged();
                    dialog.dismiss();

                }
            }
        };

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }




    public static void saveChildCountPref(String key,long index,Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, index);
        editor.commit();
    }
    public static long getChildCountPref(String key,Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long myIntValue = prefs.getLong(key, -1);
        return myIntValue;
    }
    public  void setupListView() {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("songs");
        final ProgressDialog dialog=new ProgressDialog(this,R.style.MyAlertDialogStyle);
        dialog.setTitle("Loading..");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        //myRef.addValueEventListener(myRef_ValueEventListener);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final long count =dataSnapshot.getChildrenCount();
                saveChildCountPref(getString(R.string.child_count_playlist),count,mContext);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        myRef.keepSynced(true);
        //myRef.addChildEventListener(myRef_ChildEventListener);
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Song song = dataSnapshot.getValue(Song.class);
                songs_list.add(song);



                //when reaches the final child and List is complete it will reverse the list
                if(songs_list.size() == getChildCountPref(getString(R.string.child_count_playlist),mContext)){
                    Collections.reverse(songs_list);
                }



                playlist_adapter = new SongRecyclerView(mContext, R.layout.item, songs_list);
                listView.setAdapter(playlist_adapter);
                dialog.dismiss();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //remove listeners
        //myRef.removeEventListener(myRef_ChildEventListener);
        //myRef.removeEventListener(myRef_ValueEventListener);


    }



    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onStop() {
        super.onStop();


    }

    public void showToast(String msg) {

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();
        Animatoo.animateSplit(mContext);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (onDownloadComplete != null) {
            try{
                unregisterReceiver(onDownloadComplete);
            }catch (NullPointerException e){
                Log.d(TAG, "onDestroy: NullPointerException " +e.getMessage());
            }catch (IllegalArgumentException e){
                Log.d(TAG, "onDestroy: IllegalArgumentException " +e.getMessage());
            }catch (IllegalStateException e){
                Log.d(TAG, "onDestroy: IllegalStateException " +e.getMessage());
            }

        }


    }
}







