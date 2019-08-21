package com.obcomdeveloper.realmusic.DataSource;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.obcomdeveloper.realmusic.Models.Quote;
import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.Models.Wallpaper;
import com.obcomdeveloper.realmusic.Utils.SharedPreferences;

import java.util.List;

import durdinapps.rxfirebase2.DataSnapshotMapper;
import durdinapps.rxfirebase2.RxFirebaseDatabase;
import io.reactivex.Maybe;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class Repository implements DataSource {
    private static final String TAG = "Repository";
    public static final int ITEMS_PER_PAGE =10;

    private Context context;
    private DatabaseReferences mRefs;
    private SharedPreferences mSharedPrefs;
    private int childCOunt;
    private int estimatePages;
    private int endingPage;
    private Query query;

    public Repository(Context context) {
        this.context=context;
        mRefs=new DatabaseReferences(context);
        mSharedPrefs=new SharedPreferences(context);

    }

    public void getChildCount(DatabaseReference reference){
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int tempChildCount=(int)dataSnapshot.getChildrenCount();
                mSharedPrefs.saveInt("paginationChildCount",tempChildCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getQuery(DatabaseReference reference, int page){
        childCOunt=mSharedPrefs.getInt("paginationChildCount",64);
        estimatePages= childCOunt / ITEMS_PER_PAGE; //=6
        mSharedPrefs.saveInt("estimatePages",estimatePages);
        endingPage = estimatePages + 1;

        if (page <= estimatePages){

            query = reference
                    .orderByKey()
                    .limitToLast( page * ITEMS_PER_PAGE);
        }
        else if(page == endingPage){

            int lastCount=childCOunt % ITEMS_PER_PAGE;
            query = reference
                    .orderByKey()
                    //.startAt(lastNode)
                    .limitToFirst(lastCount);
        }
    }

    @Override
    public Maybe<List<Song>> getSongsPlaylist(int page) {

        DatabaseReference ref2=mRefs.playlistReference;
        getChildCount(ref2);
        getQuery(ref2,page);

        return RxFirebaseDatabase.observeSingleValueEvent(query
                , DataSnapshotMapper.listOf(Song.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Maybe<List<Song>> getSongExtras(int page) {

        DatabaseReference ref2=mRefs.extrasReference;
        getChildCount(ref2);
        getQuery(ref2,page);


        return RxFirebaseDatabase.observeSingleValueEvent(query
                , DataSnapshotMapper.listOf(Song.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Maybe<List<Wallpaper>> getThumbsList(int page) {


        DatabaseReference ref2=mRefs.thumbsReference;

        getChildCount(ref2);
        getQuery(ref2,page);

        return RxFirebaseDatabase.observeSingleValueEvent(query
                , DataSnapshotMapper.listOf(Wallpaper.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }






    @Override
    public Maybe<List<Wallpaper>> getLargeWallsList() {
        return RxFirebaseDatabase.observeSingleValueEvent(mRefs.largeWallsReference
                ,DataSnapshotMapper.listOf(Wallpaper.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Maybe<Quote> getQuotesArray() {
        return RxFirebaseDatabase.observeSingleValueEvent(mRefs.quotesReference
                , DataSnapshotMapper.of(Quote.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
