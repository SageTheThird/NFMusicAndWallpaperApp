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
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.homie.nf.Adapters.SongRecyclerView;
import com.homie.nf.Models.RowItem;
import com.homie.nf.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import javax.annotation.Nullable;

import dmax.dialog.SpotsDialog;

import static com.homie.nf.Songs.playlist_Activity.dialog;


public class playlist_Activity extends AppCompatActivity {
    private static final String TAG = "playlist_Activity";
    public static RecyclerView recyclerView;
    static AlertDialog dialog;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;
    private SongRecyclerView songRecyclerViewAdapter;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ImageView playlist_background, imageView_title, imageView_sideButton;
    private ArrayList<String> song_list;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private TextInputLayout searchInputLayout;
    //static Progress progress;
    private ArrayList<RowItem> arrayList;
    private BroadcastReceiver onDownloadComplete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playlist_);




        song_list = new ArrayList<>();

        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("songs");


        picassoImageLoading();

        final String directory = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/Documents";


        File direct = new File(directory);

        if (!direct.exists()) {
            File myDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + this.getPackageName() + "/files/Documents");
            myDirectory.mkdir();
        }

        setupRecyclerView();
        //Internet Connectivity
        connectivityManager = (ConnectivityManager)
                playlist_Activity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();


        songRecyclerViewAdapter.setOnItemClickListener(new SongRecyclerView.onItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

               /* RowItem rowItem=documentSnapshot.toObject(RowItem.class);
                String id=documentSnapshot.getId();
                String path=documentSnapshot.getReference().getPath();
                String downloadUrl=documentSnapshot.getString("downloadUrl");*/


                String genius_url = documentSnapshot.getString("genius_url");
                String song_name = documentSnapshot.getString("song_name");
                String songFull = song_name + ".mp3";

                String lyricsFetch = documentSnapshot.getString("song_name");
                String lyrics_file_name = lyricsFetch.replace(".mp3", ".txt");

                String path101 = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Documents/" + song_name;


                if (new File(path101).exists()) {

                    //Do something


                    showToast("Playing..");
                    startActivity(new Intent(playlist_Activity.this, PlayerActivity.class)
                            .putExtra("songname", song_name)
                            .putExtra("GENIUSFILENAME", genius_url)
                            .putExtra("LYRICSFILE", lyrics_file_name)
                    );


                } else {
                    if ((networkInfo == null || !networkInfo.isConnected())) {
                        showToast("Please Check Your Internet Connection!");
                        return;
                    } else {

                        //dialog for when downloading
                        dialog = new SpotsDialog.Builder()
                                .setContext(playlist_Activity.this)
                                .setTheme(R.style.Custom)
                                .setCancelable(false)
                                .build();
                        //download file
                        download(song_name, directory);


                    }


                }


            }


                /*startActivity(new Intent(playlist_Activity.this,PlayerActivity.class)
                        .putExtra("downloadUrl",downloadUrl));*/


        });


    }

    private void picassoImageLoading() {
        playlist_background = findViewById(R.id.playlist_background);
        imageView_sideButton = findViewById(R.id.playlist_sideButton);
        imageView_title = findViewById(R.id.playlistimageView_title);

        Picasso
                .with(this)
                .load(R.drawable.playlist_blurred)
                .resize(700, 700)
                .into(playlist_background);

        Picasso
                .with(this)
                .load(R.drawable.title)
                // .resize(700,700)

                .placeholder(R.drawable.back_arrow)
                .into(imageView_title);
        Picasso
                .with(this)
                .load(R.drawable.sidebutton)
                // .resize(700,700)

                .placeholder(R.drawable.ic_search_black_24dp)
                .into(imageView_sideButton);

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


    public void download(final String fileNameInto, final String downloadDirectory) {

        storageReference = firebaseStorage.getInstance().getReference("songs");
        storageReference = storageReference.child(fileNameInto);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //after download url from file is fetched it will download file

                String url = uri.toString();
                DownloadFiles downloadFiles = new DownloadFiles(
                        playlist_Activity.this,
                        "",
                        downloadDirectory,
                        fileNameInto,
                        onDownloadComplete);

                final long downloadid = downloadFiles.downloadingFiles(url);

                initializeReciever(downloadid);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Failed to connect to server:");
            }
        });

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
        Query query = collectionReference.orderBy("id", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<RowItem> firebaseRecyclerOptions = new FirestoreRecyclerOptions.Builder<RowItem>()
                .setQuery(query, RowItem.class)
                .build();

        songRecyclerViewAdapter = new SongRecyclerView(firebaseRecyclerOptions, this);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(songRecyclerViewAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();
        songRecyclerViewAdapter.startListening();
    }


    @Override
    protected void onStop() {
        super.onStop();

        songRecyclerViewAdapter.stopListening();

    }

    public void showToast(String msg) {

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();
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






