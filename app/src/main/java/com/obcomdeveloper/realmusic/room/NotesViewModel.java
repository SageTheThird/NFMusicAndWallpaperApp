package com.obcomdeveloper.realmusic.room;

import androidx.lifecycle.ViewModel;



import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class NotesViewModel extends ViewModel {


    private LocalDataSource mLocalDataSource;

    public NotesViewModel(LocalDataSource mLocalDataSource) {
        this.mLocalDataSource = mLocalDataSource;
    }

    public Observable<List<SongDB>> getAllSongs(){
        return mLocalDataSource.getAllSongs();
    }

    public Completable updateSong(SongDB song){
        return mLocalDataSource.updateNote(song);
    }

    public Observable<SongDB> getsong(long id){
        return mLocalDataSource.getSong(id);
    }

    public Completable addSong(SongDB song){
        return mLocalDataSource.addSong(song);
    }

    public Completable deleteSong(SongDB song){
        return mLocalDataSource.deleteSong(song);
    }
}
