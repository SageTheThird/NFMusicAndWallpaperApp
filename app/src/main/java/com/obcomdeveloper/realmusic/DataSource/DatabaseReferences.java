package com.obcomdeveloper.realmusic.DataSource;

import android.content.Context;
import android.provider.ContactsContract;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.obcomdeveloper.realmusic.MyApplication;
import com.obcomdeveloper.realmusic.R;

public class DatabaseReferences {

    private Context context;

    public DatabaseReferences(Context context) {
        this.context=context;
    }

    public  DatabaseReference playlistReference= FirebaseDatabase
            .getInstance().getReference()
            .child("songs");

    public  DatabaseReference extrasReference= FirebaseDatabase
            .getInstance().getReference()
            .child("extras");

    public  DatabaseReference thumbsReference= FirebaseDatabase
            .getInstance().getReference()
            .child("resized_wallpapers");

    public  DatabaseReference largeWallsReference= FirebaseDatabase
            .getInstance().getReference()
            .child("wallpapers");

    public DatabaseReference quotesReference=FirebaseDatabase.getInstance()
            .getReference().child("random_quotes");

}
