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
public interface PlaylistDAO {
    //Data Access Object
    //annotations do most of the job for inserting, just needs contact

    @Insert
    Completable addNewSong(PlaylistEntity playlistEntity);

    @Update
    Completable updateSong(PlaylistEntity playlistEntity);

    @Query("DELETE FROM Playlist WHERE song_name = :song_name")
    Completable deleteSong(String song_name);

    @Query("select * from Playlist")
    Observable<List<PlaylistEntity>> getAllSongs();


    @Query("select * from Playlist where song_id ==:id")
    Observable<PlaylistEntity> getSong(long id);

}
