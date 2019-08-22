package com.obcomdeveloper.realmusic.Extras;


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
import android.os.Parcelable;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.obcomdeveloper.realmusic.DataSource.UniversalViewModel;
import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.Adapters.ExtraListAdapter;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Songs.PlayerActivity;
import com.obcomdeveloper.realmusic.Utils.Ads;
import com.obcomdeveloper.realmusic.Utils.DownloadFiles;
import com.obcomdeveloper.realmusic.Utils.SharedPreferences;
import com.obcomdeveloper.realmusic.room.DatabaseTransactions;
import com.obcomdeveloper.realmusic.room.ExtrasEntity;
import com.obcomdeveloper.realmusic.room.PlaylistEntity;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class Extras extends AppCompatActivity implements ExtraListAdapter.ExtrasItemClickListener {
    private static final String TAG = "Extras";
    public static final int EXTRA_IDENTIFIER=10;
    static AlertDialog dialog;
    private List<Song> songs_list_for_click;
    private RecyclerView recyclerView;
    private ImageView mBackground;
    private Context mContext = Extras.this;
    private String directory,lyrics_directory;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;

    //static Progress progress;

    private BroadcastReceiver onDownloadComplete;
    private ArrayList<String> filesNameList = new ArrayList<>();

    public static ExtraListAdapter adapter;

    private EditText searchText;

    private TextView patentTextView;

    //ads
    private AdView adView;
    private Ads ads;
    private InterstitialAd interstitialAd;

    //
    private List<String> temp_list;
    private List<Song> existing_songs_models_list;

    private ProgressDialog dialog_loading;
    private UniversalViewModel mUniversalViewModel;

    private SharedPreferences mSharedPrefs;
    private FloatingActionButton floatingActionButton;
    private CompositeDisposable mDisposibles=new CompositeDisposable();
    private int pageNumber=1;
    private boolean loading = false;
    private final int VISIBLE_THRESHOLD = 1;
    private int lastVisibleItem;
    private int totalItemCount;
    private ProgressBar loadingProgress;

    private LinearLayoutManager layoutManager;
    private DatabaseTransactions mDatabaseTransactions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_extras);
        recyclerView = findViewById(R.id.recyclerviewextra);
        searchText=findViewById(R.id.searchExtra);
        patentTextView =findViewById(R.id.patent_extra);
        mUniversalViewModel =new UniversalViewModel(Extras.this);
        mSharedPrefs=new SharedPreferences(Extras.this);
        floatingActionButton=findViewById(R.id.floating_button);
        floatingActionButton.setOnClickListener(FloatingButtonClickListener);
        layoutManager=new LinearLayoutManager(this);
        loadingProgress=findViewById(R.id.pagination_progressBar);
        mDatabaseTransactions=new DatabaseTransactions(this);


        patentTextView.setHorizontallyScrolling(true);
        patentTextView.setSelected(true);


        adView =findViewById(R.id.adView);



        songs_list_for_click = new ArrayList<>();


        adapter = new ExtraListAdapter(mContext, R.layout.item_extra, Extras.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        //methods
        directorySetup();
        getFilesListFromFolder();
        layoutImageLoading();
        internetConnectivity();
        if(networkInfo == null || !networkInfo.isConnected()){
            //loadFromDatabase();
            //add data to songs_for_click list
            //add data to existing_songs_model
            showProgressDialog();
            mDatabaseTransactions.getAllSongsExtras().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<ExtrasEntity>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                            mDisposibles.add(d);
                        }

                        @Override
                        public void onNext(List<ExtrasEntity> list) {

                            for(int i = 0; i< list.size(); i++){

                                String song_name = list.get(i).getSong_name();
                                String download_url = list.get(i).getDownload_url();
                                String genius_url = list.get(i).getGenius_url();
                                int thumbnail = list.get(i).getThumbnail();
                                String artist_name = list.get(i).getArtist_name();
                                String lyrics_url = list.get(i).getLyrics_url();

                                Song song=new Song(String.valueOf(i),song_name,download_url,genius_url
                                        ,String.valueOf(thumbnail),null,artist_name,lyrics_url);

                                songs_list_for_click.add(song);
                                existing_songs_models_list.add(song);

                            }

                            adapter.addItems(songs_list_for_click);

                            dialog_loading.dismiss();

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }else {

            fetchDataandSet();

        }

        setupSearch();


        //Listeners


        //ads
        interstitialAd=new InterstitialAd(this);
        ads=new Ads();
        ads.initAdMob(this);
        ads.setupBanner(adView);
        ads.setupInterstitial(this,getString(R.string.interstitial_ad_test_unit_id),interstitialAd);

    }
    View.OnClickListener FloatingButtonClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            continuePlayingSong();
        }
    };


    private void continuePlayingSong(){

        setup_existing_ojects_list();

        int tempPlaying=mSharedPrefs.getInt(mContext.getString(R.string.shared_current_index),0);

        if( tempPlaying != -1 && existing_songs_models_list.size() > tempPlaying){


            String song_name = existing_songs_models_list.get(mSharedPrefs.getInt(mContext.getString(R.string.shared_current_index),0)).getSong_name();
            String songFull = song_name + getString(R.string.mp3_extenstion);


            //if file is there in the fileNameList i-e present in the folder it will play it
            if (filesNameList.contains(songFull)) {

                int index=filesNameList.indexOf(songFull);

                startActivity(new Intent(Extras.this, PlayerActivity.class)
                        .putExtra(getString(R.string.current_index), index)
                        .putStringArrayListExtra(getString(R.string.folder_songs_list), filesNameList)
                        .putParcelableArrayListExtra(getString(R.string.existing_songs_object_list), (ArrayList<? extends Parcelable>) existing_songs_models_list)
                        .putExtra(getString(R.string.coming_from_extra_activity)
                                , getString(R.string.coming_from_extra_activity))
                        .putExtra(getString(R.string.coming_from_extra_activity_int)
                                ,EXTRA_IDENTIFIER)
                );

                Animatoo.animateZoom(mContext);


            }
        }else {
            Toast.makeText(mContext, "Play a Song First", Toast.LENGTH_LONG).show();
        }


    }

    private void setupSearch(){
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                try{
                    String text = searchText.getText().toString().toLowerCase(Locale.getDefault());
                    adapter.filter(text);
                }catch (NullPointerException e){
                    Log.d(TAG, "afterTextChanged: NullPointerException" +e.getMessage());
                }

            }
        });
    }

    private void layoutImageLoading() {
        mBackground = findViewById(R.id.background);
        Picasso
                .with(this)
                .load(R.drawable.extras_background)
                //.resize(800, 800)

                .placeholder(R.drawable.extras_backgroundloading)
                .into(mBackground);

    }

    private void directorySetup() {
        //directory
        directory = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/Extras/";
        lyrics_directory=Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/Lyrics";

        File direct = new File(directory);
        File direct_lyrics = new File(lyrics_directory);

        if (!direct_lyrics.exists() ) {
            File myDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Android/data/" + this.getPackageName() + "/files/Lyrics");
            myDirectory.mkdir();
        }

        if (!direct.exists()) {
            File myDirectory = new File(Environment.getExternalStorageDirectory().
                    getAbsolutePath() + "/Android/data/" + this.getPackageName() + "/files/Extras/");
            myDirectory.mkdir();
        }

    }

    private void getFilesListFromFolder() {
        temp_list=new ArrayList<>();
        existing_songs_models_list=new ArrayList<>();
        int filesIndex = 0;
        //String path = directory;
        Log.d("Folder Files", "Path: " + directory);
        File directoryFolder = new File(directory);
        File[] files = directoryFolder.listFiles();
        //Log.d("Folder Files", "Size: "+ files.length);

        if (files.length == 0) {
            Log.d(TAG, "onCreate: Folder Empty");

        } else {

            for (int i = 0; i < files.length; i++) {
                Log.d("Files", "FileName:" + files[i].getName());
                filesNameList.add(filesIndex, files[i].getName());
                filesIndex++;
            }
            Log.d(TAG, "getFilesListFromFolder: filesNameIndex : "+filesNameList.size());

            mSharedPrefs.saveList(filesNameList, getString(R.string.shared_array_list_key));
        }


        folder_files_to_temp_list();


    }

    private void folder_files_to_temp_list(){
        for(int i=0;i<filesNameList.size();i++){
            String name= filesNameList.get(i).replace(".mp3","");
            Log.d(TAG, "getFilesListFromFolder: temp_name : "+name+ "songs_list_size : "+songs_list_for_click.size());
            temp_list.add(name);
        }
        Log.d(TAG, "getFilesListFromFolder: temp_list : "+temp_list.size());
    }

    private void internetConnectivity() {
        //Internet Connectivity
        connectivityManager = (ConnectivityManager)
                Extras.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

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

    public void download(final String fileNameInto, final String downloadDirectory,String download_url,int position,String artistName) {



        DownloadFiles downloadFiles = new DownloadFiles(
                mContext,
                ".mp3",
                downloadDirectory,
                fileNameInto,
                dialog);

        final long downloadid = downloadFiles.downloadingFiles(download_url);

        initializeReciever(downloadid,fileNameInto,position,artistName);
    }

    private void initializeReciever(final long downloadID,String song_name,int position,String artist_name) {
        onDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Fetching the downloadSong id received with the broadcast
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                //Checking if the received broadcast is for our enqueued downloadSong by matching downloadSong id
                if (downloadID == id) {


                    Toast.makeText(context, "File Loaded", Toast.LENGTH_SHORT).show();
                    getFilesListFromFolder();
                    adapter.notifyDataSetChanged();

                    ExtrasEntity songNew =new ExtrasEntity(position+1,song_name,R.drawable.offline_extras_small_edited,artist_name,null,null,null);
                    enterSongInDatabase(songNew);

                    dialog.dismiss();

                }
            }
        };

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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

    private void showProgressDialog(){
        dialog_loading = new ProgressDialog(this,R.style.MyAlertDialogStyle);
        dialog_loading.setTitle("Loading..");
        dialog_loading.setCanceledOnTouchOutside(false);
        dialog_loading.show();
    }
    private void fetchDataandSet() {

        showProgressDialog();

        Maybe<List<Song>> extrasObservable= mUniversalViewModel.getSongsExtrasList(pageNumber);

        extrasObservable
                .subscribe(new MaybeObserver<List<Song>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposibles.add(d);
                    }

                    @Override
                    public void onSuccess(List<Song> list) {

                        Collections.reverse(list);

                        songs_list_for_click.addAll(list);

                        adapter.addItems(list);

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

                    int estimatePages=mSharedPrefs.getInt("estimatePages",0) ;
                    Log.d(TAG, "onScrolled: estimate Pages : "+estimatePages);

                    if( pageNumber <= estimatePages + 1 &&  estimatePages >= 2){

                        loadingProgress.setVisibility(View.VISIBLE);

                        mUniversalViewModel.getSongsExtrasList(pageNumber).
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

                                        if(adapter!=null){

                                            songs_list_for_click.addAll(sublist);

                                            adapter.addItems(sublist);
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


                }
            }
        });

    }

    private void setup_existing_ojects_list(){
        for(int j=0;j<temp_list.size();j++){
            String temp_name=temp_list.get(j);
            for(int k=0;k<songs_list_for_click.size();k++){
                if(songs_list_for_click.get(k).getSong_name().equals(temp_name)){
                    existing_songs_models_list.add(songs_list_for_click.get(k));
                }
            }
        }

    }

    //////-------------------------------LISTENERS------------------------/////////////

    @Override
    public void onItemClickListener(Song song, int position, View view) {
        setup_existing_ojects_list();

        String download_url=songs_list_for_click.get(position).getDownload_url();
        String song_name = songs_list_for_click.get(position).getSong_name();
        String songFull = song_name + getString(R.string.mp3_extenstion);
        String lyrics_url=songs_list_for_click.get(position).getLyrics_url();
        String artist_name=songs_list_for_click.get(position).getArtist_name();



        //if file is there in the fileNameList i-e present in the folder it will play it
        if (filesNameList.contains(songFull)) {
            int index=filesNameList.indexOf(songFull);
            Log.d(TAG, "onItemClick: song found in the arrayList/directory");

            startActivity(new Intent(Extras.this, PlayerActivity.class)
                    .putExtra(getString(R.string.current_index), index)
                    .putStringArrayListExtra(getString(R.string.folder_songs_list), filesNameList)
                    .putParcelableArrayListExtra(getString(R.string.existing_songs_object_list), (ArrayList<? extends Parcelable>) existing_songs_models_list)
                    .putExtra(getString(R.string.coming_from_extra_activity)
                            , getString(R.string.coming_from_extra_activity))
                    .putExtra(getString(R.string.coming_from_extra_activity_int)
                            ,EXTRA_IDENTIFIER)
            );
            Log.d(TAG, "getFilesListFromFolder: existing_songs_models_list : "+existing_songs_models_list.size());
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
                        .setContext(Extras.this)
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
                download(song_name, directory,download_url,position,artist_name);

                if(lyrics_url != null){
                    download_lyrics_file(song_name,lyrics_directory,lyrics_url);
                }

            }
        }
    }

    private void enterSongInDatabase(ExtrasEntity song) {
        mDatabaseTransactions.addSongExtras(song)
                .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposibles.add(d);
                    }

                    @Override
                    public void onComplete() {

                        Log.d(TAG, "onComplete: Extras Added Song : "+song.toString());
                        Log.d(TAG, "onComplete: Extras Song Added TO Database");

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });


    }

    //////-------------------------------LISTENERS------------------------/////////////

}