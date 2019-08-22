package com.obcomdeveloper.realmusic.room;



import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;


public interface ExtrasDataSource {


    Observable<List<ExtrasEntity>> getAllSongsExtras();

    Completable addSongExtras(ExtrasEntity song);

    Completable updateSongExtras(ExtrasEntity song);

    Completable deleteSongExtras(String song_name);

    Observable<ExtrasEntity> getSongExtras(long id);


}
