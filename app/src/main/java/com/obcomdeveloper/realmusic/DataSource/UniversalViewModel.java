package com.obcomdeveloper.realmusic.DataSource;

import android.content.Context;

import com.obcomdeveloper.realmusic.Models.Quote;
import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.Models.Wallpaper;

import java.util.List;

import io.reactivex.Maybe;

public class UniversalViewModel {

    private Repository mRepo;

    public UniversalViewModel(Context context){
        mRepo=new Repository(context);
    }


    public Maybe<List<Song>> getSongsPlaylist(int page){
        return mRepo.getSongsPlaylist(page);
    }

    public Maybe<List<Song>> getSongsExtrasList(int page){
        return mRepo.getSongExtras(page);
    }

    public Maybe<List<Wallpaper>> getThumbsList(int page){
        return mRepo.getThumbsList(page);
    }

    public Maybe<List<Wallpaper>> getLargeWallslist(){
        return mRepo.getLargeWallsList();
    }

    public Maybe<Quote> getQuotes(){
        return mRepo.getQuotesArray();
    }


}
