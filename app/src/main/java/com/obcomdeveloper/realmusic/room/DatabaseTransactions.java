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
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DatabaseTransactions {
    private static final String TAG = "DatabaseTransactions";

    private Context context;
    private LocalDataSource mLocalDataSource;
    private NotesViewModel mNotesViewModel;

    public DatabaseTransactions(Context context) {
        this.context = context;

        mLocalDataSource=new LocalDataSource(context,CreateAndOpenCallBack);
        mNotesViewModel =new NotesViewModel(mLocalDataSource);
    }

    public Completable addSong(final SongDB song){

        Log.d(TAG, "addNewNote: adding new note to database");

        return mNotesViewModel.addSong(song)
                .subscribeOn(Schedulers.io());
    }

    public Observable<List<SongDB>> getAllSongs(){
        return mNotesViewModel
                .getAllSongs()
                .subscribeOn(Schedulers.io());
    }

    public Completable deleteSong(final SongDB song){

        return mNotesViewModel.deleteSong(song)
                .subscribeOn(Schedulers.io());

    }

    public Completable updateSong(SongDB song){
        return mNotesViewModel.
                updateSong(song)
                .subscribeOn(Schedulers.io());
    }

    RoomDatabase.Callback CreateAndOpenCallBack=new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            Log.d("CallBack", "onCreate: database callBack onCreate");

            //calls only once when database is created
            String song_name= String.valueOf(R.string.playlist_firstrun_lyrics_song_name);
            Completable newNote=addSong(new
                    SongDB(1, song_name,R.drawable.offline_playlist_small,"NF"));


            newNote.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {

                            Log.d(TAG, "onComplete: New Note Added");

                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            //calls everytime database is opened


        }

    };
}
