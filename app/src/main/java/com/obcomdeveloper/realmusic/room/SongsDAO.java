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
public interface SongsDAO {
    //Data Access Object
    //annotations do most of the job for inserting, just needs contact

    @Insert
    Completable addNewNote(SongDB songDB);

    @Update
    Completable updateNote(SongDB songDB);

    @Delete
    Completable deleteNote(SongDB songDB);

    @Query("select * from Songs")
    Observable<List<SongDB>> getAllNotes();


    @Query("select * from Songs where song_id ==:note_id")
    Observable<SongDB> getNote(long note_id);

}
