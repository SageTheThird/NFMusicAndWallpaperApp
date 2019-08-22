package com.obcomdeveloper.realmusic.room;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.obcomdeveloper.realmusic.R;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DatabaseTransactions {
    private static final String TAG = "DatabaseTransactions";

    private Context context;
    private LocalDataSource mLocalDataSource;
    private SongsOfflineViewModel mSongsOfflineViewModel;
    private CompositeDisposable mDisposibles=new CompositeDisposable();

    public DatabaseTransactions(Context context) {
        this.context = context;

        mLocalDataSource=new LocalDataSource(context,CreateAndOpenCallBack);
        mSongsOfflineViewModel =new SongsOfflineViewModel(mLocalDataSource);
    }

    public Completable addSong(final PlaylistEntity song){

        Log.d(TAG, "addNewSong: adding new note to database");

        return mSongsOfflineViewModel.addSong(song)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<PlaylistEntity>> getAllSongs(){
        return mSongsOfflineViewModel
                .getAllSongs()
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteSong(final String song_name){

        return mSongsOfflineViewModel.deleteSong(song_name)
                .subscribeOn(Schedulers.io());

    }

    public Completable updateSong(PlaylistEntity song){
        return mSongsOfflineViewModel.
                updateSong(song)
                .subscribeOn(Schedulers.io());
    }

    RoomDatabase.Callback CreateAndOpenCallBack=new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            Log.d("CallBack", "onCreate: database callBack onCreate");

            //calls only once when database is created

        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            //calls everytime database is opened


        }

    };






    public Completable addSongExtras(final ExtrasEntity song){

        Log.d(TAG, "addNewSong: adding new note to database");

        return mSongsOfflineViewModel.addSongExtras(song)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<ExtrasEntity>> getAllSongsExtras(){
        return mSongsOfflineViewModel
                .getAllSongsExtras()
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteSongExtras(final String song_name){

        return mSongsOfflineViewModel.deleteSongExtras(song_name)
                .subscribeOn(Schedulers.io());

    }

    public Completable updateSongExtras(ExtrasEntity song){
        return mSongsOfflineViewModel.
                updateSongExtras(song)
                .subscribeOn(Schedulers.io());
    }
}
