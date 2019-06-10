package com.homie.nf.Extras;


import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.homie.nf.Models.Song;
import com.homie.nf.Models.SongAdapter;
import com.homie.nf.R;
import com.homie.nf.Songs.PlayerActivity;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

import static com.homie.nf.Extras.Extras.dialog;


public class Extras extends AppCompatActivity {
    private static final String TAG = "Extras";
    static AlertDialog dialog;
    private List<Song> songs_list;
    private ListView listView;
    private ImageView mBackground;
    private Context mContext = Extras.this;
    private String directory;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private StorageReference storageReference;


    //static Progress progress;

    private BroadcastReceiver onDownloadComplete;
    private ArrayList<String> filesNameList = new ArrayList<>();

    private SongAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_extras);
        listView = findViewById(R.id.recyclerviewextra);
        mBackground = findViewById(R.id.background);

        songs_list = new ArrayList<>();

        directorySetup();
        getFilesListFromFolder();
        //initImageLoader();
        //layoutImageLoading();
        internetConnectivity();

        Picasso
                .with(this)
                .load(R.drawable.playlist_blurred)
                //.resize(800, 800)

                .placeholder(R.drawable.madeinsociety)
                .into(mBackground);

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("songs");


        final ArrayList<String> serverSongs=new ArrayList<>();

//        CollectionReference collectionReference= FirebaseFirestore.getInstance().collection("songs");
//        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                Log.d(TAG, "onComplete: ");
//
//                for (QueryDocumentSnapshot document:task.getResult()){
//                    String singleSong=document.getString("song_name");
//                    serverSongs.add(singleSong);
//                    Log.d(TAG, "onComplete: "+document.getString("song_name"));
//                }
//
//            }
//        });






//        int i=19;
//        while(i>=0){
//            DatabaseReference songRef = FirebaseDatabase.getInstance().getReference().child("wallpapers").push();
//
//            Map<String,String> map=new HashMap<>();
//            map.put(getString(R.string.wall_id),""+i);
//            map.put(getString(R.string.wall_download_url),"null");
//            songRef.setValue(map);
//            i--;
//        }


        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Song song = dataSnapshot.getValue(Song.class);
                songs_list.add(song);
                adapter = new SongAdapter(mContext, R.layout.item, songs_list);
                listView.setAdapter(adapter);

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


        Log.d(TAG, "onCreate: songs_list:  " + songs_list.size());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(mContext, "Item Clicked : " + position, Toast.LENGTH_LONG).show();

                String download_url=songs_list.get(position).getDownload_url();
                String genius_url = songs_list.get(position).getGenius_url();
                String song_name = songs_list.get(position).getSong_name();
                String songFull = song_name + getString(R.string.mp3_extenstion);

                String lyrics_file_name = songFull
                        .replace(getString(R.string.mp3_extenstion), getString(R.string.txt_extenstion));

                //if file is there in the fileNameList i-e present in the folder it will play it
                if (filesNameList.contains(songFull)) {
                    int index=filesNameList.indexOf(songFull);
                    Log.d(TAG, "onItemClick: song found in the arrayList/directory");
                    showToast("Playing..");

                    startActivity(new Intent(Extras.this, PlayerActivity.class)
                            .putExtra(getString(R.string.position_song), index)
                            .putStringArrayListExtra(getString(R.string.songslist), filesNameList)
                            .putExtra(getString(R.string.songname), song_name)
                            .putExtra(getString(R.string.GENIUSFILENAME), genius_url)
                            .putExtra(getString(R.string.LYRICSFILE), lyrics_file_name)
                            .putExtra(getString(R.string.coming_from_extra_activity)
                                    , getString(R.string.coming_from_extra_activity))
                            .putExtra(getString(R.string.coming_from_extra_activity_int)
                                    ,10)
                    );
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
                        //download file
                        download(song_name, directory,download_url);
                    }
                }
            }
        });
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
            PlayerActivity.saveArrayList(filesNameList, getString(R.string.extras_songs_list), mContext);
        }
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


                    Toast.makeText(context, "File Saved", Toast.LENGTH_SHORT).show();
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
            unregisterReceiver(onDownloadComplete);
        }


    }
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
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);


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






