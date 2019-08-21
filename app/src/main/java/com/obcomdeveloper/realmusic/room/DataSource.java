package com.obcomdeveloper.realmusic.room;



import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;


public interface DataSource {


    Observable<List<SongDB>> getAllSongs();

    Completable addSong(SongDB song);

    Completable updateNote(SongDB song);

    Completable deleteSong(SongDB song);

    Observable<SongDB> getSong(long id);


}
