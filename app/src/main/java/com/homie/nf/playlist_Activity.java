package com.homie.nf;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class playlist_Activity extends AppCompatActivity {


    static ListView listView_songs;


    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageReference ref;


    String[] member_names;
    TypedArray profile_pics;
    String[] statues;


    List<RowItem> rowItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playlist_);
        String path = Environment.getExternalStorageDirectory().getPath() + "Documents";


        File direct = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + this.getPackageName() + "/files/Documents");

        if (!direct.exists()) {
            File myDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + this.getPackageName() + "/files/Documents");
            myDirectory.mkdir();
        }


        listView_songs = findViewById(R.id.listView_songs);

        rowItems = new ArrayList<RowItem>();

        member_names = getResources().getStringArray(R.array.Member_names);

        profile_pics = getResources().obtainTypedArray(R.array.profile_pics);

        statues = getResources().getStringArray(R.array.statues);


        for (int i = 0; i < member_names.length; i++) {
            RowItem item = new RowItem(member_names[i],
                    profile_pics.getResourceId(i, -1), statues[i]);
            rowItems.add(item);
        }

        CustomAdapter adapter = new CustomAdapter(this, rowItems);
        listView_songs.setAdapter(adapter);


        listView_songs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = rowItems.get(position).getMember_name();

                String nextFileName = listView_songs.getItemAtPosition(position + 1).toString();
                Log.i("Next File Name: ", nextFileName);


                String path101 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + getApplicationContext().getPackageName() + "/files/Documents/" + fileName;
                if (new File(path101).exists()) {

                    //Do something
                    Toast.makeText(playlist_Activity.this, "File Exists", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(playlist_Activity.this, PlayerActivity.class)
                            .putExtra("songname", fileName).putExtra("nextsong", nextFileName));


                } else {


                    download(fileName);
                    Toast.makeText(playlist_Activity.this, "Downloading", Toast.LENGTH_SHORT).show();

                }
            }


        });
    }


    public void download(final String fileNameInto) {

        storageReference = firebaseStorage.getInstance().getReference();
        ref = storageReference.child(fileNameInto);
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //after download url from file is fetched it will download file

                String url = uri.toString();
                downloadFiles(playlist_Activity.this, fileNameInto, "", Environment.DIRECTORY_DOCUMENTS, url);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        });

    }

    public void downloadFiles(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {

        DownloadManager downloadManager = (DownloadManager) context
                .getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);

        downloadManager.enqueue(request);
        // Toast.makeText(playlist_Activity.this, "File Stored For offline Use", Toast.LENGTH_SHORT).show();


    }
}




