package com.obcomdeveloper.realmusic.room;

import android.content.Context;

import androidx.room.RoomDatabase;


import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class LocalDataSource implements PlaylistDataSource,ExtrasDataSource {

    private PlaylistDAO mPlaylistDao;
    private ExtrasDAO mExtrasDao;


    public LocalDataSource(Context context){
        SongsDatabase notesDatabase=SongsDatabase.getInstance(context);
        this.mPlaylistDao =notesDatabase.getPlaylistDAO();
        this.mExtrasDao=notesDatabase.getExtrasDAO();
    }

    public LocalDataSource(Context context, RoomDatabase.Callback callback) {


        SongsDatabase notesDatabase = SongsDatabase.getInstance(context, callback);
        this.mPlaylistDao = notesDatabase.getPlaylistDAO();
        this.mExtrasDao=notesDatabase.getExtrasDAO();
    }

    @Override
    public Observable<List<PlaylistEntity>> getAllSongs() {
        return mPlaylistDao.getAllSongs();
    }



    @Override
    public Completable addSong(PlaylistEntity song) {
        return mPlaylistDao.addNewSong(song);
    }

    @Override
    public Completable updateNote(PlaylistEntity song) {
        return mPlaylistDao.updateSong(song);
    }

    @Override
    public Completable deleteSong(String song_name) {
        return mPlaylistDao.deleteSong(song_name);
    }

    @Override
    public Observable<PlaylistEntity> getSong(long id) {
        return mPlaylistDao.getSong(id);
    }

    @Override
    public Observable<List<ExtrasEntity>> getAllSongsExtras() {
        return mExtrasDao.getAllSongs();
    }

    @Override
    public Completable addSongExtras(ExtrasEntity song) {
        return mExtrasDao.addNewSong(song);
    }

    @Override
    public Completable updateSongExtras(ExtrasEntity song) {
        return mExtrasDao.updateSong(song);
    }

    @Override
    public Completable deleteSongExtras(String song_name) {
        return mExtrasDao.deleteSong(song_name);
    }

    @Override
    public Observable<ExtrasEntity> getSongExtras(long id) {
        return mExtrasDao.getSong(id);
    }
}
