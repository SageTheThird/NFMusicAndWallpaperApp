package com.obcomdeveloper.realmusic.DataSource;

import android.app.DownloadManager;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;

public class Repository implements DataSource {
    private static final String TAG = "Repository";

    private Context context;
    private DatabaseReferences mRefs;
    private SharedPreferences mSharedPrefs;

    public Repository(Context context) {
        this.context=context;
        mRefs=new DatabaseReferences(context);
        mSharedPrefs=new SharedPreferences(context);

    }

    @Override
    public Maybe<List<Song>> getSongsPlaylist(int page) {

        DatabaseReference ref2=mRefs.playlistReference;
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot.getChildrenCount());
                int childCOunt=(int)dataSnapshot.getChildrenCount();
                mSharedPrefs.saveInt("paginationChildCount",childCOunt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        int numberOfElemets=10;
        int childCOunt=mSharedPrefs.getInt("paginationChildCount",64);
        String startAt=String.valueOf(childCOunt- (page * 10));  //55  45
        String endAt=String.valueOf(Integer.parseInt(startAt) + 10);  //64 55
                                    //childCount

        //will give us 2nd last page
        int endingPage= childCOunt / 10; //=6

        if(Integer.parseInt(startAt)  < 10 && page <= endingPage+1){
            //means if there are less than 10 elements then divide childCount by 10 unless u get a reminder
            startAt="0";
            endAt=String.valueOf(childCOunt % 10) ;
        }

        Query query2=mRefs.playlistReference.orderByChild("id").startAt(startAt).endAt(endAt);
        //Query query=mRefs.playlistReference.limitToFirst();

        return RxFirebaseDatabase.observeSingleValueEvent(query2
                              , DataSnapshotMapper.listOf(Song.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        //child count 64
        //page 1: 55 - 64
        //page 2: 44 - 54

        //page 1
        //  startAt: childCOunt - 10 = 55 = var
        // endAt: childCount

        //page 2
        //startAt (var - 10)  = 46 = var
        //endAt (var)

        //page 3
        //startAt (var - 10)
        //endAt (var)

    }

    @Override
    public Maybe<List<Song>> getSongExtras() {
        return RxFirebaseDatabase.observeSingleValueEvent(mRefs.extrasReference
                ,DataSnapshotMapper.listOf(Song.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Maybe<List<Wallpaper>> getThumbsList(int page) {


        DatabaseReference ref2=mRefs.thumbsReference;
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: "+dataSnapshot.getChildrenCount());
                int childCOunt=(int)dataSnapshot.getChildrenCount();
                mSharedPrefs.saveInt("paginationChildCount",childCOunt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        int childCOunt=mSharedPrefs.getInt("paginationChildCount",64);
        String startAt=String.valueOf(childCOunt- (page * 9));
        String endAt=String.valueOf(childCOunt - (Integer.parseInt(startAt) + 9));

        //child count 64
        //page 1: 55 - 64
        //page 2: 44 - 54

        //page 1
        //  startAt: childCOunt - 10 = 55 = var
        // endAt: childCount

        //page 2
        //startAt (var - 10)  = 46 = var
        //endAt (var)

        //page 3
        //startAt (var - 10)
        //endAt (var)

        Query query2=mRefs.thumbsReference.orderByChild("id").startAt(startAt).endAt(endAt);
        //Query query=mRefs.playlistReference.limitToFirst();

        return RxFirebaseDatabase.observeSingleValueEvent(query2
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
