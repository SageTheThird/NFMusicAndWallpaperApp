package com.homie.nf.Songs;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.homie.nf.Adapters.SongRecyclerView;
import com.homie.nf.Models.RowItem;
import com.homie.nf.Models.Song;
import com.homie.nf.R;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;
import dmax.dialog.SpotsDialog;
import static com.homie.nf.Songs.playlist_Activity.dialog;


public class playlist_Activity extends AppCompatActivity {
    private static final String TAG = "playlist_Activity";
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
    private String directory;


    private ArrayList<String> filesNameList=new ArrayList<>();

    private SongRecyclerView adapter;

    private List<Song> songs_list;

    private EditText searchView;

    private TextView patentTextView;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_playlist_);

        searchView=findViewById(R.id.searchEdit);
        listView = findViewById(R.id.listViewPlaylist);
        patentTextView =findViewById(R.id.patent_playlist);
        songs_list=new ArrayList<>();


        patentTextView.setHorizontallyScrolling(true);
        patentTextView.setSelected(true);

        //methods
        directorySetup();
        getFilesListFromFolder();
        layoutImageLoading();
        internetConnectivity();
        setupListView();

        //listeners
        listView.setOnItemClickListener(SongClickListener);


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
                    adapter.filter(text);
                }catch (NullPointerException e){
                    Log.d(TAG, "afterTextChanged: NullPointerException" +e.getMessage());
                }

            }
        });
    }

    AdapterView.OnItemClickListener SongClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String download_url=songs_list.get(position).getDownload_url();
            String song_name = songs_list.get(position).getSong_name();
            String songFull = song_name + getString(R.string.mp3_extenstion);



            //if file is there in the fileNameList i-e present in the folder it will play it
            if (filesNameList.contains(songFull)) {
                int index=filesNameList.indexOf(songFull);
                Log.d(TAG, "onItemClick: song found in the arrayList/directory");

                startActivity(new Intent(mContext, PlayerActivity.class)
                        .putExtra(getString(R.string.current_index), index)
                        .putStringArrayListExtra(getString(R.string.folder_songs_list), filesNameList)
                        .putParcelableArrayListExtra(getString(R.string.all_songs_object_list), (ArrayList<? extends Parcelable>) songs_list)
                        .putExtra(getString(R.string.coming_from_playlist_activity)
                                , getString(R.string.coming_from_playlist_activity))
                        .putExtra(getString(R.string.coming_from_playlist_activity_int)
                                ,PLAYLIST_IDENTIFIER)

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
    };


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


    private void layoutImageLoading() {
        playlist_background = findViewById(R.id.playlist_background);
        Picasso
                .with(this)
                .load(R.drawable.playlist_blurred)
                //.resize(800, 800)

                .placeholder(R.drawable.madeinsociety)
                .into(playlist_background);
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


                    Toast.makeText(context, "File Loaded!", Toast.LENGTH_SHORT).show();
                    getFilesListFromFolder();
                    adapter.notifyDataSetChanged();
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
    public void setupListView() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("songs");
        final ProgressDialog dialog=new ProgressDialog(this,R.style.MyAlertDialogStyle);
        dialog.setTitle("Loading..");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

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

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Song song = dataSnapshot.getValue(Song.class);
                songs_list.add(song);

                //when reaches the final child and List is complete it will reverse the list
                if(songs_list.size() == getChildCountPref(getString(R.string.child_count_playlist),mContext)){
                    Collections.reverse(songs_list);
                }

                adapter = new SongRecyclerView(mContext, R.layout.item, songs_list);
                listView.setAdapter(adapter);
                dialog.dismiss();

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






