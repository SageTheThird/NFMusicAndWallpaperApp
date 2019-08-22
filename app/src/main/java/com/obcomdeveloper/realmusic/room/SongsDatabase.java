package com.obcomdeveloper.realmusic.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {PlaylistEntity.class, ExtrasEntity.class},version = 1)
public abstract class SongsDatabase extends RoomDatabase {

    public static final String DATABASE_NAME="SongsDB";

    public abstract PlaylistDAO getPlaylistDAO();
    public abstract ExtrasDAO getExtrasDAO();

    private static volatile SongsDatabase INSTANCE;


    public static SongsDatabase getInstance(Context context, RoomDatabase.Callback callback) {
        if (INSTANCE == null) {
            synchronized (SongsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SongsDatabase.class, DATABASE_NAME).addCallback(callback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static SongsDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SongsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SongsDatabase.class, DATABASE_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
