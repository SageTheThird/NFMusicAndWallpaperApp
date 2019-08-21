package com.obcomdeveloper.realmusic.Songs;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.obcomdeveloper.realmusic.Adapters.SongRecyclerView;
import com.obcomdeveloper.realmusic.DataSource.UniversalViewModel;
import com.obcomdeveloper.realmusic.Models.Quote;
import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Utils.Ads;

import com.obcomdeveloper.realmusic.Utils.DownloadFiles;
import com.obcomdeveloper.realmusic.Utils.UIUpdater;
import com.obcomdeveloper.realmusic.room.DatabaseTransactions;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class PlaylistActivity extends AppCompatActivity implements SongRecyclerView.PlaylistClickListener {
    private static final String TAG = "PlaylistActivity";

    public static final int AD_TIME_INTERVAL = 90 * 1000;
    public static final int RANDOM_QUOTE_INTERVAL = 20 * 1000;

    public static final int PLAYLIST_IDENTIFIER = 20;
    public RecyclerView recyclerView;
    static AlertDialog dialog;
    private ImageView playlist_background;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    //static Progress progress;
    private BroadcastReceiver onDownloadComplete;
    private Context mContext = PlaylistActivity.this;
    private String track_directory, lyrics_directory;


    private List<String> filesNameList = new ArrayList<>();

    public static SongRecyclerView playlist_adapter;

    private List<Song> songs_list_forclick;

    private EditText searchView;

    private TextView patentTextView, random_quotes_tv;

    //ads
    private AdView adView;
    private Ads ads;
    private InterstitialAd interstitialAd;


    private Handler mHandler = new Handler();

    //
    private List<String> temp_list;
    private List<Song> existing_songs_models_list;

    //random_quotes_tv for textview
    private DatabaseReference quotes_Ref;

    private int count_quotes;

    private UIUpdater mUIUpdater;

    private List<String> random_quotes_list;

    private FloatingActionButton floatingActionButton;

    private UniversalViewModel mUniversalViewModel;

    private ProgressDialog dialog_loading;

    private com.obcomdeveloper.realmusic.Utils.SharedPreferences mSharedPrefs;

    private CompositeDisposable mDisposibles = new CompositeDisposable();

    private ProgressBar loadingProgress;

    private DatabaseTransactions mDatabaseTransactions;

    //Pagination

    private int pageNumber = 1;
    private boolean loading = false;
    private final int VISIBLE_THRESHOLD = 1;
    private int lastVisibleItem;
    private int totalItemCount;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_playlist_);
        quotes_Ref = FirebaseDatabase.getInstance().getReference().child(getString(R.string.db_random_quotes)).child("array");

        searchView = findViewById(R.id.searchEdit);
        recyclerView = findViewById(R.id.listViewPlaylist);
        patentTextView = findViewById(R.id.patent_playlist);
        random_quotes_tv = findViewById(R.id.random_quotes_playlist);
        adView = findViewById(R.id.adView);
        interstitialAd = new InterstitialAd(this);
        songs_list_forclick = new ArrayList<>();
        random_quotes_list = new ArrayList<>();
        floatingActionButton = findViewById(R.id.floating_button);
        loadingProgress=findViewById(R.id.pagination_progressBar);
        floatingActionButton.setOnClickListener(FloatingButtonClickListener);
        layoutManager=new LinearLayoutManager(this);

        patentTextView.setHorizontallyScrolling(true);
        patentTextView.setSelected(true);

        random_quotes_tv.setHorizontallyScrolling(true);
        random_quotes_tv.setSelected(true);
        mUniversalViewModel = new UniversalViewModel(PlaylistActivity.this);
        mSharedPrefs = new com.obcomdeveloper.realmusic.Utils.SharedPreferences(PlaylistActivity.this);


        //methods
        directorySetup();
        getFilesListFromFolder();
        layoutImageLoading();
        internetConnectivity();

        if(networkInfo == null || !networkInfo.isConnected()){
            //loadFromDatabase();
        }else {
            fetchDataandSet();
        }

        searchSetup();


        playlist_adapter = new SongRecyclerView(mContext, R.layout.item,PlaylistActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(playlist_adapter);
        //listeners
        //recyclerView.setOnItemClickListener(SongClickListener);


        //ads
        ads = new Ads();
        ads.initAdMob(this);
        ads.setupBanner(adView);
        ads.setupInterstitial(this, getString(R.string.interstitial_ad_test_unit_id), interstitialAd);


        initQuotesTv();
        setupQuotesTv();


        mAdRunnable.run();
        //

    }

    private void setupQuotesTv() {


        mUIUpdater = new UIUpdater(new Runnable() {
            @Override
            public void run() {
                //do what ever you want to do with your textview
                try {

                    random_quotes_list = mSharedPrefs.getList(getString(R.string.random_quotes_list_prefs));
                    count_quotes = mSharedPrefs.getInt(getString(R.string.count_random_quotes), 1);
                    random_quotes_tv.setText(random_quotes_list.get(count_quotes));
                    if (count_quotes == random_quotes_list.size() - 1) {
                        count_quotes = 0;
                        mSharedPrefs.saveInt(getString(R.string.count_random_quotes), count_quotes);
                    } else {
                        count_quotes++;
                        mSharedPrefs.saveInt(getString(R.string.count_random_quotes), count_quotes);
                    }


                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        Maybe<Quote> quotesObservable = mUniversalViewModel.getQuotes();

        quotesObservable.subscribe(new MaybeObserver<Quote>() {
            @Override
            public void onSubscribe(Disposable d) {

                mDisposibles.add(d);

            }

            @Override
            public void onSuccess(Quote quote) {

                String temp = quote.getArray();
                String modified_string = temp.substring(1);
                //String[] quotes_array=modified_string.split("-");
                List<String> quotes_list = Arrays.asList(modified_string.split("-"));

                mSharedPrefs.saveList(quotes_list, getString(R.string.random_quotes_list_prefs));

            }

            @Override
            public void onError(Throwable e) {

                Toast.makeText(mContext, "" + e.getMessage(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onComplete() {

            }
        });



    }


    private void initQuotesTv(){


        if(mSharedPrefs.getBoolean(getString(R.string.setCountToZero), true)) {

            count_quotes=0;
            mSharedPrefs.saveInt(getString(R.string.count_random_quotes),count_quotes);
            mSharedPrefs.saveBoolean(getString(R.string.setCountToZero), false);

        }


    }

    private void continuePlayingSong(){

        setup_existing_ojects_list();

        int tempPlaying=mSharedPrefs.getInt(mContext.getString(R.string.shared_current_index),0);

        if( tempPlaying != -1 && existing_songs_models_list.size() > tempPlaying){


            String song_name = existing_songs_models_list.get(mSharedPrefs.getInt(mContext.getString(R.string.shared_current_index),0)).getSong_name();
            String songFull = song_name + getString(R.string.mp3_extenstion);

            filesNameList=mSharedPrefs.getList(getString(R.string.shared_array_list_key));

            //if file is there in the fileNameList i-e present in the folder it will play it
            if (filesNameList.contains(songFull)) {

                int index=filesNameList.indexOf(songFull);

                startActivity(new Intent(mContext, PlayerActivity.class)
                        .putExtra(getString(R.string.current_index), index)
                        .putStringArrayListExtra(getString(R.string.folder_songs_list), (ArrayList<String>) filesNameList)
                        .putParcelableArrayListExtra(getString(R.string.existing_songs_object_list), (ArrayList<? extends Parcelable>) existing_songs_models_list)
                        .putExtra(getString(R.string.coming_from_playlist_activity)
                                , getString(R.string.coming_from_playlist_activity))
                        .putExtra(getString(R.string.coming_from_playlist_activity_int)
                                ,PLAYLIST_IDENTIFIER)
                );


                Animatoo.animateZoom(mContext);


            }
        }else {
            Toast.makeText(mContext, "Play a Song First", Toast.LENGTH_LONG).show();
        }


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





    View.OnClickListener FloatingButtonClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            continuePlayingSong();
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

        File directoryFolder = new File(track_directory);
        File[] files = directoryFolder.listFiles();


        if(files.length == 0){

            Log.d(TAG, "onCreate: Folder Empty");

        }else {

            for (int i = 0; i < files.length; i++)
            {

                filesNameList.add(filesIndex,files[i].getName());
                filesIndex++;

            }

            mSharedPrefs.saveList(filesNameList,getString(R.string.shared_array_list_key));
        }

        folder_files_to_temp_list();


    }

    private void folder_files_to_temp_list(){

        for(int i=0;i<filesNameList.size();i++){

            String name= filesNameList.get(i).replace(".mp3","");
            temp_list.add(name);

        }
    }


    private void setup_existing_ojects_list(){
        for(int j=0;j<temp_list.size();j++){

            String temp_name=temp_list.get(j);

            for(int k=0;k<songs_list_forclick.size();k++){

                if(songs_list_forclick.get(k).getSong_name().equals(temp_name)){

                    existing_songs_models_list.add(songs_list_forclick.get(k));

                }
            }
        }

        mSharedPrefs.saveObjectsList(existing_songs_models_list,getString(R.string.existing_objects_list_prefs));


    }


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
                PlaylistActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
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


    public  void fetchDataandSet() {

        showProgressDialog();

        Maybe<List<Song>> playlistObservable= mUniversalViewModel.getSongsPlaylist(pageNumber);

        playlistObservable.
                subscribe(new MaybeObserver<List<Song>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                        mDisposibles.add(d);

                    }

                    @Override
                    public void onSuccess(List<Song> songs) {

                        Collections.reverse(songs);

                        songs_list_forclick.addAll(songs);

                        playlist_adapter.addItems(songs);

                        dialog_loading.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {



                    }
                });


        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount= layoutManager.getItemCount();
                lastVisibleItem= layoutManager.findLastVisibleItemPosition();

                if (!loading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {

                    loading = true;
                    pageNumber++;


                    if( pageNumber <= mSharedPrefs.getInt("estimatePages",0) + 1){

                        loadingProgress.setVisibility(View.VISIBLE);

                        mUniversalViewModel.getSongsPlaylist(pageNumber).
                                subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new MaybeObserver<List<Song>>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                        mDisposibles.add(d);

                                    }

                                    @Override
                                    public void onSuccess(List<Song> list) {
                                        //playlist_adapter.clearList();
                                        Collections.reverse(list);
                                        int estimatePages=mSharedPrefs.getInt("estimatePages",0);

                                        List<Song> sublist=new ArrayList<>();

                                        if(pageNumber <= estimatePages){

                                            int tempPage=pageNumber -1;
                                            sublist=list.subList( (tempPage * 9) +tempPage,list.size());

                                        }else {
                                            sublist=list;
                                        }




                                        //list.remove(6);
                                        if(playlist_adapter!=null){

                                            //playlist_adapter.clearList();

                                            for(int i=0;i<sublist.size();i++){
                                                Log.d(TAG, "onSuccess: SongId's : "+sublist.get(i).getId() + "  "+sublist.get(i).getSong_name());
                                            }
                                            Log.d(TAG, "onSuccess: ----------------------------------------------");

                                            songs_list_forclick.addAll(sublist);


                                            //playlist_adapter.removeLastItem();
                                            playlist_adapter.addItems(sublist);
                                            if(loadingProgress.getVisibility() == View.VISIBLE){
                                                loadingProgress.setVisibility(View.INVISIBLE);
                                            }

                                            //playlist_adapter.notifyDataSetChanged();
                                            loading = false;
                                        }

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                    }


                    //paginator.onNext(pageNumber);


                }
            }
        });



    }

    private void showProgressDialog(){
        dialog_loading=new ProgressDialog(this,R.style.MyAlertDialogStyle);
        dialog_loading.setTitle("Loading..");
        dialog_loading.setCanceledOnTouchOutside(false);
        dialog_loading.show();
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
                mDisposibles.clear();
            }catch (NullPointerException e){
                Log.d(TAG, "onDestroy: NullPointerException " +e.getMessage());
            }catch (IllegalArgumentException e){
                Log.d(TAG, "onDestroy: IllegalArgumentException " +e.getMessage());
            }catch (IllegalStateException e){
                Log.d(TAG, "onDestroy: IllegalStateException " +e.getMessage());
            }

        }


    }

    @Override
    public void onItemClickListener(Song song, int position, View view) {

        setup_existing_ojects_list();

        String download_url=songs_list_forclick.get(position).getDownload_url();
        String song_name = songs_list_forclick.get(position).getSong_name();
        String songFull = song_name + getString(R.string.mp3_extenstion);
        String lyrics_url=songs_list_forclick.get(position).getLyrics_url();

        

        filesNameList=mSharedPrefs.getList(getString(R.string.shared_array_list_key));


        //if file is there in the fileNameList i-e present in the folder it will play it
        if (filesNameList.contains(songFull)) {

            int index=filesNameList.indexOf(songFull);

            startActivity(new Intent(mContext, PlayerActivity.class)
                    .putExtra(getString(R.string.current_index), index)
                    .putStringArrayListExtra(getString(R.string.folder_songs_list), (ArrayList<String>) filesNameList)
                    .putParcelableArrayListExtra(getString(R.string.existing_songs_object_list), (ArrayList<? extends Parcelable>) existing_songs_models_list)
                    .putExtra(getString(R.string.coming_from_playlist_activity)
                            , getString(R.string.coming_from_playlist_activity))
                    .putExtra(getString(R.string.coming_from_playlist_activity_int)
                            ,PLAYLIST_IDENTIFIER)
            );


            Animatoo.animateZoom(mContext);


        }
        //if not downloadSong it
        else {

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
}







