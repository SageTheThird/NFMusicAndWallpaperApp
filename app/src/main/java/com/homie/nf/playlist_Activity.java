package com.homie.nf;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class playlist_Activity extends AppCompatActivity {



    private FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=firebaseFirestore.collection("songs");
    static RecyclerView recyclerView;
    ImageRecyclerView imageRecyclerViewAdapter;


    //static ListView listView_songs;


    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    ImageView playlist_background,imageView_title,imageView_sideButton;
    ArrayList<String> song_list;
    Map<String, Integer> songs_map;
    String directorypath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playlist_);
        playlist_background=findViewById(R.id.playlist_background);
        imageView_sideButton=findViewById(R.id.playlist_sideButton);
        imageView_title=findViewById(R.id.playlistimageView_title);
        song_list=new ArrayList<>();


        Picasso
                .with(this)
                .load(R.drawable.playlist_notblurred)
                .resize(700,700)
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


        String path = Environment.getExternalStorageDirectory().getPath() + "/Documents";

        final String directory=Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/Documents";


        File direct = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" + this.getPackageName() + "/files/Documents");

        if (!direct.exists()) {
            File myDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + this.getPackageName() + "/files/Documents");
            myDirectory.mkdir();
        }

        setupRecyclerView();




        imageRecyclerViewAdapter.setOnItemClickListener(new ImageRecyclerView.onItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

               /* RowItem rowItem=documentSnapshot.toObject(RowItem.class);
                String id=documentSnapshot.getId();
                String path=documentSnapshot.getReference().getPath();
                String downloadUrl=documentSnapshot.getString("downloadUrl");*/
                String genius_url=documentSnapshot.getString("genius_url");

                String song_name=documentSnapshot.getString("song_name");

                String lyrics=documentSnapshot.getString("song_name").replace(".mp3",".txt");

                String path101 = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Documents/"+song_name;

                if (new File(path101).exists()) {

                    //Do something

                    showToast("Playing..");
                    startActivity(new Intent(playlist_Activity.this, PlayerActivity.class)
                            .putExtra("songname", song_name)
                            .putExtra("GENIUSFILENAME",genius_url)
                            .putExtra("LYRICSFILE",lyrics)
                    );



                } else {

                    download(song_name,directory);
                    showToast("Downloading File...");

                }


            }


                /*startActivity(new Intent(playlist_Activity.this,PlayerActivity.class)
                        .putExtra("downloadUrl",downloadUrl));*/




        });


    }





    public void download(final String fileNameInto, final String downloadDirectory) {

        storageReference = firebaseStorage.getInstance().getReference();
        storageReference = storageReference.child(fileNameInto);
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //after download url from file is fetched it will download file

                String url = uri.toString();
                // downloadFiles(playlist_Activity.this, fileNameInto, "", Environment.DIRECTORY_DOCUMENTS, url);

                DownloadFiles downloadFiles=new DownloadFiles(playlist_Activity.this,"",downloadDirectory,fileNameInto);
                downloadFiles.execute(url);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Failed to connect to server:");


            }
        });

    }


    private boolean checkConnectivity() {
        boolean enabled = true;

        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if ((info == null || !info.isConnected() )) {
            showToast("No internet connection!");
            return false;
        } else {
            return true;
        }

    }






    public void setupRecyclerView (){
        Query query=collectionReference.orderBy("priority",Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<RowItem> firebaseRecyclerOptions=new FirestoreRecyclerOptions.Builder<RowItem>()
                .setQuery(query,RowItem.class)
                .build();

        imageRecyclerViewAdapter=new ImageRecyclerView(firebaseRecyclerOptions,this);

        recyclerView=findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(imageRecyclerViewAdapter);




    }
    @Override
    protected void onStart() {
        super.onStart();
        imageRecyclerViewAdapter.startListening();
    }


    @Override
    protected void onStop() {
        super.onStop();
        imageRecyclerViewAdapter.stopListening();

    }

    public void showToast(String msg){

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }




}

class DownloadFiles extends AsyncTask<String,Void,Void>{

    private Context context;
    private String fileExtension;
    private String destinationDirectory;
    private String fileName;

    public DownloadFiles(Context context, String fileExtension, String destinationDirectory,String fileName) {
        this.context = context;
        this.fileExtension = fileExtension;
        this.destinationDirectory = destinationDirectory;
        this.fileName=fileName;
    }

    @Override
    protected Void doInBackground(String... url) {


        DownloadManager downloadManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            downloadManager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
        }
        Uri uri = Uri.parse(url[0]);
        DownloadManager.Request request = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            request = new DownloadManager.Request(uri);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            request.setDestinationUri(Uri.fromFile(new File(destinationDirectory,fileName+fileExtension)));
        }

        if(downloadManager!=null) {
            downloadManager.enqueue(request);
        }






        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context, "File Downloaded:", Toast.LENGTH_LONG).show();
    }
}




