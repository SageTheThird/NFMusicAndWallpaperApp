package com.obcomdeveloper.realmusic.room;

import androidx.lifecycle.ViewModel;



import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class SongsOfflineViewModel extends ViewModel {


    private LocalDataSource mLocalDataSource;

    public SongsOfflineViewModel(LocalDataSource mLocalDataSource) {
        this.mLocalDataSource = mLocalDataSource;
    }

    public Observable<List<PlaylistEntity>> getAllSongs(){
        return mLocalDataSource.getAllSongs();
    }

    public Completable updateSong(PlaylistEntity song){
        return mLocalDataSource.updateNote(song);
    }

    public Observable<PlaylistEntity> getsong(long id){
        return mLocalDataSource.getSong(id);
    }

    public Completable addSong(PlaylistEntity song){
        return mLocalDataSource.addSong(song);
    }

    public Completable deleteSong(String song_name){
        return mLocalDataSource.deleteSong(song_name);
    }

    /*
    *
    * Below For Fetching Extras Data
    *
    * */


    public Observable<List<ExtrasEntity>> getAllSongsExtras(){
        return mLocalDataSource.getAllSongsExtras();
    }

    public Completable updateSongExtras(ExtrasEntity song){
        return mLocalDataSource.updateSongExtras(song);
    }

    public Observable<ExtrasEntity> getsongExtras(long id){
        return mLocalDataSource.getSongExtras(id);
    }

    public Completable addSongExtras(ExtrasEntity song){
        return mLocalDataSource.addSongExtras(song);
    }

    public Completable deleteSongExtras(String song_name){
        return mLocalDataSource.deleteSongExtras(song_name);
    }
}
