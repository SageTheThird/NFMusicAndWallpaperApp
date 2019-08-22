package com.obcomdeveloper.realmusic.room;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

@Dao
public interface ExtrasDAO {
    //Data Access Object
    //annotations do most of the job for inserting, just needs contact

    @Insert
    Completable addNewSong(ExtrasEntity playlistEntity);

    @Update
    Completable updateSong(ExtrasEntity playlistEntity);

    @Query("DELETE FROM Extras WHERE song_name = :song_name")
    Completable deleteSong(String song_name);

    @Query("select * from Extras")
    Observable<List<ExtrasEntity>> getAllSongs();


    @Query("select * from Extras where song_id ==:note_id")
    Observable<ExtrasEntity> getSong(long note_id);

}
