package com.obcomdeveloper.realmusic.room;

import android.content.Context;

import androidx.room.RoomDatabase;


import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class LocalDataSource implements DataSource {

    private SongsDAO mNotesDao;


    public LocalDataSource(Context context){
        SongsDatabase notesDatabase=SongsDatabase.getInstance(context);
        this.mNotesDao=notesDatabase.getContactDAO();
    }

    public LocalDataSource(Context context, RoomDatabase.Callback callback) {


        SongsDatabase notesDatabase = SongsDatabase.getInstance(context, callback);
        this.mNotesDao = notesDatabase.getContactDAO();
    }

    @Override
    public Observable<List<SongDB>> getAllSongs() {
        return mNotesDao.getAllNotes();
    }

    @Override
    public Completable addSong(SongDB song) {
        return mNotesDao.addNewNote(song);
    }

    @Override
    public Completable updateNote(SongDB song) {
        return mNotesDao.updateNote(song);
    }

    @Override
    public Completable deleteSong(SongDB song) {
        return mNotesDao.deleteNote(song);
    }

    @Override
    public Observable<SongDB> getSong(long id) {
        return mNotesDao.getNote(id);
    }
}
