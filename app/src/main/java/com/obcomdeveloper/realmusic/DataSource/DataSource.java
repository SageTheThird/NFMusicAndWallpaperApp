package com.obcomdeveloper.realmusic.DataSource;

import com.nostra13.universalimageloader.utils.L;
import com.obcomdeveloper.realmusic.Models.Quote;
import com.obcomdeveloper.realmusic.Models.Song;
import com.obcomdeveloper.realmusic.Models.Wallpaper;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.internal.operators.observable.ObservableError;

public interface DataSource {


     Maybe<List<Song>> getSongsPlaylist(int page);

     Maybe<List<Song>> getSongExtras(int page);

     Maybe<List<Wallpaper>> getThumbsList(int page);


     Maybe<List<Wallpaper>> getLargeWallsList();

     Maybe<Quote> getQuotesArray();

}
