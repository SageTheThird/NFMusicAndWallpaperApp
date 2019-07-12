package com.obcomdeveloper.realmusic.Extras;


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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.Adapters.ExtraListAdapter;
import com.obcomdeveloper.realmusic.R;
import com.obcomdeveloper.realmusic.Songs.PlayerActivity;
import com.obcomdeveloper.realmusic.Utils.Ads;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import needle.Needle;

import static com.obcomdeveloper.realmusic.Extras.Extras.dialog;


public class Extras extends AppCompatActivity {
    private static final String TAG = "Extras";
    public static final int EXTRA_IDENTIFIER=10;
    static AlertDialog dialog;
    private List<Song> songs_list;
    private ListView listView;
    private ImageView mBackground;
    private Context mContext = Extras.this;
    private String directory;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_extras);
        listView = findViewById(R.id.recyclerviewextra);
        searchText=findViewById(R.id.searchExtra);
        patentTextView =findViewById(R.id.patent_extra);


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





//        FOR DATABASE ENTRY
//        int i=20;
//        while(i>=0){
//            DatabaseReference songRef = FirebaseDatabase.getInstance().getReference().child("extras").push();
//
//            Map<String,String> map=new HashMap<>();
//            map.put(getString(R.string.song_id),""+i);
//            map.put(getString(R.string.song_genius_url),"null");
//            map.put(getString(R.string.song_download_url),"null");
//            map.put(getString(R.string.song_song_name),"null");
//            songRef.setValue(map);
//            i--;
//        }


        //Listeners
        listView.setOnItemClickListener(SongClickListener);

        //ads
        interstitialAd=new InterstitialAd(this);
        ads=new Ads();
        ads.initAdMob(this);
        ads.setupBanner(adView);
        ads.setupInterstitial(this,getString(R.string.interstitial_ad_test_unit_id),interstitialAd);

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
                .load(R.drawable.playlist_blurred)
                //.resize(800, 800)

                .placeholder(R.drawable.madeinsociety)
                .into(mBackground);

    }

    private void directorySetup() {
        //directory
        directory = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/Extras/";

        File direct = new File(directory);

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
            PlayerActivity.saveArrayList(filesNameList, getString(R.string.shared_array_list_key), mContext);
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


    public void download(final String fileNameInto, final String downloadDirectory,String download_url) {



        DownloadFilesExtra downloadFiles = new DownloadFilesExtra(
                mContext,
                ".mp3",
                downloadDirectory,
                fileNameInto,
                onDownloadComplete);

        final long downloadid = downloadFiles.downloadingFiles(download_url);

        initializeReciever(downloadid);
    }

    private void initializeReciever(final long downloadID) {
        onDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Fetching the download id received with the broadcast
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                //Checking if the received broadcast is for our enqueued download by matching download id
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
            }catch (NullPointerException e){
                Log.d(TAG, "onDestroy: NullPointerException " +e.getMessage());
            }catch (IllegalArgumentException e){
                Log.d(TAG, "onDestroy: IllegalArgumentException " +e.getMessage());
            }catch (IllegalStateException e){
                Log.d(TAG, "onDestroy: IllegalStateException " +e.getMessage());
            }

        }


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

    private void setupListView() {
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("extras");
        final ProgressDialog dialog = new ProgressDialog(this,R.style.MyAlertDialogStyle);
        dialog.setTitle("Loading..");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        //myRef.addValueEventListener(myRef_ValueEventListener);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final long count =dataSnapshot.getChildrenCount();
                saveChildCountPref(getString(R.string.child_count_extras),count,mContext);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Song song = dataSnapshot.getValue(Song.class);
                songs_list.add(song);



                //when reaches the final child and List is complete it will reverse the list
                if(songs_list.size() == getChildCountPref(getString(R.string.child_count_extras),mContext)){
                    Collections.reverse(songs_list);
                }

                adapter = new ExtraListAdapter(mContext, R.layout.item_extra, songs_list);
                listView.setAdapter(adapter);
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
        myRef.keepSynced(true);

        //myRef.addChildEventListener(myRef_ChildEventListener);

        //remove listeners
        //myRef.removeEventListener(myRef_ValueEventListener);
        //myRef.removeEventListener(myRef_ChildEventListener);


    }

    private void setup_existing_ojects_list(){
        for(int j=0;j<temp_list.size();j++){
            String temp_name=temp_list.get(j);
            for(int k=0;k<getChildCountPref(getString(R.string.child_count_extras),mContext);k++){
                if(songs_list.get(k).getSong_name().equals(temp_name)){
                    existing_songs_models_list.add(songs_list.get(k));
                }
            }
        }

        Log.d(TAG, "getFilesListFromFolder: existing_songs_models_list : "+existing_songs_models_list.size());
    }

    //////-------------------------------LISTENERS------------------------/////////////

    AdapterView.OnItemClickListener SongClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            setup_existing_ojects_list();

            String download_url=songs_list.get(position).getDownload_url();
            String song_name = songs_list.get(position).getSong_name();
            String songFull = song_name + getString(R.string.mp3_extenstion);



            //if file is there in the fileNameList i-e present in the folder it will play it
            if (filesNameList.contains(songFull)) {
                int index=filesNameList.indexOf(songFull);
                Log.d(TAG, "onItemClick: song found in the arrayList/directory");

                startActivity(new Intent(Extras.this, PlayerActivity.class)
                        .putExtra(getString(R.string.current_index), index)
                        .putStringArrayListExtra(getString(R.string.folder_songs_list), filesNameList)
                        .putParcelableArrayListExtra(getString(R.string.all_songs_object_list), (ArrayList<? extends Parcelable>) songs_list)
                        .putParcelableArrayListExtra(getString(R.string.existing_songs_object_list), (ArrayList<? extends Parcelable>) existing_songs_models_list)
                        .putExtra(getString(R.string.coming_from_extra_activity)
                                , getString(R.string.coming_from_extra_activity))
                        .putExtra(getString(R.string.coming_from_extra_activity_int)
                                ,EXTRA_IDENTIFIER)
                );
                Log.d(TAG, "getFilesListFromFolder: existing_songs_models_list : "+existing_songs_models_list.size());
                Animatoo.animateZoom(mContext);


            }
            //if not download it
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

                    //download file
                    download(song_name, directory,download_url);
                }
            }
        }
    };


    //////-------------------------------LISTENERS------------------------/////////////

}

class DownloadFilesExtra {

    private Context context;
    private String fileExtension;
    private String destinationDirectory;
    private String fileName;
    private BroadcastReceiver onDownloadCompleteAsync;

    public DownloadFilesExtra(Context context,
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






