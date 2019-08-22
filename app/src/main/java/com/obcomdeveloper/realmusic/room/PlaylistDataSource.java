package com.obcomdeveloper.realmusic.room;



import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;


public interface PlaylistDataSource {


    Observable<List<PlaylistEntity>> getAllSongs();

    Completable addSong(PlaylistEntity song);

    Completable updateNote(PlaylistEntity song);

    Completable deleteSong(String song_name);

    Observable<PlaylistEntity> getSong(long id);


}
