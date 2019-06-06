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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
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
import com.homie.nf.Utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
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
    private StorageReference storageReference;
    private ImageView playlist_background, imageView_title, tick_imageView;
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    //static Progress progress;
    private ArrayList<RowItem> arrayList;
    private BroadcastReceiver onDownloadComplete;
    private Context mContext=playlist_Activity.this;
    private String directory;


    private ArrayList<String> filesNameList=new ArrayList<>();
    private EditText mSearchEditText;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playlist_);


        //references
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection(getString(R.string.store_song_db));

        //methods
        directorySetup();
        getFilesListFromFolder();
        initImageLoader();
        layoutImageLoading();
        internetConnectivity();
        setupRecyclerView();

        //listeners
        songRecyclerViewAdapter.setOnItemClickListener(SongClickListener);

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

    SongRecyclerView.onItemClickListener SongClickListener=new SongRecyclerView.onItemClickListener() {
        @Override
        public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

            String genius_url = documentSnapshot.getString(getString(R.string.field_genius_url));
            String song_name = documentSnapshot.getString(getString(R.string.field_song_name));
            String songFull = song_name + getString(R.string.mp3_extenstion);

            String lyricsFetchfromSongName = documentSnapshot.getString(getString(R.string.field_song_name));
            String lyrics_file_name = lyricsFetchfromSongName
                    .replace(getString(R.string.mp3_extenstion), getString(R.string.txt_extenstion));

            //if file is there in the fileNameList i-e present in the folder it will play it
               if(filesNameList.contains(documentSnapshot.getString(getString(R.string.field_song_name)))){
                   Log.d(TAG, "onItemClick: song found in the arrayList/directory");
                   showToast("Playing..");

                   startActivity(new Intent(playlist_Activity.this, PlayerActivity.class)
                           .putExtra(getString(R.string.position_song),position)
                           .putStringArrayListExtra(getString(R.string.songslist),filesNameList)
                           .putExtra(getString(R.string.songname), song_name)
                           .putExtra(getString(R.string.GENIUSFILENAME), genius_url)
                           .putExtra(getString(R.string.LYRICSFILE), lyrics_file_name)
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
                               .setContext(playlist_Activity.this)
                               .setTheme(R.style.Custom)
                               .setCancelable(false)
                               .build();
                       //download file
                       download(song_name, directory);
                   }
               }
        }
    };
    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }



    private void layoutImageLoading() {
        playlist_background = findViewById(R.id.playlist_background);
        //imageView_sideButton = findViewById(R.id.playlist_sideButton);
        imageView_title = findViewById(R.id.playlistimageView_title);
        tick_imageView = findViewById(R.id.tick);
        mSearchEditText = findViewById(R.id.searchEditText);


        /*//Playlist Background Image
        String imageUri1 = getString(R.string.drawable_universal) + R.drawable.playlist_blurred;
        UniversalImageLoader.setImage(imageUri1,playlist_background,null,"");

        //Side Button ImageView
        String imageUri2 = getString(R.string.drawable_universal) + R.drawable.ic_drawer;
        UniversalImageLoader.setImage(imageUri2,imageView_sideButton,null,"");

        //title ImageView
        //Side Button ImageView
        String imageUri3 = getString(R.string.drawable_universal) + R.drawable.title;
        UniversalImageLoader.setImage(imageUri3,imageView_title,null,"");*/

        Picasso
                .with(this)
                .load(R.drawable.title)
                // .resize(700,700)

                .placeholder(R.drawable.back_arrow)
                .into(imageView_title);
       /* Picasso
                .with(this)
                .load(R.drawable.ic_drawer)
                // .resize(700,700)

                .placeholder(R.drawable.ic_search_black_24dp)
                .into(imageView_sideButton);*/
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


    public void download(final String fileNameInto, final String downloadDirectory) {

        storageReference = FirebaseStorage.getInstance().getReference(getString(R.string.storage_song_db));
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
                showToast("Failed to connect to server: "+e.getMessage());
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
                    getFilesListFromFolder();
                    songRecyclerViewAdapter.notifyDataSetChanged();
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
        Query query = collectionReference.orderBy(getString(R.string.field_id), Query.Direction.DESCENDING);

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






