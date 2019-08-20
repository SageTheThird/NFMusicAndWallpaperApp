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
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.obcomdeveloper.realmusic.DataSource.UniversalViewModel;
import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.Adapters.ExtraListAdapter;
import com.obcomdeveloper.realmusic.Models.Wallpaper;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Songs.PlayerActivity;
import com.obcomdeveloper.realmusic.Utils.Ads;
import com.obcomdeveloper.realmusic.Utils.DownloadFiles;
import com.obcomdeveloper.realmusic.Utils.SharedPreferences;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class Extras extends AppCompatActivity {
    private static final String TAG = "Extras";
    public static final int EXTRA_IDENTIFIER=10;
    static AlertDialog dialog;
    private List<Song> songs_list;
    private ListView listView;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_extras);
        listView = findViewById(R.id.recyclerviewextra);
        searchText=findViewById(R.id.searchExtra);
        patentTextView =findViewById(R.id.patent_extra);
        mUniversalViewModel =new UniversalViewModel(Extras.this);
        mSharedPrefs=new SharedPreferences(Extras.this);
        floatingActionButton=findViewById(R.id.floating_button);
        floatingActionButton.setOnClickListener(FloatingButtonClickListener);


        patentTextView.setHorizontallyScrolling(true);
        patentTextView.setSelected(true);


        adView =findViewById(R.id.adView);



        songs_list = new ArrayList<>();

        //methods
        directorySetup();
        getFilesListFromFolder();
        layoutImageLoading();
        internetConnectivity();
        setupListView();
        setupSearch();


        //Listeners
        listView.setOnItemClickListener(SongClickListener);

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
            Log.d(TAG, "getFilesListFromFolder: temp_name : "+name+ "songs_list_size : "+songs_list.size());
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

    public void download(final String fileNameInto, final String downloadDirectory,String download_url) {



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


                    Toast.makeText(context, "File Loaded", Toast.LENGTH_SHORT).show();
                    getFilesListFromFolder();
                    adapter.notifyDataSetChanged();
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

    public void setPageNumber(int pageNumber){
        pageNumber=pageNumber;
    }

    private void showProgressDialog(){
        dialog_loading = new ProgressDialog(this,R.style.MyAlertDialogStyle);
        dialog_loading.setTitle("Loading..");
        dialog_loading.setCanceledOnTouchOutside(false);
        dialog_loading.show();
    }
    private void setupListView() {

        showProgressDialog();
        //mUniversalViewModel.setPageNumber(3);
        Maybe<List<Song>> extrasObservable= mUniversalViewModel.getSongsExtrasList();


//        Observable<List<Wallpaper>> extraRealObservable=extrasObservable.
//                flatMapObservable(new Function<List<Wallpaper>, ObservableSource<Wallpaper>>() {
//                    @Override
//                    public ObservableSource<Wallpaper> apply(List<Wallpaper> wallpapers) throws Exception {
//                        List<Wallpaper> items = new ArrayList<>();
//                        List<Observable> list=new ArrayList<>();
//
//                        for (int i = 0; i < pageNumber * 10; i++) {
//                            items.add(wallpapers.get(i));
//
//
//                            Observable<Wallpaper> maybeSource = Observable.just(items.get(i)).subscribeOn(Schedulers.io())
//                                    .observeOn(AndroidSchedulers.mainThread());
//                            list.add(maybeSource);
//                        }
//
//
//                        return list;
//                    }
//        }).subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//                ;
//
//        extraRealObservable.buffer(34).subscribe(new Observer<List<List<Wallpaper>>>() {
//                                                     @Override
//                                                     public void onSubscribe(Disposable d) {
//
//                                                     }
//
//                                                     @Override
//                                                     public void onNext(List<List<Wallpaper>> lists) {
//
//                                                         Log.d(TAG, "onNext: "+lists.size());
//                                                     }
//
//                                                     @Override
//                                                     public void onError(Throwable e) {
//
//                                                         Log.d(TAG, "onError: ");
//                                                     }
//
//                                                     @Override
//                                                     public void onComplete() {
//
//                                                         Log.d(TAG, "onComplete: ");
//                                                     }
//                                                 });

//
//
//        extrasObservable.concatMap(new Function<List<Wallpaper>, Maybe<List<Wallpaper>>>() {
//            @Override
//            public Maybe<List<Wallpaper>> apply(List<Wallpaper> wallpapers) throws Exception {
//
//                List<Wallpaper> items=new ArrayList<>();
//
//                for(int i=0;i< pageNumber *10;i++){
//                    items.add(wallpapers.get(i));
//                }
//
//                Maybe<List<Wallpaper>> maybeSource= Maybe.just(items).subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread());
//
//
//                return maybeSource;
//            }
//        }).subscribe(new MaybeObserver<List<Wallpaper>>() {
//                         @Override
//                         public void onSubscribe(Disposable d) {
//
//                         }
//
//                         @Override
//                         public void onSuccess(List<Wallpaper> wallpapers) {
//
//                             Log.d(TAG, "onSuccess:  "+wallpapers.size());
//
//
//                         }
//
//                         @Override
//                         public void onError(Throwable e) {
//
//                         }
//
//                         @Override
//                         public void onComplete() {
//
//                         }
//                     });

                setPageNumber(4);


        extrasObservable.
                subscribe(new MaybeObserver<List<Song>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                        mDisposibles.add(d);

                    }

                    @Override
                    public void onSuccess(List<Song> songs) {

                        Log.d(TAG, "onSuccess: size " +songs.size());
                        songs_list=songs;
                        Collections.reverse(songs_list);
                        adapter = new ExtraListAdapter(mContext, R.layout.item_extra, songs_list);
                        listView.setAdapter(adapter);
                        dialog_loading.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {



                    }
                });

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

    }

    //////-------------------------------LISTENERS------------------------/////////////

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
                    download(song_name, directory,download_url);
                    if(lyrics_url != null){
                        download_lyrics_file(song_name,lyrics_directory,lyrics_url);
                    }

                }
            }
        }
    };




    //////-------------------------------LISTENERS------------------------/////////////

}