package com.homie.nf.Songs;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.homie.nf.Adapters.SongRecyclerView;
import com.homie.nf.Models.RowItem;
import com.homie.nf.Models.Song;
import com.homie.nf.R;
import com.homie.nf.Utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import dmax.dialog.SpotsDialog;
import static com.homie.nf.Songs.playlist_Activity.dialog;


public class playlist_Activity extends AppCompatActivity {
    private static final String TAG = "playlist_Activity";
    public static ListView listView;
    static AlertDialog dialog;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;
    private ImageView playlist_background;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    //static Progress progress;
    private ArrayList<RowItem> arrayList;
    private BroadcastReceiver onDownloadComplete;
    private Context mContext=playlist_Activity.this;
    private String directory;


    private ArrayList<String> filesNameList=new ArrayList<>();
    private EditText mSearchEditText;

    private SongRecyclerView adapter;

    private List<Song> songs_list;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playlist_);


        //references
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection(getString(R.string.store_song_db));
        songs_list=new ArrayList<>();
        listView = findViewById(R.id.listViewPlaylist);

        //methods
        directorySetup();
        getFilesListFromFolder();
        initImageLoader();
        layoutImageLoading();
        internetConnectivity();
        setupRecyclerView();

        //listeners
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

                    startActivity(new Intent(mContext, PlayerActivity.class)
                            .putExtra(getString(R.string.position_song), index)
                            .putStringArrayListExtra(getString(R.string.songslist), filesNameList)
                            .putExtra(getString(R.string.songname), song_name)
                            .putExtra(getString(R.string.GENIUSFILENAME), genius_url)
                            .putExtra(getString(R.string.LYRICSFILE), lyrics_file_name)
                            .putExtra(getString(R.string.coming_from_playlist_activity)
                                    , getString(R.string.coming_from_playlist_activity))
                            .putExtra(getString(R.string.coming_from_extra_activity_int)
                                    ,20)

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
                                .setContext(mContext)
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


    private void getFilesListFromFolder(){
        int filesIndex=0;
        //String path = directory;
        Log.d("Folder Files", "Path: " + directory);
        File directoryFolder = new File(directory);
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
    }


    private void directorySetup(){
        //directory
        directory = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/Documents";

        File direct = new File(directory);

        if (!direct.exists()) {
            File myDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + this.getPackageName() + "/files/Documents");
            myDirectory.mkdir();
        }

    }
    private void internetConnectivity(){
        //Internet Connectivity
        connectivityManager = (ConnectivityManager)
                playlist_Activity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

    }



    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }



    private void layoutImageLoading() {
        playlist_background = findViewById(R.id.playlist_background);
        //imageView_sideButton = findViewById(R.id.playlist_sideButton);
        mSearchEditText = findViewById(R.id.searchEditText);

        Picasso
                .with(this)
                .load(R.drawable.playlist_blurred)
                //.resize(800, 800)

                .placeholder(R.drawable.madeinsociety)
                .into(playlist_background);

    }

    private void search(String s) {

        Query query = collectionReference
                .startAt(s)
                .endAt(s + "\uf8ff");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    System.err.println("Listen failed:" + e);
                    return;
                }
                arrayList = new ArrayList<>();

                for (DocumentSnapshot doc : queryDocumentSnapshots) {

                    RowItem rowItem = doc.toObject(RowItem.class);
                    arrayList.add(rowItem);
                }


            }
        });


    }



    public void download(final String fileNameInto, final String downloadDirectory,String download_url) {



        DownloadFiles downloadFiles = new DownloadFiles(
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


  /*  private boolean checkConnectivity() {
        boolean enabled = true;

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if ((info == null || !info.isConnected() )) {
            showToast("Fake one");
            return false;
        } else {
            return true;
        }

    }*/


    public void setupRecyclerView() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("songs");
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {
                Song song = dataSnapshot.getValue(Song.class);
                songs_list.add(song);
                adapter = new SongRecyclerView(mContext, R.layout.item, songs_list);
                listView.setAdapter(adapter);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @android.support.annotation.Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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
        if(onDownloadComplete != null){
            unregisterReceiver(onDownloadComplete);
        }


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






